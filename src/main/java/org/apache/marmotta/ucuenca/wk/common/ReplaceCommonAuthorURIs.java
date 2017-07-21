/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.common;

import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.pgsql.PostgreSQLDialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.ucuenca.wk.commons.impl.DistanceServiceImpl;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ReplaceCommonAuthorURIs {

    KiWiDialect postgresDialet = new PostgreSQLDialect();
    KiWiConfiguration conf;
    KiWiStore store;
    Repository repository;
    DistanceServiceImpl distance = new DistanceServiceImpl();
    URI graph = ValueFactoryImpl.getInstance().createURI("http://ucuenca.edu.ec/wkhuska");

    public ReplaceCommonAuthorURIs(String name, String jdbc, String username, String password) throws RepositoryException, MalformedQueryException, QueryEvaluationException, UpdateExecutionException {
        conf = new KiWiConfiguration(name, jdbc, username, password, postgresDialet);
        store = new KiWiStore(conf);
        repository = new SailRepository(store);
        // Used to print contexts.
        repository.initialize();
        RepositoryConnection con = repository.getConnection();
        con.begin();
        for (Resource object : Iterations.asList(con.getContextIDs())) {
            System.out.println(object);
        }
        con.commit();
        con.close();

        identifySameURIs();
        repository.shutDown();
    }

    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, UpdateExecutionException {
        new ReplaceCommonAuthorURIs("Test", "jdbc:postgresql://localhost:5432/lmf?prepareThreshold=3", "marmotta", "marmotta");
    }

    private void identifySameURIs() throws RepositoryException, MalformedQueryException, QueryEvaluationException, UpdateExecutionException {
        RepositoryConnection con = repository.getConnection();
        con.begin();
        ValueFactory vf = con.getValueFactory();
        TupleQueryResult authors = ((TupleQuery) con.prepareTupleQuery(QueryLanguage.SPARQL, getAuthors())).evaluate();
        while (authors.hasNext()) {
            Resource author = vf.createURI(authors.next().getValue("author").stringValue());

            // Get author name
            Query query = con.prepareQuery(QueryLanguage.SPARQL, getAuthorName(author.stringValue()));
            Name targetName = null;
            TupleQueryResult rsltOrign = ((TupleQuery) query).evaluate();
            if (rsltOrign.hasNext()) {
                BindingSet bs = rsltOrign.next();
                String fName = bs.getBinding("fname").getValue().stringValue();
                String lName = bs.getBinding("lname").getValue().stringValue();
                targetName = new Name(fName, lName);
            }

            // Get contributors and creators from author's publications.
            Set<Resource> targetUris = new HashSet();
            for (Statement publicationStm : Iterations.asList(con.getStatements(author, FOAF.PUBLICATIONS, null, true, graph))) {
                URI publication = vf.createURI(publicationStm.getObject().stringValue());
                if (targetName != null) {
                    targetUris.addAll(similarAuthorURI(con, author, publication, DCTERMS.CONTRIBUTOR, targetName));
                    targetUris.addAll(similarAuthorURI(con, author, publication, DCTERMS.CREATOR, targetName));
                }
            }

            // Change similar URIs with author URI.
            for (Resource t : targetUris) {
                Update queryReplace = con.prepareUpdate(QueryLanguage.SPARQL, replaceURIQuery());
                queryReplace.setBinding("old", t);
                queryReplace.setBinding("new", author);
                queryReplace.execute();
                System.out.println(String.format("Replaced %s for %s", t, author));
            }
        }
        con.commit();
        con.close();
    }

    private List<Resource> similarAuthorURI(RepositoryConnection con, Resource author, Resource publication, URI type, Name targetName) throws RepositoryException {
        List<Resource> similarURIs = new ArrayList<>();
        for (Statement creatorStm : Iterations.asList(con.getStatements(publication, type, null, true, graph))) {
            URI otherAuthor = (URI) creatorStm.getObject();
            if (!author.equals(otherAuthor) && targetName != null) {
                List<Statement> creators = Iterations.asList(con.getStatements(otherAuthor, FOAF.NAME, null, true, graph));
                creators.stream()
                        .filter(otherName -> (distance.getEqualNamesWithoutInjects(targetName.getFirstName(), targetName.getLastName(), otherName.getObject().stringValue())))
                        .map(statement -> statement.getSubject())
                        .forEach(similarUri -> similarURIs.add(similarUri));
            }
        }
        return similarURIs;
    }

    private String replaceURIQuery() {
        return "DELETE {\n"
                + "  GRAPH <http://ucuenca.edu.ec/wkhuska> {\n"
                + "    ?target ?p ?o.\n"
                + "    ?s ?prop ?target.\n"
                + "  }\n"
                + "}\n"
                + "INSERT {\n"
                + "  GRAPH <http://ucuenca.edu.ec/wkhuska> { \n"
                + "    ?replacement ?p ?o.\n"
                + "    ?s ?prop ?replacement.\n"
                + "  }\n"
                + "}\n"
                + "WHERE {\n"
                + "  BIND (?old as ?target)\n"
                + "  BIND (?new as  ?replacement)\n"
                + "  ?s ?prop ?target .\n"
                + "  ?target ?p ?o.\n"
                + "}";
    }

    private String getAuthors() {
        return "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "SELECT DISTINCT ?author \n"
                + "FROM <http://ucuenca.edu.ec/wkhuska>\n"
                + "WHERE {\n"
                + "  ?author foaf:publications []\n"
                + "}";
    }

    private String getAuthorName(String author) {
        return "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "SELECT DISTINCT *\n"
                + "FROM <http://ucuenca.edu.ec/wkhuska>\n"
                + "WHERE {\n"
                + "<" + author + "> foaf:givenName  ?fname;\n"
                + "                 foaf:familyName  ?lname.\n"
                + "}";
    }

    private class Name {

        private final String firstName;
        private final String lastName;

        public Name(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

    }
}

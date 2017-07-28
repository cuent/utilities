/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.common;

import info.aduna.iteration.Iterations;
import java.io.IOException;
import java.util.List;
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
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class LinkSubjects {

    private final DBPediaExtractor extractor = new DBPediaExtractor();
    private final KiWiDialect postgresDialet = new PostgreSQLDialect();
    private final URI graph = ValueFactoryImpl.getInstance().createURI("http://ucuenca.edu.ec/wkhuska");
    private final DistanceServiceImpl distance = new DistanceServiceImpl();
    private KiWiConfiguration conf;
    private KiWiStore store;
    private Repository repository;

    public LinkSubjects(String name, String jdbc, String username, String password) throws RepositoryException, MalformedQueryException, QueryEvaluationException, UpdateExecutionException, IOException, ClassNotFoundException {
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
        linking();
        repository.shutDown();
    }

    private void linking() throws RepositoryException, IOException {
        RepositoryConnection con = repository.getConnection();
        con.begin();
        ValueFactory vf = con.getValueFactory();
        RepositoryResult<Statement> topics = con.getStatements(null, FOAF.TOPIC_INTEREST, null, true, graph);
        topics.enableDuplicateFilter();
        while (topics.hasNext()) {
            Statement topic = topics.next();
            String topicStr = topic.getObject().stringValue();
            RepositoryResult<Statement> labels = con.getStatements(vf.createURI(topicStr), RDFS.LABEL, null, true, graph);
            labels.enableDuplicateFilter();
            while (labels.hasNext()) {
                Statement label = labels.next();
                List<String> same = null;
                String lbl = label.getObject().stringValue().toLowerCase();
                String lang = Service.getInstance().detectLanguage(lbl);
                if ("en".equals(lang)) {
                    same = extractor.extractEn(lbl);
                } else if ("es".equals(lang)) {
                    same = extractor.extractEs(lbl);
                }

                if (same != null) {
                    for (String url : same) {
                        URI dbpediaLink = vf.createURI(url);
                        Statement stament = vf.createStatement(label.getSubject(), OWL.SAMEAS, dbpediaLink, graph);
                        con.add(stament, graph);
                        System.out.println(String.format("Linking %s with %s", label.getSubject(), dbpediaLink));
                    }
                }
            }
        }
        con.commit();
        con.close();
    }

    public static void main(String[] args) throws RepositoryException, MalformedQueryException, QueryEvaluationException, UpdateExecutionException, IOException, ClassNotFoundException {
        new LinkSubjects("Test Linking", "jdbc:postgresql://localhost:5432/lmf?prepareThreshold=3", "marmotta", "marmotta");
    }
}

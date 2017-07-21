/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.testlda;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class PrintQueries {
    
    public static void main(String[] args) {
        String PREFIXES = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + " PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                + " PREFIX owl: <http://www.w3.org/2002/07/owl#> "
                + " PREFIX dct: <http://purl.org/dc/terms/> "
                + " PREFIX mm: <http://marmotta.apache.org/vocabulary/sparql-functions#> "
                + " PREFIX dcat: <http://www.w3.org/ns/dcat#> "
                + " PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX dc: <http://purl.org/dc/elements/1.1/> ";
        
        String graph = "http://190.15.141.66:8899/UTA/";
        
        System.out.println(PREFIXES
                + " SELECT (COUNT(DISTINCT ?s) as ?count) WHERE {"
                + " SELECT DISTINCT ?s WHERE {" + " GRAPH <" + graph + "> " + "{ "
                + " ?docu rdf:type bibo:Document ; "
                + "      ?c ?s ."
                + " ?s a foaf:Person."
                + " }}"
                + " GROUP BY ?s"
                + " HAVING (count(?docu)>1)}");
        
        System.out.println(PREFIXES
                + "SELECT ?s WHERE { GRAPH <" + graph + "> " + "{"
                + " ?doc rdf:type bibo:Document ;"
                + " ?c ?s ."
                + "?s a foaf:Person."
                + "} }"
                + " GROUP BY ?s"
                + " HAVING (count(?doc)>1)");

        
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

public class DBPediaExtractor {

    private final static String API_URL = "http://model.dbpedia-spotlight.org/";
    private static final double CONFIDENCE = 0.0;
    private static final int SUPPORT = 0;

    public List<String> extractEn(String text) {
        return extract(text, "en");
    }

    public List<String> extractEs(String text) {
        return extract(text, "es");
    }

    private List<String> extract(String text, String lang) {

        URIBuilder builder = null;
        List<String> urls = new LinkedList<>();
        try {
            builder = new URIBuilder(API_URL + lang + "/annotate");

            builder.setParameter("confidence", String.valueOf(CONFIDENCE));
            builder.setParameter("support", String.valueOf(SUPPORT));
            builder.setParameter("text", text);

            HttpGet get = new HttpGet(builder.build());
            get.addHeader("Accept", "application/json");

            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                String output = IOUtils.toString(response.getEntity().getContent());
                JSONObject result = new JSONObject(output);
                if (result.isNull("Resources")) {
                    return urls;
                }
                JSONArray resources = result.getJSONArray("Resources");

                for (int i = 0; i < resources.length(); i++) {
                    JSONObject entity = resources.getJSONObject(i);
                    System.out.println(entity.getString("@URI") + "\t\t"
                            + entity.getString("@similarityScore") + "\t\t"
                            + entity.getString("@surfaceForm"));
                    urls.add(entity.getString("@URI"));
                }
            }
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(DBPediaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return urls;
    }

}

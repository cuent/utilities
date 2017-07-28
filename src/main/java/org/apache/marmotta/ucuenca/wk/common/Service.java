/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.common;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Service {

    private static final Service instanceService = new Service();
    private final HttpClient httpClient = HttpClients.createDefault();

    private Service() {
    }

    public static Service getInstance() {
        return instanceService;
    }

    public String detectLanguage(String text) throws UnsupportedEncodingException, IOException {
        Preconditions.checkNotNull(text, "It is necessary some text to detect language");
        Preconditions.checkArgument(!text.equals(""));

        HttpPost post = new HttpPost("http://api.cortical.io/rest/text/detect_language");

        StringEntity textEntity = new StringEntity(text);

        post.setEntity(textEntity);
        post.addHeader("api-key", "1c556a80-8595-11e6-a057-97f4c970893c");
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addHeader("Cache-Control", "no-cache");
        post.addHeader("Accept", "application/json");
        return executeService(post, "iso_tag");
    }

    private String executeService(HttpUriRequest request, @Nullable String key) throws IOException {
        while (true) {
            try {
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity != null && response.getStatusLine().getStatusCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"))) {
                        String jsonResult = reader.readLine();
                        Object parser = new JsonParser().parse(jsonResult);
                        if (parser instanceof JsonObject) {
                            return ((JsonObject) parser).get(key).getAsString();
                        } else if (parser instanceof JsonArray) {
                            return ((JsonArray) parser).toString();
                        } else {
                            return "";
                        }
                    }
                }
            } catch (UnknownHostException e) {
                Logger.getLogger(Service.class.getName()).log(Level.SEVERE, "Can't reach host in service: Detect Language", e);
            }
        }
    }
}

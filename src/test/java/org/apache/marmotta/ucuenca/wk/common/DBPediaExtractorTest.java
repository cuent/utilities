/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.common;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class DBPediaExtractorTest {

    private final DBPediaExtractor extractor = new DBPediaExtractor();

    /**
     * Test of extractor method in ES and EN, of class DBPediaExtractor.
     */
    @Test
    public void testExtractor() {
        String textEn = "semantic web is an interesting topic for education in Ecuador";
        String textEs = "web semantica es un topico interesante para la educacion del ecuador";
        List<String> result = extractor.extractEn(textEn);
        Assert.assertTrue("There is no results", result.size() > 0);

        result = extractor.extractEs(textEs);
        Assert.assertTrue("There is no results", result.size() > 0);
    }
}

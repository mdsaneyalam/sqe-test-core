package com.softech.test.core.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class VelocityService {

    public static String getString(String templateName, Map<String, String> map) {
        return getString(templateName, new VelocityContext(map));
    }

    public static String getString(String templateName, VelocityContext vContext) {
        BufferedWriter bw = null;
        try {
            Velocity.init();// will not execute if already initialized
            Template template = Velocity.getTemplate(templateName);
            StringWriter sw = new StringWriter();
            bw = new BufferedWriter(sw);
            template.merge(vContext, bw);
            bw.flush();
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);  
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

}

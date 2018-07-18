package com.softech.test.core.omniture;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.softech.test.core.report.AllureAttachment;
import com.softech.test.core.util.VelocityService;

public class CallLoggerUtil {

    public static void logCallsToTable(String reportTableName, Map<String, String> expectedMap,
        Map<String, String> actualMap) {
        Velocity.setProperty("resource.loader", "classpath");
        Velocity.setProperty("classpath.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityContext context = new VelocityContext();
        context.put("expectedMap", expectedMap);
        context.put("actualMap", actualMap);

        Writer writer = null;
        try {
            File file = File.createTempFile("call_table", ".html");

            writer = createWriter(file); 
            writer.write(VelocityService.getString("templates/call_table.html.vm", context));
            writer.flush(); 

            AllureAttachment.attachHtmlFile(reportTableName, file);

            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } catch (Exception e) {
        } finally {
            try {
                writer.close(); 
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private static PrintWriter createWriter(File file) {
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create Writer");
        }

        return printWriter;
    }

}

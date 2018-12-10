package com.ids.rest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;

public class ServerConfig {
    public static String baseUrl;
    public static String packagePath;
    public static boolean parse(String xml)  {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new File(xml));
            Node server = doc.selectSingleNode("/rest/server");
            if (server != null) {
                Node nd = server.selectSingleNode("baseurl");
                baseUrl = (nd!=null) ? nd.getText() : null;
                nd = server.selectSingleNode("class");
                packagePath = (nd!=null) ? nd.getText() : null;
                if ((baseUrl!=null) || (packagePath!=null)) {
                    return true;
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return false;
    }
}

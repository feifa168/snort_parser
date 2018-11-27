package ids;

import com.sun.xml.internal.bind.v2.runtime.output.NamespaceContextImpl;
import org.dom4j.*;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class TestDom4j {
    @Test
    public void testWriteDom4j() {
        SAXReader reader = new SAXReader();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("root");
        Element author1 = root.addElement("author")
                .addAttribute("name", "james")
                .addAttribute("location", "UK")
                .addText("James Strachan");
        Element author2 = root.addElement("author")
                .addAttribute("name", "Bob")
                .addAttribute("location", "US")
                .addText("Bob McWhirter");

        Element author3 = root.addElement("test")
                .addAttribute("name", "Ft")
                .addAttribute("location", "CN")
                .addCDATA("<(?<pri>\\d+)>(?<time>\\w{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)?\\s*(?<host>\\w+)\\s+((?<tag>\\w+)(\\[(?<pid>\\w+)\\])?:)?\\s*(\\[(?<gid>\\w+):(?<sid>\\w+):(?<rid>\\w+)\\])?\\s*(\\\"(?<msg>.+)\\\"|(?<msg1>.+))(\\s+\\[\\w+:\\s+(?<priority>\\d+)\\]\\s+\\{(?<proto>\\w+)\\}\\s+(?<sip>\\d+\\.\\d+\\.\\d+\\.\\d+)(\\:(?<sport>\\d+))?\\s+(?<direction>->|<-)\\s+(?<dip>\\d+\\.\\d+\\.\\d+\\.\\d+)(\\:(?<dport>\\d+))?)?");
        XMLWriter writer = null;
        try {
            OutputFormat format = null;
            format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            writer = new XMLWriter(new FileWriter("test.xml"), format);
            writer.write(document);
            writer.close();

            format = OutputFormat.createCompactFormat();
            writer = new XMLWriter(System.out, format);
            writer.write(document);
            writer.close();

            format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            writer = new XMLWriter(System.out, format);
            writer.write(document);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadDom4j() {
        SAXReader reader = new SAXReader();
        try {
            File f = new File("test.xml");
            Document document = reader.read(f);
            Element root = document.getRootElement();
            System.out.println(document.getXMLEncoding());

            // iterate through child elements of root
            for (Iterator i = root.elementIterator(); i.hasNext(); ) {
                Element element = (Element) i.next();
                List<Attribute> attrs = element.attributes();
                System.out.println(element.getName() + "=" + element.getText());
                for (Attribute attr : attrs) {
                    System.out.println(attr.getName()+"="+attr.getValue());
                }
            }

            System.out.println("=====================");
            Iterator i2 = root.elementIterator( "author1" );
            if (i2.hasNext()) {
                Element e = (Element)i2.next();
            }
            // iterate through child elements of root with element name "foo"
            for ( Iterator i = root.elementIterator( "author" ); i.hasNext(); ) {
                Element element = (Element) i.next();
                List<Attribute> attrs = element.attributes();
                System.out.println(element.getName() + ":" + element.getStringValue());
                for (Attribute attr : attrs) {
                    System.out.println(attr.getName()+"="+attr.getValue());
                }
            }

            Node nd = document.selectSingleNode("/root/test[@name=\"Ft\"]");
            String reg = nd.getText();
            // iterate through attributes of root
            for ( Iterator i = root.attributeIterator(); i.hasNext(); ) {
                Attribute attribute = (Attribute) i.next();
                // do something
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testXpath() {
        SAXReader reader = new SAXReader();
        try {
            File f = new File("server.xml");
            Document doc = reader.read(f);
            Node server = doc.selectSingleNode("/rest/server");
            if (null != server) {
                List<Node> selectNodes = server.selectNodes("baseurl");
                for(Node node :selectNodes){
                    //System.out.println(node.asXML());
                    System.out.println(node.getName() + ", " + node.getText());
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

}

package dkpro.topic.utils;

import dkpro.topic.interpreter.rules.Result;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class XMLUtils {

    private static Logger jlog = LoggerFactory.getLogger(XMLUtils.class);
    private static final String default_encoding = "UTF-8";

    public static void parse(File f, DefaultHandler h)
            throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory sp = SAXParserFactory.newInstance();
        sp.setNamespaceAware(true);
        SAXParser p = sp.newSAXParser();
        p.parse(f, h);
    }

    public static void parse(Node xml, DefaultHandler h)
            throws SAXException {
        SAXWriter writer = new SAXWriter();
        writer.setContentHandler(h);
        writer.write(xml);
    }

    public static void parse(InputStream is, DefaultHandler h)
            throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory sp = SAXParserFactory.newInstance();
        sp.setNamespaceAware(true);
        SAXParser p = sp.newSAXParser();
        p.parse(is, h);
    }

    public static void dumpDocumentToFile(File file, Document doc) throws IOException {
        jlog.info("writing XML file {} with discourse information to directory",
                file.getAbsolutePath());
        String encode;
        OutputFormat outformat = OutputFormat.createPrettyPrint();

        if (doc.getXMLEncoding() == null)
            encode = default_encoding;
        else
            encode = doc.getXMLEncoding();

        outformat.setEncoding(encode);
        FileOutputStream ous = new FileOutputStream(file);
        Writer osw = new BufferedWriter(new OutputStreamWriter(ous, encode));

        XMLWriter writer = new XMLWriter(osw,
                outformat);
        writer.write(doc);
        writer.flush();
        writer.close();
    }

    // fix: StringBuffer is much faster, though not synchronized
    public static StringBuffer collapseWhitespace(StringBuffer sb) {
        boolean drop = true;

        int length = sb.length();
        int ipos = 0;
        for (int i = 0; i < length; i++) {
            char c = sb.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!drop) {
                    sb.setCharAt(ipos, ' ');
                    ipos++;
                    drop = true;
                }
            } else {
                drop = false;
                sb.setCharAt(ipos, c);
                ipos++;
            }
        }
        String pattern = "(\\w)(\\s+)([\\.,\\!\\?])";
        sb.setLength(ipos);
        return sb;
    }

    public static String[] ruleEnumeration(List<Result> results) {
        StringBuffer b = new StringBuffer();
        StringBuffer id = new StringBuffer();
        if (results == null || results.size() == 0)
            return null;
        for (Result r : results) {
            b.append(r.getRule().getDefinition().getTopicType() + ";");
            id.append(r.getRule().getName() + ";");
        }
        b.deleteCharAt(b.length() - 1);
        id.deleteCharAt(id.length() - 1);
        String[] list = new String[2];
        list[0] = id.toString();
        list[1] = b.toString();
        return list;
    }

    public static String splitSentenceIdentifier(String s) {
        return s.split(":")[0];
    }

    public static String splitSentence(String s) {
        return s.split(":")[1];
    }


    public static void close(Closeable o) {
        if (o == null) return;
        try {
            o.close();
        } catch (IOException e) {
        }
    }

}

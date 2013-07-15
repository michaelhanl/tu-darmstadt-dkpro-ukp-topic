package dkpro.topic.utils;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXWriter;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;

public final class XMLUtils {
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

    public static void dumpToFile(File f, Document doc)
            throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(f);
            String encoding = doc.getXMLEncoding();

            if (encoding == null) {
                encoding = "UTF-8";
            }

            XMLWriter writer = null;
            Writer osw = new BufferedWriter(new OutputStreamWriter(os, encoding));

            writer = new XMLWriter(osw, new OutputFormat("  ", false, encoding));

            writer.write(doc);
            writer.flush();
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                }
        }
    }

    public static StringBuilder collapseWhitespace(StringBuilder sb) {
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

    public static void close(Closeable o) {
        if (o == null) return;
        try {
            o.close();
        } catch (IOException e) {
        }
    }
}

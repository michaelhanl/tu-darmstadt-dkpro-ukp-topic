package dkpro.topic.annotator;

import dkpro.topic.utils.ConfigUtils;
import dkpro.topic.utils.Configuration;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;


public class TRAnnotator {
    private static final String outDirEx = "topics";
    public static final String outDirFinal = ConfigUtils.getOutputDir() + "/" +
            outDirEx;
    private static Logger jlog = LoggerFactory.getLogger(TRAnnotator.class);
    private DocResultsHolder topicResults;
    private File file;


    // TODO: statistics!
    public static void annotate(File parse, DocResultsHolder topics) {
        TRAnnotator annotator = new TRAnnotator(parse, topics);
        annotator.process();
    }

    private TRAnnotator(File parse, DocResultsHolder topics) {
        this.topicResults = topics;
        this.file = parse;
    }


    public void process() {
        jlog.info("running XML annotator");
        try {
            processNodes();
        } catch (DocumentException e) {
            jlog.error("document not readable", e);
        }
    }


    private void processNodes() throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        Element root = document.getRootElement();
        jlog.info("running the annotator for file {}", file);
        for (Iterator<Element> i = root.elementIterator(Configuration
                .getElementSentence()); i.hasNext(); ) {
            Element sentence = (Element) i.next();
            Attribute sentID = sentence.attribute("id");
            if (topicResults.getTopicRule(sentID.getValue()) == null) {
                jlog.info("no topics available. Annotator cancelled!");
                return;
            } else {
                sentence.addAttribute(Configuration.getAttrTopicRule(),
                        topicResults.getTopicRule(sentID.getValue()));
                sentence.addAttribute(Configuration.getAttrTopicLabel(),
                        topicResults.getTopicLabel(sentID.getValue()));
                dumpDocumentToFile(document);
            }
        }

    }


    private void dumpDocumentToFile(Document doc) {

        jlog.info("writing XML file {} with annotation to directory {}",
                file.getName(), outDirFinal);
        OutputFormat outformat = OutputFormat.createPrettyPrint();
        outformat.setEncoding("UTF-8");
        File out = new File(outDirFinal, file.getName());

        try {
            XMLWriter writer = new XMLWriter(new FileOutputStream(out),
                    outformat);
            writer.write(doc);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

package dkpro.topic.annotator;

import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.utils.Configuration;
import dkpro.topic.utils.NamingParameters;
import dkpro.topic.utils.XMLUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TreeAnnotator {
    public static final String outDirEx = "topics";
    private Logger jlog = LoggerFactory.getLogger(TreeAnnotator.class);
    private Map<String, List<Result>> results;
    private File file;

    public static TreeAnnotator instantiate(File parse, Map<String, List<Result>> results) {
        return new TreeAnnotator(parse, results);
    }

    private TreeAnnotator(File parse, Map<String, List<Result>> results) {
        this.results = results;
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

        for (Iterator<Element> i = root.elementIterator(NamingParameters
                .getElementSentence()); i.hasNext(); ) {
            Element sentence = i.next();

            Attribute sentID = sentence.attribute(NamingParameters.getAttrSentenceID());
            String[] f = XMLUtils.ruleEnumeration(getResults(sentID.getValue()));
            String ruleIds = f[0];
            String ruleLabels = f[1];

            if (getResults(sentID.getValue()) == null) {
                jlog.info("no topics available for sentence with ID {}!", sentID.getValue());
                continue;
            } else {
                sentence.addAttribute(NamingParameters.getAttrTopicRule(), ruleIds);
                //sentence.addAttribute(NamingParameters.getAttrTopicLabel(), ruleLabels);
                try {
                    File dir = new File(Configuration.getOutputDir(), outDirEx);
                    dir.mkdir();
                    XMLUtils.dumpDocumentToFile(new File(dir, this.file.getName()), document);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Result> getResults(String id) {
        for (String key : results.keySet()) {
            if (key.startsWith(id))
                return results.get(key);
        }
        return null;
    }

}

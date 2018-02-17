package dkpro.topic.annotator;

import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.utils.ConfigParameters;
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

/**
 *
 *
 * @author micha.hanl@gmail.com
 * @date 11/6/13
 */
public class TreeAnnotator {


    public static final String OUTDIREX = "topics";
    private static Logger jlog = LoggerFactory.getLogger(TreeAnnotator.class);
    private Map<String, List<Result>> results;
    private File file;


    public TreeAnnotator(File parse, Map<String, List<Result>> results) {
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
        jlog.debug("running the annotator for file {}", file);

        String sent = ConfigParameters.Instances.getNamingParameters()
                .getElementSentence();
        for (Iterator<Element> i = root.elementIterator(sent); i.hasNext(); ) {
            Element sentence = i.next();

            Attribute sentID = sentence.attribute(ConfigParameters.Instances.getNamingParameters().getAttrSentenceID());
            if (getResults(sentID.getValue()) == null) {
                jlog.warn("no topics available for sentence with ID '{}'", sentID.getValue());

            } else {
                String[] f = XMLUtils.ruleEnumeration(getResults(sentID.getValue()));
                String ruleIds = f[0];
                // fixme: usage?
                String ruleLabels = f[1];

                sentence.addAttribute(ConfigParameters.Instances.getNamingParameters().getAttrTopicRule(), ruleIds);
                //sentence.addAttribute(NamingParameters.getAttrTopicLabel(), ruleLabels);
                try {
                    File dir = new File(ConfigParameters.Instances.getConfiguration().getOutputDir(), OUTDIREX);
                    dir.mkdir();
                    XMLUtils.dumpDocumentToFile(new File(dir, this.file.getName()), document);
                } catch (IOException e) {
                    throw new DocumentException(e);
                }
            }
        }
    }

    private List<Result> getResults(String id) {
        for (String key : results.keySet()) {
            if (key.startsWith(id) && !results.get(key).isEmpty()) {
                return results.get(key);
            }
        }
        return null;
    }

}

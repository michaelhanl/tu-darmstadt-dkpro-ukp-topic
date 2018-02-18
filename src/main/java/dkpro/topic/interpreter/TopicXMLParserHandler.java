package dkpro.topic.interpreter;

import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.interpreter.rules.RuleBook;
import dkpro.topic.utils.OutputWriter;
import dkpro.topic.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * class was copied and modified from the tree-rule-processor-engine by R.
 * Eckart de Castilho
 *
 * @author eckart@ukp.informatik.tu-darmstadt.de, micha.hanl@gmail.com
 * @date 11/6/13
 */

public class TopicXMLParserHandler {
    private static Logger jlog = LoggerFactory.getLogger(TopicXMLParserHandler.class);
    private TopicSentInterpreter interpreter;
    private OutputWriter out;

    public TopicXMLParserHandler() {
        out = new OutputWriter(System.out);
    }


    public void process(File ruleFile, File xmlFile)
            throws ParserConfigurationException, SAXException, IOException {
        RuleBook rules = new RuleBook();
        rules.read(ruleFile);
        jlog.info("interpreting file {}", xmlFile);
        this.interpreter = new TopicSentInterpreter(rules);
        this.apply(xmlFile, rules);
    }

    private void apply(File xmlFile, RuleBook rules) throws ParserConfigurationException,
            SAXException, IOException {
        jlog.debug("--- Now we will try to match the rules");
        SAXParser parseXML = new SAXParser(this.interpreter);
        XMLUtils.parse(xmlFile, parseXML);

        out.writeOutResults(this.interpreter.getSentenceResults());
        out.dumpStats(this.interpreter.getStats());
        out.writeRuleBuild(rules);
    }

    public Map<String, List<Result>> getSentenceResults() {
        return this.interpreter.getSentenceResults();
    }

}

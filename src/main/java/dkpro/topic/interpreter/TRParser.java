package dkpro.topic.interpreter;

import dkpro.topic.annotator.DocResultsHolder;
import dkpro.topic.annotator.Statistics;
import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.interpreter.rules.RuleBook;
import dkpro.topic.interpreter.rules.RuleDefinition;
import dkpro.topic.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * class was copied and modified from the tree-rule-processor-engine by R.
 * Eckart de Castilho
 *
 * @author hanl
 */
public class TRParser {
    private Logger jlog = LoggerFactory.getLogger(TRParser.class);
    private Logger stats = LoggerFactory.getLogger(Statistics.statLogger);

    private TopicInterpreter interpreter;
    private DocResultsHolder results;

    private TRParser() {
    }

    public static TRParser runner(File ruleFile, File xmlFile)
            throws ParserConfigurationException, SAXException, IOException {
        TRParser wrapper = new TRParser();
        wrapper.run(ruleFile, xmlFile);
        return wrapper;
    }

    private void run(File ruleFile, File xmlFile)
            throws ParserConfigurationException, SAXException, IOException {
        RuleBook rules = new RuleBook();
        rules.read(ruleFile);
        jlog.info("interpreting file {}", xmlFile);
        this.interpreter = new TopicInterpreter(rules);
        this.results = DocResultsHolder.createResultMap();
        this.apply(xmlFile, rules);
    }

    private void apply(File xmlFile, RuleBook rules) throws ParserConfigurationException,
            SAXException, IOException {

        Map<RuleDefinition, Collection<RuleDefinition>> ruleMap = this
                .generateRules(rules);

        jlog.debug("--- Now we will try to match the rules");
        SAXParser parseXML = new SAXParser(interpreter, results);
        XMLUtils.parse(xmlFile, parseXML);
        System.out.println("sentence results " + interpreter.getSentenceResults());
        System.out.println("all rules matched "+ interpreter.getAllRulesMatched());
        System.out.println("ruleDef " + rules.toString());
        this.writeOutResults(interpreter.getSentenceResults());
        interpreter.getStats().dumpStats(System.out);

        /**
        stats.info("\n--- Rule and rules superseding them");
        for (RuleDefinition r1 : ruleMap.keySet()) {

            //TODO: do better formatting!
            stats.info(r1.getTopicType() + ": [");
            for (RuleDefinition r2 : ruleMap.get(r1)) {
                stats.info("[" + r2.getTopicType() + "] ");
            }
            stats.info("]");
        }
         */
//		 Statistics.dumpResultsToConsole(results);

    }

    private Map<RuleDefinition, Collection<RuleDefinition>> generateRules(
            RuleBook ruleBook) {
        jlog.debug("--- We are going through the rules determining how they relate");
        Map<RuleDefinition, Collection<RuleDefinition>> ruleGen = new LinkedHashMap<>();

        for (RuleDefinition r1 : ruleBook) {

            Collection<RuleDefinition> supersededBy = new ArrayList<>();

            for (RuleDefinition r2 : ruleBook) {
                if ((r1 != r2) && (r1.matches(r2))) {
                    supersededBy.add(r2);
                }
            }
            ruleGen.put(r1, supersededBy);
        }

        return ruleGen;
    }

    private void writeOutResults(Map<String, List<Result>> sentenceResults) {
        int numberOfSentences = 0;
        int sentencesWithoutResults = 0;

        stats.info("--- Results");
        for (Map.Entry<String, List<Result>> e : sentenceResults.entrySet()) {
            numberOfSentences++;

            boolean fullSentenceFound = false;
            stats.info("Sentence: [" + (String) e.getKey() + "]");
            for (Result r : e.getValue()) {
                if ((!r.isRemoved())
                        && (((String) e.getKey()).equals(r.getSentence()))) {
                    fullSentenceFound = true;
                }
                stats.info("   " + r);
            }

            if (!fullSentenceFound) {
                sentencesWithoutResults++;
                stats.info(" ! No rule matched the whole sentence !");
            }
        }

        stats.info("Sentences without results : "
                + sentencesWithoutResults);
        stats.info("Classified sentences % \t  : "
                + (100.0D - sentencesWithoutResults / numberOfSentences
                * 100.0D));
    }

    public DocResultsHolder getDocResults() {
        return results;
    }

}

package dkpro.topic.utils;

import dkpro.topic.annotator.Statistics;
import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.interpreter.rules.RuleBook;
import dkpro.topic.interpreter.rules.RuleDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.*;

/**
 * User: hanl
 * Date: 9/22/13
 * Time: 7:42 PM
 */
public class OutputWriter {

    private Logger stats = LoggerFactory.getLogger(Statistics.statLogger);
    private PrintStream out;

    public OutputWriter(PrintStream out) {
        this.out = out;
    }


    public void writeOutResults(Map<String, List<Result>> sentenceResults) {
        int numberOfSentences = 0;
        int sentencesWithoutResults = 0;

        out.println("--- Results");
        for (Map.Entry<String, List<Result>> e : sentenceResults.entrySet()) {
            numberOfSentences++;

            boolean fullSentenceFound = false;
            String sent = XMLUtils.splitSentence(e.getKey());
            String id = XMLUtils.splitSentenceIdentifier(e.getKey());

            out.println("Sentence: [" + e.getKey() + "]");
            for (Result r : e.getValue()) {
                if (!r.isRemoved()
                        && sent.equals(r.getSentence()))
                    fullSentenceFound = true;
                out.println("   " + r);
            }

            if (!fullSentenceFound) {
                sentencesWithoutResults++;
                out.println(" ! No rule matched the whole sentence !");
            }
        }

        out.println("Sentences without results : "
                + sentencesWithoutResults);
        out.println("Classified sentences % \t  : "
                + (100.0D - sentencesWithoutResults / numberOfSentences
                * 100.0D));
    }


    public void dumpStats(StatisticsContainer stats) {
        out.format("====== STATISTICS ======%n", new Object[0]);
        out.format("%n===== Overall statistics =====%n", new Object[0]);
        out.format("| Sentences found                        | %7d |%n", new Object[]{Integer.valueOf(stats._sentences)});
        out.format("| Avg matches per sentence               | %7.2f |%n", new Object[]{Double.valueOf(stats._totalMatches / stats._sentences)});
        out.format("| Total matches                          | %7d |%n", new Object[]{Integer.valueOf(stats._totalMatches)});
        out.format("| Total matches MET             (true +) | %7d |%n", new Object[]{Integer.valueOf(stats._totalMet)});
        out.format("| Total matches UNEXPECTED     (false +) | %7d |%n", new Object[]{Integer.valueOf(stats._totalUnexpectedMatches)});
        out.format("| Total matches MISMATCHed     (false +) | %7d |%n", new Object[]{Integer.valueOf(stats._totalMismatches)});
        out.format("| Overall recall                         | %6.2f%% |%n", new Object[]{Double.valueOf(stats.getRecall() * 100.0D)});
        out.format("| Overall precision    (with unexpected) | %6.2f%% |%n", new Object[]{Double.valueOf(stats.getPrecision(true) * 100.0D)});
        out.format("| Overall precision (without unexpected) | %6.2f%% |%n", new Object[]{Double.valueOf(stats.getPrecision(false) * 100.0D)});
        out.format("| Overall f-score      (with unexpected) | %7.4f |%n", new Object[]{Double.valueOf(stats.getFScore(true, 1.0D))});
        out.format("| Overall f-score   (without unexpected) | %7.4f |%n", new Object[]{Double.valueOf(stats.getFScore(false, 1.0D))});

        out.format("%n===== Detail statistics =====%n", new Object[]{Integer.valueOf(stats._sentences)});
        out.format("%-8s%-30s%-4s%-4s%-4s%-4s %-4s %-7s %-7s %-7s %-7s %-7s%n", new Object[]{"RuleInstance", "Label", "Exp", "Met", "UMet", "MisM", "UExp", "Rec", "Prec+", "Prec-", "FSco+", "FSco-"});

        List<RuleDefinition> rdefs = new ArrayList(stats._stats.keySet());

        Collections.sort(rdefs, new Comparator<RuleDefinition>() {
            public int compare(RuleDefinition o1, RuleDefinition o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (RuleDefinition def : rdefs)
            out.println(stats._stats.get(def));
    }

    public void writeRuleBuild(RuleBook rules) {
        Map<RuleDefinition, Collection<RuleDefinition>> ruleMap = rules.generateRules();
        out.println("\n--- Rule and rules superseding them");
        for (RuleDefinition r1 : ruleMap.keySet()) {

            //TODO: do better formatting!
            out.print(r1.getTopicType() + ": [");
            for (RuleDefinition r2 : ruleMap.get(r1)) {
                out.print("[" + r2.getTopicType() + "] ");
            }
            out.println("]");
        }

    }

}

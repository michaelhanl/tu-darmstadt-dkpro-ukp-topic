package dkpro.topic.annotator;

import dkpro.topic.annotator.DocResultsHolder.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create statistics for document: how many sentence have been parsed, how many
 * rules applied, what rules have been matched --> write custom learner to
 * calculate precision and recall values!
 *
 * @author hanl
 */
public class Statistics {
    public static final String statLogger = "statLogger";
    private static Logger stats = LoggerFactory.getLogger(Statistics.statLogger);

    public static void dumpResultsToConsole(DocResultsHolder results) {
        for (String id : results.getIDs()) {
            stats.info("sentence id: " + id);
            for (Topic t : results.getEntries(id)) {
                stats.info("topics for sentence " + t.getRuleName());
            }

        }

    }

}

package dkpro.topic.annotator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author hanl@ids-mannheim.de
 */
@Deprecated
public class DocResultsHolder {
    private Logger jlog = LoggerFactory.getLogger(DocResultsHolder.class);
    // allows multiple instances of the same rule type to be stored for every
    // sentence. shall I allow this?!
    private ListMultimap<String, Topic> results;


    private DocResultsHolder() {
        results = ArrayListMultimap.create();
    }


    public static DocResultsHolder createResultMap() {
        return new DocResultsHolder();
    }


    public List<Topic> getEntries(String id) {
        return results.get(id);
    }


    public Collection<String> getIDs() {
        return results.keySet();
    }


    public void add(String sentenceID, String rule, String label) {
        Topic t = new Topic(rule, label);
        results.put(sentenceID, t);
    }


    public void addAll(String sentenceID, List<Topic> list) {
        results.putAll(sentenceID, list);
    }


    public void add(String sentenceID, Topic topic) {
        results.put(sentenceID, topic);
    }


    @Deprecated
    public String getTopicLabel(String id) {
        if (results.size() > 0) {
            List<Topic> s = results.get(id);
            if (s.size() > 1) {
                jlog.info("multiple topic values!");
                for (Topic t : s) {
                    System.out.println("rule: " + t.getRuleName());
                    System.out.println("label: " + t.getRuleLabel());
                }
                return new String();
            } else {
                Topic values = s.get(0);
                return values.getRuleLabel();
            }
        } else {
            jlog.warn("no rules matched. Topics cannot be retrieved");
            return null;
        }

    }


    // multiple topics are supported in general, but currently only the first
    // element is returned
    // TODO: hierarchy of returns --> superseded values?!
    @Deprecated
    public String getTopicRule(String id) {
        if (results.size() > 0) {
            List<Topic> s = results.get(id);
            if (s.size() > 1) {
                jlog.info("multiple topic values!");
                for (Topic t : s) {
                    System.out.println("rule: " + t.getRuleName());
                    System.out.println("label: " + t.getRuleLabel());
                }
                return new String();
            } else {
                Topic values = s.get(0);
                return values.getRuleName();
            }
        } else {
            jlog.warn("no rules matched. Topics cannot be retrieved");
            return null;
        }
    }

    public static class Topic {
        private String ruleName;
        private String ruleLabel;


        public Topic(String name, String label) {
            this.ruleName = name;
            this.ruleLabel = label;
        }


        public String getRuleName() {
            return ruleName;
        }


        public String getRuleLabel() {
            return ruleLabel;
        }

    }

}

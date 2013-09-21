package dkpro.topic.interpreter;

import dkpro.topic.annotator.DocResultsHolder.Topic;
import dkpro.topic.interpreter.data.Constituent;
import dkpro.topic.interpreter.data.MatchListener;
import dkpro.topic.utils.Statistics;
import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.interpreter.rules.RuleBook;
import dkpro.topic.interpreter.rules.RuleDefinition;
import dkpro.topic.interpreter.rules.RuleInstance;
import dkpro.topic.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;


/**
 * wrapper around topic interpreter class in the annolab project, written by R.
 * Eckart de Castilho
 * I dont get the sentence StringBuilder object, because it is used within the original theme interpreter class only
 *
 * @author hanl@ids-mannheim.de
 */
public class TopicInterpreter {

    private static final Log _log = LogFactory
            .getLog(TopicInterpreter.class);
    private static final int SENTENCE_DEPTH = 1;
    private final RuleBook _ruleBook;
    private final List<RuleInstance> _activeRules;
    private final Map<String, List<Result>> _sentenceResults;
    private final StringBuilder _sentence;
    private final Statistics _stats = new Statistics();
    private int _depth = 0;
    private List<Result> _results;
    private List<RuleInstance> _allRulesMatched = new ArrayList<>();
    private List<RuleInstance> _rulesMatched = null;
    @Deprecated
    private ArrayList<Topic> sentenceResult = new ArrayList<>();
    private boolean _filterGeneralRules = true;
    private List<MatchListener> _matchListeners = new ArrayList<>();
    //private Map<MatchListener, Object> _matchListeners = new WeakHashMap<>();


    public TopicInterpreter(RuleBook rules) {
        this._ruleBook = rules;
        this._activeRules = new ArrayList<>();
        this._sentenceResults = new LinkedHashMap<>();
        this._sentence = new StringBuilder();
    }

    public static Result.Expectation getExpectation(Constituent constituent,
                                                    RuleInstance rule) {

        if (constituent.getExpected() == null)
            return Result.Expectation.UNEXPECTED;

        /**
         * fixed a bug, where the String getExpected was compared with the getDefinition object (RuleDefinition)
         */
        return ((constituent.getExpected().equals(rule.getDefinition().toString())) ? Result.Expectation.MET
                : Result.Expectation.MISMATCH);
    }

    public void addMatchListener(MatchListener l) {
        this._matchListeners.add(l);
    }

    public void removeMatchListener(MatchListener l) {
        this._matchListeners.remove(l);
    }

    protected void fireMatch(RuleInstance r, Constituent c) {
        for (MatchListener l : this._matchListeners)
            l.match(r, c);
    }

    public RuleBook getRuleBook() {
        return this._ruleBook;
    }

    public void setFilterGeneralRules(boolean b) {
        this._filterGeneralRules = b;
    }

    public void startElement(Constituent node) {
        this._depth += 1;

        _log.debug("--- " + this._depth + " START " + node);

        this._ruleBook.getRules(this._depth, this._activeRules, node);
        _log.debug("Active rules: " + this._activeRules.size());

        this._results = new ArrayList<>();
        if (this._depth == 2) {
            //this._sentence.setLength(0);
            //this._results = new ArrayList<>();
        }

        Iterator i = this._activeRules.iterator();
        while (i.hasNext()) {
            RuleInstance r = (RuleInstance) i.next();

            boolean remove = r.startElement(this._depth, node);
            if (remove) {
                i.remove();
                _log.debug("RuleInstance is no longer a candidate: rule did not get what it expected (" +
                        this._activeRules.size() + "): " + r);
            }
        }
    }

    public void endElement(Constituent node) {
        _log.debug("--- " + this._depth + " END   " + node);

        sentenceResult = new ArrayList<>();

        this._depth -= 1;

        boolean expectationMet = false;
        this._rulesMatched = new ArrayList<>();

        Set<RuleInstance> toRemove = new HashSet<>();
        Iterator i1;
        RuleInstance r1;
        Iterator i2;
        if (this._filterGeneralRules)
            for (i1 = this._activeRules.iterator(); i1.hasNext(); ) {
                r1 = (RuleInstance) i1.next();

                if (!(r1.expectsMore()))
                    for (i2 = this._activeRules.iterator(); i2.hasNext(); ) {
                        RuleInstance r2 = (RuleInstance) i2.next();

                        if ((!(r2.expectsMore())) &&
                                (r1 != r2) &&
                                (r1.getDefinition().matches(r2.getDefinition())))
                            toRemove.add(r1);
                    }
            }
        Iterator i = this._activeRules.iterator();
        while (i.hasNext()) {
            RuleInstance r = (RuleInstance) i.next();

            boolean remove = r.endElement(this._depth + 1, node);
            if (remove) {
                i.remove();
                _log.debug("RuleInstance is no longer a candidate [2] (" +
                        this._activeRules.size() + "): " + r);
            } else if (r.getCreationDepth() > this._depth) {
                i.remove();

                if (!r.expectsMore()) {
                    String s = "RuleInstance matches: " + r + " -> " +
                            r.getDefinition().getTopicType();


                    _log.debug("RuleInstance matches: " + r + " -> " +
                            r.getDefinition().getTopicType());
                    Result.Expectation e = getExpectation(node, r);

                    if (e == Result.Expectation.MET) {
                        expectationMet = true;
                    }

                    boolean removed = toRemove.contains(r);
                    if (!removed) {
                        fireMatch(r, node);
                        this._rulesMatched.add(r);
                        this._allRulesMatched.add(r);
                        this._stats.tallyMatch(r, e);
                    }

                    this._results.add(new Result(r, e, getExpectedRule(node),
                            removed));

                    // my own implementation to extract topic label and rule
                    Topic t = new Topic(r.getDefinition().getName(), r
                            .getDefinition().getTopicType());
                    sentenceResult.add(t);

                } else {
                    _log.debug("RuleInstance left scope (" +
                            this._activeRules.size() + "): " + r);
                }

            }

        }

        RuleDefinition expected = getExpectedRule(node);
        if (expected != null) {
            this._stats.tallyExpectation(expected);
            if (!(expectationMet)) {
                this._stats.tallyUnfulfilledExpectation(getExpectedRule(node));
            }

        }

        if (this._depth == 1) {
            this._sentenceResults.put(XMLUtils.collapseWhitespace(this._sentence)
                    .toString(), this._results);

            this._stats.tallySentence();
        }

        this._rulesMatched = null;
    }


    public void chars(Constituent node, String value)
    {
        if (node.getParent() != null && node.getParent().equals("ROOT"))
            _sentence.setLength(0);
        _sentence.append(value);
    }



    public List<RuleInstance> getRulesMatched() {
        return this._rulesMatched;
    }

    public String getSentence() {
        return XMLUtils.collapseWhitespace(this._sentence).toString();
    }

    public Statistics getStats() {
        return this._stats;
    }

    public Map<String, List<Result>> getSentenceResults() {
        return this._sentenceResults;
    }

    public List<RuleInstance> getAllRulesMatched() {
        return this._allRulesMatched;
    }

    private RuleDefinition getExpectedRule(Constituent constituent) {
        return this._ruleBook.getDefinitionByName(constituent.getExpected());
    }


    public List<Topic> getSentenceTopics() {
        return sentenceResult;
    }

}

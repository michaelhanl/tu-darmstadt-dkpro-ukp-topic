package dkpro.topic.interpreter;

import dkpro.topic.interpreter.data.Constituent;
import dkpro.topic.interpreter.data.XMLConstituent;
import dkpro.topic.interpreter.rules.Result;
import dkpro.topic.interpreter.rules.RuleBook;
import dkpro.topic.interpreter.rules.RuleDefinition;
import dkpro.topic.interpreter.rules.RuleInstance;
import dkpro.topic.utils.StatisticsContainer;
import dkpro.topic.utils.XMLUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.util.*;


/**
 * wrapper around topic interpreter class in the annolab project, written by R.
 * Eckart de Castilho
 *
 * @author eckart@ukp.informatik.tu-darmstadt.de, micha.hanl@gmail.com
 * @date 11/6/13
 *
 */
public class TopicSentInterpreter {

    private static final Logger _log = LoggerFactory
            .getLogger(TopicSentInterpreter.class);
    private static final int SENTENCE_DEPTH = 2;
    private final RuleBook _ruleBook;
    private final List<RuleInstance> _activeRules;
    private final Map<String, List<Result>> _sentenceResults;
    private final StringBuffer _sentence;
    private final StatisticsContainer _stats = new StatisticsContainer();
    private int _depth = 0;
    private List<Result> _results;
    private List<RuleInstance> _allRulesMatched = new ArrayList<>();
    private List<RuleInstance> _rulesMatched = new ArrayList<>();
    private boolean _filterGeneralRules = true;
    private StringBuffer _prevSentence;


    public TopicSentInterpreter(RuleBook rules) {
        this._ruleBook = rules;
        this._activeRules = new ArrayList<>();
        this._sentenceResults = new LinkedHashMap<>();
        this._sentence = new StringBuffer();
        this._prevSentence = new StringBuffer();
    }

    public void setFilterGeneralRules(boolean b) {
        this._filterGeneralRules = b;
    }

    public void startElement(Constituent node) {
        _log.debug("--- " + this._depth + " START " + node + " ---");
        this._depth += 1;
        this._ruleBook.getRules(this._depth, this._activeRules, node);
        _log.debug("Active rules: {}", this._activeRules.size());


        /**
         * required to separate sentence within the results
         */
        if (this._depth == SENTENCE_DEPTH) {
            this._sentence.setLength(0);
            this._results = new ArrayList<>();
        }

        Iterator i = this._activeRules.iterator();
        while (i.hasNext()) {
            RuleInstance r = (RuleInstance) i.next();

            boolean remove = r.startElement(this._depth, node);
            if (remove) {
                i.remove();
                _log.debug("RuleInstance is no longer a candidate: rule did not get what it expected ({}): {}",
                        this._activeRules.size(), r);
            }
        }
    }

    public void endElement(Constituent node) {
        _log.debug("--- {} END  {}",this._depth, node);
        _log.debug("--------- INTERPRETER @END ------------");
        XMLConstituent c = (XMLConstituent) node;
        this._depth -= 1;

        boolean expectationMet = false;
        this._rulesMatched.clear();

        Set<RuleInstance> toRemove = new HashSet<>();
        Iterator i1;
        RuleInstance r1;
        Iterator i2;
        if (this._filterGeneralRules)
            for (i1 = this._activeRules.iterator(); i1.hasNext(); ) {
                r1 = (RuleInstance) i1.next();

                if (!r1.expectsMore())
                    for (i2 = this._activeRules.iterator(); i2.hasNext(); ) {
                        RuleInstance r2 = (RuleInstance) i2.next();

                        if (!r2.expectsMore() &&
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
                _log.debug("RuleInstance is no longer a candidate [2] ({}): {}",
                        this._activeRules.size(), r);
            } else if (r.getCreationDepth() > this._depth) {
                i.remove();

                if (!r.expectsMore()) {
                    _log.debug("RuleInstance matches: {} -> {}",r.toString(),
                            r.getDefinition().getTopicType());
                    _log.debug("Matching node: {}", node.toString());
                    Result.Expectation e = r.getExpectation(node);

                    if (e == Result.Expectation.MET) {
                        expectationMet = true;
                    }

                    boolean removed = toRemove.contains(r);
                    if (!removed) {
                        r.addTextMatch(XMLUtils.collapseWhitespace(_sentence).toString());
                        this._rulesMatched.add(r);
                        this._allRulesMatched.add(r);
                        this._stats.tallyMatch(r, e);

                    }
                    this._results.add(new Result(r, e, getExpectedRule(node),
                            removed));
                } else {
                    _log.debug("RuleInstance left scope ({}): {}",
                            this._activeRules.size(), r);
                }

            }

        }

        /**
         * Due to an error/bug in the matching of the rule Identifier (getNodeExpectation expected format [rule 1]
         * as the value for the "expect" attribute, whereas getExpectedRule expected the name to be without brackets,
         * e.g. "rule 1"
         * if expected == null, no expect attribute found in XML
         *
         */
        RuleDefinition expected = getExpectedRule(node);
        if (expected != null) {
            this._stats.tallyExpectation(expected);
            if (!(expectationMet))
                this._stats.tallyUnfulfilledExpectation(getExpectedRule(node));
        }

        if (this._depth == 1) {
            this._sentenceResults.put(c.getSentenceID() + ":" + XMLUtils.collapseWhitespace(this._sentence).toString(),
                    this._results);
            this._stats.tallySentence();
        }
        //this._rulesMatched = null;
    }

    public void chars(Constituent node, String value) {
        _sentence.append(value);
    }

    public List<RuleInstance> getRulesMatched() {
        return this._rulesMatched;
    }

    public String getSentence() {
        return XMLUtils.collapseWhitespace(this._sentence).toString();
    }

    public StatisticsContainer getStats() {
        return this._stats;
    }

    public Map<String, List<Result>> getSentenceResults() {
        return this._sentenceResults;
    }

    public List<RuleInstance> getAllRulesMatched() {
        return this._allRulesMatched;
    }

    private RuleDefinition getExpectedRule(Constituent constituent) {
        return this._ruleBook.getDefinitionByName(constituent.getNodeExpectation());
    }
}

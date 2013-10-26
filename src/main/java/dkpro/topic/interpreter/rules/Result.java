package dkpro.topic.interpreter.rules;

import dkpro.topic.utils.XMLUtils;

public class Result {
    private final RuleInstance _rule;
    private final Expectation _expectation;
    private final RuleDefinition _expectedRule;
    private final boolean _removed;

    public Result(RuleInstance rule, Expectation expectation, RuleDefinition expectedRule,
                  boolean removed) {
        this._rule = rule;
        this._expectation = expectation;
        this._expectedRule = expectedRule;
        this._removed = removed;
    }

    public String getSentence() {
        return XMLUtils.collapseWhitespace(this._rule.getTextMatch()).toString();
    }

    public RuleInstance getRule() {
        return _rule;
    }

    public Expectation getExpectation() {
        return this._expectation;
    }

    public boolean isRemoved() {
        return this._removed;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (this._removed)
            sb.append("[Ghost ");
        else {
            sb.append("[Result");
        }
        sb.append(" [");
        sb.append(getSentence());
        sb.append("] = [");
        sb.append(this._rule.getDefinition().getTopicType());
        sb.append("]");
        sb.append(" " + this._expectation);
        if (this._expectation == Expectation.MISMATCH) {
            sb.append(" expected [" + this._expectedRule.getTopicType() + "]");
        }
        if (this._removed) {
            sb.append("|SUPERSEDED");
        }
        sb.append("]");
        return sb.toString();
    }

    public static enum Expectation {
        MET,
        MISMATCH,
        UNEXPECTED;
    }
}


package dkpro.topic.interpreter.rules;

import dkpro.topic.interpreter.data.Constituent;
import dkpro.topic.utils.Configuration;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleInstance {
    private final RuleDefinition _ruleDefinition;
    private final RuleDefinition.RelaxStack _relaxStack;
    private final int _instance_id;
    Logger jlog = LoggerFactory.getLogger(RuleInstance.class);
    private Element _cursor;
    private int _createdAtDepth;
    private StringBuffer textMatch;

    //FIXME: add sentence result to this container and return list as replacement for DocHolderContainer

    public RuleInstance(RuleDefinition ruleDefinition, int createdAtDepth) {
        this._ruleDefinition = ruleDefinition;
        this._instance_id = RuleDefinition._next_instance_id;
        RuleDefinition._next_instance_id += 1;
        this._createdAtDepth = createdAtDepth;
        this._relaxStack = new RuleDefinition.RelaxStack();
        this._cursor = this._ruleDefinition._structure.getRootElement();
        jlog.info("root element {}", this._cursor);
        textMatch = new StringBuffer();

        advanceCursor();
    }


    public Result.Expectation getExpectation(Constituent constituent) {
        if (constituent.getNodeExpectation() == null)
            return Result.Expectation.UNEXPECTED;

        /**
         * fixed a bug, where the String getNodeExpectation was compared with the getDefinition object (RuleDefinition)
         */
        return ((constituent.getNodeExpectation().equals(getDefinition().getName())) ? Result.Expectation.MET
                : Result.Expectation.MISMATCH);
    }

    public String getName() {
        return this._ruleDefinition._name;
    }

    public Element getCursor() {
        return this._cursor;
    }

    public int getCurrentDepth() {
        return getElementDepth(this._cursor);
    }

    private int getElementDepth(Element element) {
        if (element == null)
            return -1;

        int depth = -1;

        for (Element parent = element.getParent();
             parent != null;
             parent = parent.getParent()) {
            depth++;
        }

        return this._createdAtDepth + depth;
    }

    public boolean isRelaxing() {
        if (this._cursor != null) {
            return Configuration.getRelaxElement().equals(this._cursor.getName());
        }
        return false;
    }

    private void startRelaxing(int depth, Constituent node) {
        this._relaxStack.push(depth, node);

        RuleDefinition._log.debug(this + " is relaxing");
    }

    protected void stopRelaxing(Constituent node) {
        RuleDefinition._log.debug(this + " stops relaxing");
        advanceCursor();
        RuleDefinition._log.debug(this + " advanced to " + RuleDefinition.elementToString(getCursor()) + " - " + getCurrentDepth());

        if (!isRelaxing()) {
            this._relaxStack.clear();
            advanceCursor();
            RuleDefinition._log.debug(this + " advanced to " + RuleDefinition.elementToString(getCursor()) + " - " + getCurrentDepth());

            if (isRelaxing()) {
                Constituent parent = node.getParent();
                RuleDefinition._log.debug(this + " pushing parent of relax following relax: [" + parent + "]");

                this._relaxStack.push(getCurrentDepth() - 1, parent);
            }
        } else {
            Constituent parent = node.getParent();
            RuleDefinition._log.debug(this + " pushing parent of relax following relax: [" + parent + "]");

            this._relaxStack.clear();
            this._relaxStack.push(getCurrentDepth() - 1, parent);
        }
    }

    public boolean startElement(int depth, Constituent node) {
        if (isRelaxing()) {
            Element expectedElement = peekAhead();


            boolean matchesThis = (node.match(getCursor())) && (depth == getCurrentDepth());

            boolean matchesNext = (node.match(expectedElement)) && (depth == peekAheadDepth());

            RuleDefinition._log.debug(this + " gets an " + node + " expecting " + RuleDefinition.elementToString(expectedElement));

            if ((matchesThis) || (matchesNext))
                stopRelaxing(node);
            else {
                startRelaxing(depth, node);
            }
            return false;
        }
        if (match(node)) {
            advanceCursor();
            RuleDefinition._log.debug(this + " matches so far - advanced to " + RuleDefinition.elementToString(getCursor()) + " - " + getCurrentDepth());
            if (isRelaxing()) {
                Constituent parent = node.getParent();
                this._relaxStack.push(getCurrentDepth() - 1, parent);
            }
            return false;
        }

        return true;
    }

    public boolean endElement(int depth, Constituent node) {
        boolean remove = false;

        if ((!this._relaxStack.isEmpty()) && (this._relaxStack.getHeadDepth() == depth)) {
            Constituent topElement = this._relaxStack.pop();

            if (this._relaxStack.isEmpty()) {
                Element followingSibling = RuleDefinition.getFollowingSibling(getCursor());

                if (followingSibling != null) {
                    remove = true;
                } else {
                    RuleDefinition._log.debug(this + " stops relaxing due to parent [" + node + "] closing");

                    advanceCursor();
                    RuleDefinition._log.debug(this + " advanced to " + RuleDefinition.elementToString(getCursor()));

                    if (isRelaxing()) {
                        Constituent parent = node.getParent();
                        RuleDefinition._log.debug(this + " pushing parent of relax following" + " relax: [" + parent + "]");

                        this._relaxStack.push(getCurrentDepth() - 1, parent);
                    }
                }
            }
        }

        return remove;
    }
    public Element peekAhead() {
        Element oldCursor = this._cursor;
        advanceCursor();
        Element ahead = this._cursor;
        this._cursor = oldCursor;
        return ahead;
    }

    public int peekAheadDepth() {
        return getElementDepth(peekAhead());
    }

    public void advanceCursor() {
        this._cursor = this._ruleDefinition.advance(this._cursor);
    }

    public boolean match(Constituent node) {
        return node.match(getCursor());
    }

    public RuleDefinition getDefinition() {
        return this._ruleDefinition;
    }

    public int getCreationDepth() {
        return this._createdAtDepth;
    }

    public void addTextMatch(String s) {
        this.textMatch.append(s);
    }

    public StringBuffer getTextMatch() {
        return this.textMatch;
    }

    public boolean expectsMore() {
        Element savedCursor = this._cursor;

        while (isRelaxing()) {
            advanceCursor();
        }

        boolean result = this._cursor != null;

        this._cursor = savedCursor;

        return result;
    }

    @Override
    public String toString() {
        Element next = peekAhead();
        String ahead = RuleDefinition.elementToString(next);
        String current = RuleDefinition.elementToString(this._cursor);
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(this._instance_id);
        sb.append("|");
        sb.append(this._ruleDefinition._name);
        sb.append(" (");
        sb.append(this._createdAtDepth);
        if (this._cursor != null) {
            sb.append("|");
            sb.append(getCurrentDepth());
        }
        sb.append(") @ ");
        sb.append(current);
        sb.append(" -> ");
        sb.append(ahead);
        sb.append("]");

        return sb.toString();
    }
}

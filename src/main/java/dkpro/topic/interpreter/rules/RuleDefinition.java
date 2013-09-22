package dkpro.topic.interpreter.rules;


import dkpro.topic.annotator.DocResultsHolder;
import dkpro.topic.interpreter.data.Constituent;
import dkpro.topic.interpreter.SAXParser;
import dkpro.topic.utils.*;
import dkpro.topic.utils.XMLUtils;
import dkpro.topic.interpreter.TopicInterpreter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.SAXException;

import java.util.*;

public class RuleDefinition {
    public static Log _log = LogFactory.getLog(RuleDefinition.class);
    public static int _next_instance_id;
    public final String _name;
    private final String _topicType;
    public final Document _structure;
    private final Map<RuleDefinition, Boolean> _matchesDefinition = new WeakHashMap();

    public RuleDefinition(String name, String topicType, Document structure) {
        this._name = name;
        this._structure = structure;
        this._topicType = topicType;
    }

    public Document getStructure() {
        return this._structure;
    }

    public String getName() {
        return this._name;
    }

    public boolean match(Constituent node) {
        return node.match(advance(this._structure.getRootElement()));
    }

    public static Element getFollowingSibling(Element e) {
        List childrenOfParent = getChildren(e.getParent());
        for (int i = 0; i < childrenOfParent.size(); i++) {
            Element currentChild = (Element) childrenOfParent.get(i);
            if ((e == currentChild) &&
                    (i + 1 < childrenOfParent.size())) {
                return (Element) childrenOfParent.get(i + 1);
            }

        }

        return null;
    }

    private static List<Element> getChildren(Element e) {
        List children = new ArrayList();
        for (Node n : (List<Node>) e.content()) {
            if ((n instanceof Element)) {
                children.add((Element) n);
            }
        }
        return children;
    }

    Element advance(Element cursor) {
        Element current = cursor;
        if (current == null) {
            return null;
        }

        List children = getChildren(current);

        // if node has children, get direct child of node and return
        if (children.size() > 0) {
            return (Element) children.get(0);
        }
        // assume node has no children, return sibling (to the right) and return
        while (current.getParent() != null) {
            Element followingSibling = getFollowingSibling(current);
            if (followingSibling != null) {
                return followingSibling;
            }
            // if there are no siblings of the current node, go back!
            current = current.getParent();
        }

        return null;
    }

    public String getTopicType() {
        return this._topicType;
    }

    public String toString() {
        return "[" + this._name + "]";
    }

    public RuleInstance instantiate(int creationDepth) {
        RuleInstance r = new RuleInstance(this, creationDepth);
        _log.debug(r + " instantiated");
        return r;
    }

    public boolean matches(RuleDefinition rd) {
        Boolean match = (Boolean) this._matchesDefinition.get(rd);
        if (match != null)
            return match.booleanValue();

        Element firstRuleElement = (Element) rd.getStructure().selectSingleNode("/ROOT/*");

        match = Boolean.valueOf(matches(firstRuleElement));

        this._matchesDefinition.put(rd, match);

        return match.booleanValue();
    }

    public boolean matches(Element element) {
        _log.debug("START MATCH");
        try {
            RuleBook testBook = new RuleBook();
            testBook.addRule(this);
            DocResultsHolder topicResults = DocResultsHolder.createResultMap();
            TopicInterpreter tri = new TopicInterpreter(testBook);

            SAXParser sw = new SAXParser(tri, topicResults);

            XMLUtils.parse(element, sw);

            List allRulesMatched = tri.getAllRulesMatched();
            if (allRulesMatched.size() == 0) {
                return false;
            }
            int depth = ((RuleInstance) allRulesMatched.get(0)).getCreationDepth();
            return depth == 1;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } finally {
            _log.debug("END MATCH");
        }
    }

    public static String elementToString(Element e) {
        return e != null ? e.getName() + "/" + e.attributeValue(Configuration.getAttrConstType()) : null;
    }

    protected static class RelaxStack {
        private final Stack<Constituent> _stack = new Stack();
        private int rootDepth;

        public synchronized String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            if (!isEmpty()) {
                sb.append(getRootDepth());
                sb.append('|');
                sb.append(getHeadDepth());
                sb.append('|');
            }
            Iterator i = this._stack.iterator();
            while (i.hasNext()) {
                Constituent n = (Constituent) i.next();
                sb.append(n);
                if (i.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(']');
            return sb.toString();
        }

        public Constituent push(int depth, Constituent node) {
            if (isEmpty()) {
                this.rootDepth = depth;
            }
            Constituent n = (Constituent) this._stack.push(node);
            RuleDefinition._log.debug("PUSH  " + this);
            return n;
        }

        public synchronized Constituent pop() {
            Constituent e = (Constituent) this._stack.pop();
            RuleDefinition._log.debug("POP   " + this);
            return e;
        }

        public void clear() {
            this._stack.clear();
            RuleDefinition._log.debug("CLEAR  " + this);
        }

        public int size() {
            return this._stack.size();
        }

        public boolean isEmpty() {
            return this._stack.isEmpty();
        }

        public int getHeadDepth() {
            return this.rootDepth + this._stack.size() - 1;
        }

        public int getRootDepth() {
            return this.rootDepth;
        }
    }
}


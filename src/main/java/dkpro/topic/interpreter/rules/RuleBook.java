package dkpro.topic.interpreter.rules;

import dkpro.topic.interpreter.data.Constituent;
import dkpro.topic.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author eckart@ukp.informatik.tu-darmstadt.de, hanl@ids-mannheim.de
 * @date 11/6/13
 */
public class RuleBook
        implements Iterable<RuleDefinition> {
    private static final Log _log = LogFactory.getLog(RuleBook.class);
    private static final String TAG_RULE = "rule";
    private static final String ATTR_RULE_IDENTIFIER = "id";
    private static final String ATTR_TOPIC_TYPE = "label";
    private List<RuleDefinition> _ruleDefinitions;

    public RuleBook() {
        this._ruleDefinitions = new ArrayList();
    }

    public void addRule(RuleDefinition rd) {
        this._ruleDefinitions.add(rd);
    }

    public Iterator<RuleDefinition> iterator() {
        return this._ruleDefinitions.iterator();
    }

    public void getRules(int depth, Collection<RuleInstance> activeRules, Constituent node) {
        for (RuleDefinition rd : this._ruleDefinitions)
            if (rd.match(node))
                activeRules.add(rd.instantiate(depth));
    }

    public RuleDefinition getDefinitionByName(String name) {
        for (RuleDefinition def : this._ruleDefinitions) {
            if (def.getName().equals(name))
                return def;
        }
        return null;
    }

    public Map<RuleDefinition, Collection<RuleDefinition>> generateRules() {
        _log.debug("--- We are going through the rules determining how they relate");
        Map<RuleDefinition, Collection<RuleDefinition>> ruleGen = new LinkedHashMap<>();

        for (RuleDefinition r1 : this) {
            Collection<RuleDefinition> supersededBy = new ArrayList<>();
            for (RuleDefinition r2 : this) {
                if ((r1 != r2) && (r1.matches(r2)))
                    supersededBy.add(r2);
            }
            ruleGen.put(r1, supersededBy);
        }
        return ruleGen;
    }



    public void read(File file)
            throws ParserConfigurationException, SAXException, IOException {
        XMLUtils.parse(file, new RuleHandler());
    }

    public void read(InputStream is)
            throws ParserConfigurationException, SAXException, IOException {
        XMLUtils.parse(is, new RuleHandler());
    }

    public int size() {
        return this._ruleDefinitions.size();
    }

    public String toString() {
        return _ruleDefinitions.toString();
    }

    private class RuleHandler extends DefaultHandler {
        private String _currentName = null;
        private String _currentType = null;
        private SAXContentHandler _currentRuleStructure = null;

        RuleHandler() {
        }

        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {
            if (TAG_RULE.equals(name)) {
                this._currentName = attributes.getValue(ATTR_RULE_IDENTIFIER).trim();
                this._currentType = attributes.getValue(ATTR_TOPIC_TYPE).trim();

                //the saxhandler introduces start and end ROOT tags, so each rule can be parsed seperately!!
                this._currentRuleStructure = new SAXContentHandler();
                this._currentRuleStructure.startElement("", "", "ROOT", new AttributesImpl());

                return;
            }

            if (this._currentRuleStructure != null)
                this._currentRuleStructure.startElement(uri, localName, name, attributes);
        }

        public void endElement(String uri, String localName, String name)
                throws SAXException {
            if (TAG_RULE.equals(name)) {
                this._currentRuleStructure.endElement("", "", "ROOT");

                RuleDefinition r = new RuleDefinition(this._currentName, this._currentType, this._currentRuleStructure.getDocument());

                RuleBook.this._ruleDefinitions.add(r);

                this._currentName = null;
                this._currentRuleStructure = null;

                RuleBook._log.debug("Created rule: " + r);
            }

            if (this._currentRuleStructure != null)
                this._currentRuleStructure.endElement(uri, localName, name);
        }
    }
}


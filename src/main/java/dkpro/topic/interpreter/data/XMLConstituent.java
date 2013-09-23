package dkpro.topic.interpreter.data;

import dkpro.topic.interpreter.data.Constituent;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.List;
import java.util.regex.Pattern;

public class XMLConstituent
        implements Constituent {
    private final Constituent _parent;
    private final Element _element;
    private final String sentenceID;

    public XMLConstituent(Constituent parent, Element element, String sentID) {
        this._parent = parent;
        this._element = element;
        if (sentID == null || sentID.equals(""))
            this.sentenceID = null;
        else
            this.sentenceID = sentID;
    }

    public Constituent getParent() {
        return this._parent;
    }

    public String getSentenceID() {
        return sentenceID;
    }

    public boolean match(Element ruleElement) {
        if (ruleElement == null)
            return false;


        if (!this._element.getName().equals(ruleElement.getName()))
            return false;


        for (Attribute attr : (List<Attribute>) this._element.attributes()) {
            String name = attr.getName();
            String value = attr.getValue();
            String regex = "^" + ruleElement.attributeValue(name, ".*") + "$";

            if (!Pattern.matches(regex, value))
                return false;
        }
        return true;
    }

    public String getNodeExpectation() {
        return this._element.attributeValue("expect");
    }
}
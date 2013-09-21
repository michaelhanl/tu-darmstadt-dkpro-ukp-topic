package dkpro.topic.interpreter;

import dkpro.topic.interpreter.data.Constituent;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.List;
import java.util.regex.Pattern;

public class XMLConstituent
        implements Constituent {
    private final Constituent _parent;
    private final Element _element;

    public XMLConstituent(Constituent parent, Element element) {
        this._parent = parent;
        this._element = element;

    }

    public Constituent getParent() {
        return this._parent;
    }

    public boolean match(Element ruleElement) {
        if (ruleElement == null) {
            return false;
        }

        if (!this._element.getName().equals(ruleElement.getName())) {
            return false;
        }

        for (Attribute attr : (List<Attribute>) this._element.attributes()) {
            String name = attr.getName();
            String value = attr.getValue();

            String regex = "^" + ruleElement.attributeValue(name, ".*") + "$";

            if (!Pattern.matches(regex, value)) {
                return false;
            }
        }

        return true;
    }

    public String getExpected() {
        return this._element.attributeValue("expect");
    }



}


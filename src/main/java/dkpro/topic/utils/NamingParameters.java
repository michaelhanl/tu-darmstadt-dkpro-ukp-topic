package dkpro.topic.utils;

import java.util.Properties;

/**
 * loads XML element names from property file
 *
 * @author micha.hanl@gmail.com
 * @date 11/6/13
 */
public class NamingParameters {
    public static String FILENAME = "Attributes.dat";
    public boolean enabledOverride = false;

    private Properties properties;

    protected NamingParameters(Properties props) {
        this.properties = props;
    }

    private String getParameter(String key) {
        String property = this.properties.getProperty(key);
        if (property != null)
            return property;
        else
            throw new RuntimeException("Property '" + key + "' is missing");

    }


    public String getDocRoot() {
        return this.getParameter("generic.document");
    }

    public String getElementSentence() {
        return this.getParameter("generic.sentence");
    }

    public String getElementConstituent() {
        return this.getParameter("generic.constituent");
    }

    public String getAttrDocName() {
        return this.getParameter("generic.docname");
    }

    public String getAttrLemma() {
        return this.getParameter("generic.lemma");
    }

    public String getAttrCitationID() {
        return this.getParameter("generic.citeid");
    }

    public String getAttrConstType() {
        return this.getParameter("generic.constituenttype");
    }

    public String getAttrSentenceID() {
        return this.getParameter("generic.id");
    }

    public String getAttrTopicLabel() {
        return this.getParameter("generic.topiclabel");
    }

    public String getAttrTopicRule() {
        return this.getParameter("generic.topicrule");
    }

    //todo: either move or delete
    @Deprecated
    public boolean isAutoOverEn() {
        return enabledOverride;
    }

    public void setEnableOverride(boolean enabledOverride) {
        this.enabledOverride = enabledOverride;
    }

    public String getRelaxElement() {
        return this.getParameter("generic.relax");
    }

}

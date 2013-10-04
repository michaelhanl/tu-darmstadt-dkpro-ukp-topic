package dkpro.topic.utils;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * loads XML element names from property file
 *
 * @author hanl@ids-mannheim.de
 */

public class Configuration extends Properties {
    public static String path;
    public static String CONFIGURATION = "config.properties";
    public static String CONFIGDIR = "config";
    public static String LOG4J = "log4j.properties";
    public static boolean enabledOverride = false;
    private static String schema;

    private static final long serialVersionUID = -5873657829605070424L;
    private static Properties ParameterProperties = new Properties();
    private static Logger _log = LoggerFactory.getLogger(Configuration.class);

    public static String bootstrapConfiguration() {
        _log.info("loading properties");
        Configuration.setEnableOverride(true);
        File f = new File(System.getProperty("java.class.path"));
        String path = f.getAbsoluteFile().getParentFile().getAbsolutePath();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(path +"/"+ Configuration.CONFIGDIR
                    + "/" + Configuration.LOG4J));
        } catch (IOException e) {
            e.printStackTrace();
        }
        schema = path +"/"+ CONFIGDIR + "/"+ "schema.xsd";
        PropertyConfigurator.configure(props);
        Configuration.loadConfigurationProperties(path);
        return path;
    }

    private static void setConfigurationProperties(
            Properties configurationProperties) {
        Configuration.ParameterProperties = configurationProperties;
    }

    private static Properties getConfigurationProperties() {
        return ParameterProperties;
    }

    private static String getParameter(String key) {
        String property = getConfigurationProperties().getProperty(key);
        if (property != null)
            return property;
        else
            throw new RuntimeException("Property '" + key + "' is missing");

    }

    public static void loadConfigurationProperties(String trimPath) {

        Properties properties = new Properties();
        String path = new String();

        try {
            byte[] bytes = trimPath.getBytes("UTF-8");
            path = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            _log.error("Encoding not supported", e.getMessage(), e);
            System.exit(0);
        }

        try {
            properties.load(new FileInputStream(path +"/"+ CONFIGDIR + "/"
                    + CONFIGURATION));
            setConfigurationProperties(properties);

        } catch (final IOException e) {
            _log.error("IOException", e.getMessage(), e);
            System.exit(0);

        }
    }

    public static String getDocRoot() {
        return Configuration.getParameter("generic.document");
    }

    public static String getElementSentence() {
        return Configuration.getParameter("generic.sentence");
    }

    public static String getElementConstituent() {
        return Configuration.getParameter("generic.constituent");
    }

    public static String getAttrDocName() {
        return Configuration.getParameter("generic.docname");
    }

    public static String getAttrLemma() {
        return Configuration.getParameter("generic.lemma");
    }

    public static String getAttrCitationID() {
        return Configuration.getParameter("generic.citeid");
    }

    public static String getAttrConstType() {
        return Configuration.getParameter("generic.constituenttype");
    }

    public static String getAttrSentenceID() {
        return Configuration.getParameter("generic.id");
    }

    public static String getAttrTopicLabel() {
        return Configuration.getParameter("generic.topiclabel");
    }

    public static String getAttrTopicRule() {
        return Configuration.getParameter("generic.topicrule");
    }

    public static boolean isAutoOverEn() {
        return enabledOverride;
    }

    public static void setEnableOverride(boolean enabledOverride) {
        Configuration.enabledOverride = enabledOverride;
    }

    public static String getRelaxElement() {
        return Configuration.getParameter("generic.relax");
    }

    public static String getSchemaPath() {
        return schema;
    }

}

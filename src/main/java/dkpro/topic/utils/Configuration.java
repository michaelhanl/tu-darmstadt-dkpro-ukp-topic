package dkpro.topic.utils;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Configuration class to load parameter from args in. Assumes a default values for log4j properties
 * and a configuration directory (config)
 *
 * @author hanl@ids-mannheim.de
 * @date 11/6/13
 */
public class Configuration {

    public static final String CONFIGDIR = "config";
    public static final String LOG4J = "log4j.properties";
    public static final String GERMAN = "de";
    public static final String ENGLISH = "en";
    public static final String PCFG = "pcfg";
    public static final String FACTORED = "factored";
    private static final String GERMAN_ENCODING = "ISO_8859-1";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static Logger _log = LoggerFactory.getLogger(Configuration.class);
    private static String lang = "";
    private static String input = "";
    private static String output = "";
    private static String model = "";
    private static int sentID = 1;
    private static int citeID = 1;
    private static int levelID = 1;
    private static DecimalFormat df1 = new DecimalFormat("000");
    private static DecimalFormat df2 = new DecimalFormat("00");
    private static File ruleFile = null;

    public static File getRuleFile() {
        return ruleFile;
    }

    public static void setRuleFile(File ruleFile) {
        Configuration.ruleFile = ruleFile;
    }

    public static String getSentID() {
        return df1.format(sentID);
    }

    public static String getCiteID() {
        return levelID + "." + df2.format(citeID);
    }

    public static void incrementCiteID() {
        int i = citeID % 2;
        if (i == 0) {
            levelID += 1;
            citeID = 0;
        }
        citeID += 1;
    }

    public static void resetCiteID() {
        citeID = 1;
        levelID = 1;
    }

    public static void resetSentID() {
        sentID = 1;
    }

    public static void incrementSentID() {
        sentID += 1;
    }

    public static String getModel() {
        if (model.isEmpty())
            model = PCFG;
        _log.trace("ModMessage: Using model: " + model);
        return model;
    }

    public static void setModel(final String model) {
        _log.debug("Setting model: {}", model);
        Configuration.model = model;
    }

    public static String getEncoding() {
        switch (lang) {
            case GERMAN:
                return GERMAN_ENCODING;
            default:
                return DEFAULT_ENCODING;
        }
    }

    public static String getLang() {
        if (lang.isEmpty())
            lang = ENGLISH;
        _log.trace("LangMessage: Using language: " + lang);
        return lang;
    }

    public static void setLang(final String lang) {
        _log.debug("Setting language: {}", lang);
        Configuration.lang = lang;
    }

    public static String getInputDir() {
        if (input.isEmpty()) {
            try {
                input = new File(".").getCanonicalPath() + "/" + "input";
                File in = new File(input);
                in.mkdirs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        _log.trace("InputMessage: Using input dir: {}", input);
        return input;
    }

    public static void setFilesDir(final String input) {
        _log.debug("Setting Files directory: {}", input);
        Configuration.input = input;
    }

    public static String getOutputDir() {
        if (output.isEmpty()) {
            try {
                output = new File(".").getCanonicalPath() + "/" + "output"
                        + "/" + getModel();
                File out = new File(output);
                out.mkdirs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        _log.trace("OutputMessage: Using output dir: " + output);
        return output;
    }

    public static void setOutputDir(final String output) {
        String out = null;
        try {
            out = new File(".").getCanonicalPath() + "/" + output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        _log.debug("Setting OutputDir: " + out);
        Configuration.output = output;
    }

    public static String getTitle(String s) {
        String[] doc = s.split("\\.(?=[^\\.]+$)");
        return doc[0];
    }

    public static String buildTargetDocPath(String path, String inputTitle) {
        String title = getTitle(inputTitle);
        StringBuffer documentPath = new StringBuffer();
        documentPath.append(path + "/");
        documentPath.append(title + ".xml");
        return documentPath.toString();

    }

    /**
     * default is English! currently only English and German rules are supported
     *
     * @param fileDir
     * @return
     */
    public static void retrieveRuleFiles(String fileDir) throws IllegalArgumentException {
        File dir = new File(fileDir);

        FileFilter ruleFilter = new WildcardFileFilter("Rules_*.xml");
        File[] files = dir.listFiles(ruleFilter);
        _log.debug("found rule files: " + Arrays.asList(files));

        System.out.println();
        for (File in : files) {
            String trimName = getTitle(in.getName());
            String languageExtension = trimName
                    .substring(trimName.length() - 2);

            if (getLang().equals(GERMAN)) {
                if (languageExtension.toLowerCase().contains(GERMAN)) {
                    _log.info("setting rule file {}", in.getName());
                    setRuleFile(in);
                    break;
                } else
                    continue;
            } else if (getLang().equals(ENGLISH)) {
                if (languageExtension.toLowerCase().contains(ENGLISH)) {
                    _log.info("setting rule file {}", in.getName());
                    setRuleFile(in);
                    break;
                } else
                    continue;
            }
        }

        if (getRuleFile() == null)
            throw new IllegalArgumentException("No rule file set! Please check you have " +
                    "set the correct directory or language");
    }
}

package dkpro.topic.main;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import dkpro.topic.constituents.ConstituentWriter;
import dkpro.topic.interpreter.TREEntryPoint;
import dkpro.topic.utils.ConfigUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.pipeline.SimplePipeline;

import java.io.IOException;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

public class ParsingPipeline {

    /**
     * Uses UIMAFit pipeline mechanism to create workflow for XML annotation and
     * topic identification
     *
     * @author hanl
     */

    static Logger _log = LoggerFactory.getLogger(ParsingPipeline.class);

    private CollectionReader collReader;
    private AnalysisEngineDescription segmenter;
    private AnalysisEngineDescription parser;
    private AnalysisEngineDescription cxmi;
    private AnalysisEngineDescription constituentXML;
    private AnalysisEngineDescription treeRuleEngine;

    public void runTopicEngineOnly() throws AnnotatorConfigurationException,
            ResourceInitializationException {
        /*
         * reads input files from Param_Path default: "src/test/resources"
		 */
        _log.debug("initize FileReader");
        collReader = createCollectionReader(TextReader.class,
                TextReader.PARAM_PATH, ConfigUtils.getFilesDir(),
                TextReader.PARAM_LANGUAGE, ConfigUtils.getLang(),
                TextReader.PARAM_PATTERNS, new String[]{"[+]*.xml"});

        _log.debug("initialize Tree-Rule-Engine");
        treeRuleEngine = createPrimitiveDescription(TREEntryPoint.class,
                TREEntryPoint.PARAM_PATH, ConfigUtils.getFilesDir());

        try {
            _log.debug("run Analysis Pipeline");
            SimplePipeline.runPipeline(collReader, treeRuleEngine);

        } catch (UIMAException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runStanfordParser() throws AnnotatorConfigurationException,
            ResourceInitializationException {
        collReader = createCollectionReader(TextReader.class,
                TextReader.PARAM_PATH, ConfigUtils.getFilesDir(),
                TextReader.PARAM_LANGUAGE, ConfigUtils.getLang(),
                TextReader.PARAM_PATTERNS, new String[]{"[+]*.txt"});
		/*
		 * loads segmentation annotator for the Stanford Tools
		 */
        segmenter = createPrimitiveDescription(StanfordSegmenter.class);

		/*
		 * laods the Stanford Parser and implements parsing parameters and
		 * parsing tree parameters
		 */

        _log.debug("initialize Parser");
        // StanfordParser.PARAM_MAX_ITEMS, 5000000
        parser = createPrimitiveDescription(StanfordParser.class,
                StanfordParser.PARAM_LANGUAGE, ConfigUtils.getLang(),
                StanfordParser.PARAM_VARIANT, ConfigUtils.getModal(),
                StanfordParser.PARAM_CREATE_CONSTITUENT_TAGS, true,
                StanfordParser.PARAM_CREATE_DEPENDENCY_TAGS, false);

        _log.debug("initialize XMI Writer");
        cxmi = createPrimitiveDescription(XmiWriter.class,
                XmiWriter.PARAM_PATH, "target/xmi",
                XmiWriter.PARAM_TYPE_SYSTEM_FILE, "TypeSystem.xml");

		/*
		 * loads constituent writer to transform JCas object to XML format,
		 * readable by the TRE
		 */
        System.out.println("ouput dir: " + ConfigUtils.getOutputDir());
        _log.debug("initialize Constituent Writer");
        constituentXML = createPrimitiveDescription(ConstituentWriter.class,
                ConstituentWriter.PARAM_PATH, ConfigUtils.getOutputDir());

        /**
         * run pipeline with instantiated AnalysisEngines
         */

        try {
            _log.debug("run Analysis Pipeline");
            SimplePipeline.runPipeline(collReader, segmenter, parser,
                    constituentXML);
        } catch (UIMAException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runAll() throws AnnotatorConfigurationException,
            ResourceInitializationException {
        collReader = createCollectionReader(TextReader.class,
                TextReader.PARAM_PATH, ConfigUtils.getFilesDir(),
                TextReader.PARAM_LANGUAGE, ConfigUtils.getLang(),
                TextReader.PARAM_PATTERNS, new String[]{"[+]*.txt"});
		/*
		 * loads segmentation annotator for the Stanford Tools
		 */
        segmenter = createPrimitiveDescription(StanfordSegmenter.class);

		/*
		 * laods the Stanford Parser and implements parsing parameters and
		 * parsing tree parameters
		 */

        _log.debug("initialize Parser");
        // StanfordParser.PARAM_MAX_ITEMS, 5000000
        parser = createPrimitiveDescription(StanfordParser.class,
                StanfordParser.PARAM_LANGUAGE, ConfigUtils.getLang(),
                StanfordParser.PARAM_VARIANT, ConfigUtils.getModal(),
                StanfordParser.PARAM_CREATE_CONSTITUENT_TAGS, true,
                StanfordParser.PARAM_CREATE_DEPENDENCY_TAGS, false);

        _log.debug("initialize XMI Writer");
        cxmi = createPrimitiveDescription(XmiWriter.class,
                XmiWriter.PARAM_PATH, "target/xmi",
                XmiWriter.PARAM_TYPE_SYSTEM_FILE, "TypeSystem.xml");

		/*
		 * loads constituent writer to transform JCas object to XML format,
		 * readable by the TRE
		 */
        _log.debug("initialize Constituent Writer");
        constituentXML = createPrimitiveDescription(ConstituentWriter.class,
                ConstituentWriter.PARAM_PATH, ConfigUtils.getOutputDir());

		/*
		 * takes XML files as input and produces statistics output in console
		 * for topic identification
		 */
        _log.debug("initialize Tree-Rule-Engine");
        treeRuleEngine = createPrimitiveDescription(TREEntryPoint.class,
                TREEntryPoint.PARAM_PATH, ConfigUtils.getOutputDir());
        /**
         * run pipeline with instantiated AnalysisEngines
         */

        try {
            _log.debug("run Analysis Pipeline");
            SimplePipeline.runPipeline(collReader, segmenter, parser,
                    constituentXML, treeRuleEngine);
        } catch (UIMAException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Deprecated
    private void init() {
        ConfigUtils.setFilesDir("resources/current/");
        ConfigUtils.setModal(ConfigUtils.PCFG);
        ConfigUtils.setOutputDir("XMLOutput/" + "current");
        ConfigUtils.setLang(ConfigUtils.GERMAN);
        ConfigUtils.retrieveRuleFiles("/Volumes/Mac User/Users/michael/Library/"
                + "Workspace/tu.darmstadt.ukp.dkpro.theme-german/src/main/resources");
        // Configuration.loadConfigurationProperties();
    }
}
//
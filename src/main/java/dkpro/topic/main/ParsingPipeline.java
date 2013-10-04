package dkpro.topic.main;

import dkpro.topic.utils.ConfigUtils;
import dkpro.topic.components.UIMAComponents;
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


    private void defaultSetup() throws ResourceInitializationException {
        collReader = UIMAComponents.setupReader(UIMAComponents.TEXT);
        segmenter = UIMAComponents.setupSegmenter();
        parser = UIMAComponents.setupParser();
        cxmi = UIMAComponents.setupXMIWriter();
        constituentXML = UIMAComponents.setupConstituentWriter();
        treeRuleEngine = UIMAComponents.setupTreeRuleEngine(ConfigUtils.getOutputDir());
    }

    public void runTopicEngineOnly() throws AnnotatorConfigurationException,
            ResourceInitializationException {
        collReader = UIMAComponents.setupReader(UIMAComponents.XML);
        treeRuleEngine = UIMAComponents.setupTreeRuleEngine(ConfigUtils.getInputDir());

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
        collReader = UIMAComponents.setupReader(UIMAComponents.TEXT);
        segmenter = UIMAComponents.setupSegmenter();
        parser = UIMAComponents.setupParser();
        constituentXML = UIMAComponents.setupConstituentWriter();

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
        defaultSetup();
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
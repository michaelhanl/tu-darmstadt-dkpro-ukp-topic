package dkpro.topic.main;

import dkpro.topic.components.UIMAComponents;
import dkpro.topic.utils.Configuration;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.pipeline.SimplePipeline;

import java.io.IOException;

/**
 * Uses UIMAFit pipeline mechanism to create workflow for XML annotation and
 * topic identification
 *
 * @author hanl@ids-mannheim.de
 * @date 11/6/13
 */
public class ParsingPipeline {

    private static Logger _log = LoggerFactory.getLogger(ParsingPipeline.class);

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
        treeRuleEngine = UIMAComponents.setupTreeRuleEngine(Configuration.getOutputDir());
    }

    public void runTopicEngineOnly() throws AnnotatorConfigurationException,
            ResourceInitializationException {
        collReader = UIMAComponents.setupReader(UIMAComponents.XML);
        treeRuleEngine = UIMAComponents.setupTreeRuleEngine(Configuration.getInputDir());

        try {
            _log.debug("run Analysis Pipeline");
            SimplePipeline.runPipeline(collReader, treeRuleEngine);
        } catch (UIMAException e) {
            _log.error("UIMA Exception", e.getMessage());
        } catch (IOException e) {
            _log.error("IO Exception", e.getMessage());
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
            _log.error("UIMA Exception", e.getMessage());
        } catch (IOException e) {
            _log.error("IO Exception", e.getMessage());
        }
    }

    public void runPipeline() throws AnnotatorConfigurationException,
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
            _log.error("UIMA Exception", e.getMessage());
        } catch (IOException e) {
            _log.error("IO Exception", e.getMessage());
        }

    }

    @Deprecated
    private void init() {
        Configuration.setFilesDir("resources/current/");
        Configuration.setModel(Configuration.PCFG);
        Configuration.setOutputDir("XMLOutput/" + "current");
        Configuration.setLang(Configuration.GERMAN);
        Configuration.retrieveRuleFiles("/Volumes/Mac User/Users/michael/Library/"
                + "Workspace/tu.darmstadt.ukp.dkpro.theme-german/src/main/resources");
        // NamingParameters.loadConfigurationProperties();
    }


}
//
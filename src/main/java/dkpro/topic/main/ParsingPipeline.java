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


    private ParsingPipeline() {

    }

    public static ParsingPipeline runTopicEngineOnly() throws AnnotatorConfigurationException,
            ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline();
        p.collReader = UIMAComponents.setupReader(UIMAComponents.XML);
        p.treeRuleEngine = UIMAComponents.setupTreeRuleEngine(Configuration.getInputDir());
        /**
         * run pipeline with instantiated AnalysisEngines
         */
        p.runPipeline(p.collReader, p.treeRuleEngine);
        return p;
    }

    private void defaultSetup() throws ResourceInitializationException {
        collReader = UIMAComponents.setupReader(UIMAComponents.TEXT);
        segmenter = UIMAComponents.setupSegmenter();
        parser = UIMAComponents.setupParser();
        cxmi = UIMAComponents.setupXMIWriter();
        constituentXML = UIMAComponents.setupConstituentWriter();
        treeRuleEngine = UIMAComponents.setupTreeRuleEngine(Configuration.getOutputDir());
    }

    public static ParsingPipeline runStanfordParser() throws AnnotatorConfigurationException,
            ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline();
        p.collReader = UIMAComponents.setupReader(UIMAComponents.TEXT);
        p.segmenter = UIMAComponents.setupSegmenter();
        p.parser = UIMAComponents.setupParser();
        p.constituentXML = UIMAComponents.setupConstituentWriter();

        /**
         * run pipeline with instantiated AnalysisEngines
         */
        p.runPipeline(p.collReader, p.segmenter, p.parser,
                p.constituentXML);
        return p;
    }

    public static ParsingPipeline runFullInterpreter() throws AnnotatorConfigurationException,
            ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline();
        p.defaultSetup();

        /**
         * run pipeline with instantiated AnalysisEngines
         */
        p.runPipeline(p.collReader, p.segmenter, p.parser,
                p.constituentXML, p.treeRuleEngine);
        return p;
    }

    private void runPipeline(CollectionReader reader, AnalysisEngineDescription... engines) {
        try {
            _log.debug("run Analysis Pipeline");
            SimplePipeline.runPipeline(reader, engines);
        } catch (UIMAException e) {
            _log.error("UIMA Exception", e);
        } catch (IOException e) {
            _log.error("IO Exception", e);
        }
    }
}

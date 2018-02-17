package dkpro.topic.main;

import dkpro.topic.components.UIMAComponents;
import dkpro.topic.utils.ConfigParameters;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses UIMAFit pipeline mechanism to create workflow for XML annotation and
 * topic identification
 *
 * @author micha.hanl@gmail.com
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


    private List<AnalysisEngineDescription> analysisEngines;

    private ParsingPipeline() {
        analysisEngines = new ArrayList<>();
    }

    public static ParsingPipeline runTopicEngineOnly(ConfigParameters c) throws AnnotatorConfigurationException,
            ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline();
        p.collReader = UIMAComponents.setupReader(UIMAComponents.XML, c);
        p.treeRuleEngine = UIMAComponents.setupTreeRuleEngine(c);
        /**
         * run pipeline with instantiated AnalysisEngines
         */
        p.runPipeline(p.collReader, p.treeRuleEngine);
        return p;
    }

    // todo: create static factory methods from this
    private void fullPipeline(ConfigParameters c) throws ResourceInitializationException {
        collReader = UIMAComponents.setupReader(UIMAComponents.TEXT, c);
        segmenter = UIMAComponents.setupSegmenter();
        parser = UIMAComponents.setupParser(c);
        cxmi = UIMAComponents.setupXMIWriter();
        constituentXML = UIMAComponents.setupConstituentWriter(c);
        treeRuleEngine = UIMAComponents.setupTreeRuleEngine(c);
    }

    public static ParsingPipeline runStanfordParser(ConfigParameters c) throws AnnotatorConfigurationException,
            ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline();
        p.collReader = UIMAComponents.setupReader(UIMAComponents.TEXT, c);
        p.segmenter = UIMAComponents.setupSegmenter();
        p.parser = UIMAComponents.setupParser(c);
        p.constituentXML = UIMAComponents.setupConstituentWriter(c);

        /**
         * run pipeline with instantiated AnalysisEngines
         */
        p.runPipeline(p.collReader, p.segmenter, p.parser,
                p.constituentXML);
        return p;
    }

    public static ParsingPipeline runFullInterpreter(ConfigParameters c)
            throws ResourceInitializationException {
        ParsingPipeline p = new ParsingPipeline();
        p.fullPipeline(c);

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

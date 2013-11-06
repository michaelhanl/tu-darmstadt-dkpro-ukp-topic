/*
 * Copyright (c) 2013. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package dkpro.topic.main;

import dkpro.topic.utils.Configuration;
import dkpro.topic.utils.NamingParameters;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Main class
 * @author hanl@ids-mannheim.de
 * @date 11/6/13
 */
public class Main {

    private static Logger _log = LoggerFactory.getLogger(Main.class);
    private static boolean topicAnnotator = false;
    private static ParsingPipeline pipeline;

    public static boolean isAEn() {
        return topicAnnotator;
    }

    public static void main(String[] args) {
        pipeline = new ParsingPipeline();
        if (args.length == 0) {
            System.out.println("please specify parameters!");
            System.out
                    .println("help: 'java -jar <jarFile> -parser | -topic | -all ... [-toFile]'");
            System.exit(-1);
        }
        String path = NamingParameters.bootstrapConfiguration();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-all":
                    _log.info("running entire annotation engine");
                    runPipeline(args, path);
                    break;
                case "-parser":
                    _log.info("running stanford parser engine");
                    runStanfordParser(args, path);
                    break;
                case "-learn":
                    // TODO: doesn't work. compilation/rule failure in source code
                    _log.info("running annotator learning engine");
                    runAnnoLabLearner(args);
                    break;
                case "-topic":
                    _log.info("running topic engine only");
                    runTopicEngine(args, path);
                    break;
            }
        }
    }

    private static void runStanfordParser(String[] args, String path) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "german":
                    Configuration.setLang(Configuration.GERMAN);
                    break;
                case "english":
                    Configuration.setLang(Configuration.ENGLISH);
                    break;
                case "-inDir":
                    Configuration.setFilesDir(args[i + 1]);
                    break;
                case "-outDir":
                    Configuration.setOutputDir(args[i + 1]);
                    break;
                case "-modal":
                    if (args[i + 1].equalsIgnoreCase(Configuration.PCFG)
                            | args[i + 1].equalsIgnoreCase(Configuration.FACTORED)) {
                        Configuration.setModal(args[i + 1]);
                        break;
                    } else {
                        System.out
                                .println("Please use 'pcfg' or 'factored' as modal description");
                        System.exit(-1);
                        return;
                    }
                case "-help":
                    System.out
                            .println("Help: java -jar Annotator.jar -parser <Language> -rules <RuleFile> -in <InputDir> -out <OutputDir> -m <Modal> [-toFile]");
                    System.exit(-1);
                    return;
            }
        }
        Configuration.retrieveRuleFiles(path + "/" + Configuration.CONFIGDIR);
        try {
            pipeline.runStanfordParser();
        } catch (AnnotatorConfigurationException
                | ResourceInitializationException e) {
            _log.error("Parsing could not be completed, due to internal error",
                    e.getMessage(), e);
        }
        _log.info("Successfully run pipeline");
    }

    private static void runTopicEngine(String[] args, String path) {
        boolean getRules = true;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "german":
                    Configuration.setLang(Configuration.GERMAN);
                    break;
                case "english":
                    Configuration.setLang(Configuration.ENGLISH);
                    break;
                case "-inDir":
                    Configuration.setFilesDir(args[i + 1]);
                    break;
                case "-outDir":
                    Configuration.setOutputDir(args[i + 1]);
                    break;
                case "-rule":
                    Configuration.setRuleFile(new File(args[i + 1]));
                    getRules = false;
                    break;
                case "-toFile":
                    topicAnnotator = true;
                    break;
                case "inFile":
                    // ??
                case "-help":
                    System.out
                            .println("Help: java -jar Annotator.jar -topic <Language> -rules <RuleFile> -inDir <InputDir> -outDir <OutputDir> [-toFile]");
                    System.exit(-1);
                    return;

            }
        }

        if(getRules)
            Configuration.retrieveRuleFiles(path + "/" + Configuration.CONFIGDIR);
        try {
           pipeline.runTopicEngineOnly();
            _log.info("Successfully run pipeline");
        } catch (AnnotatorConfigurationException
                | ResourceInitializationException e) {
            _log.error("Parsing could not be completed, due to internal error",
                    e.getMessage(), e);
        }


    }

    private static void runPipeline(String[] args, String path) {
        topicAnnotator = true;
        boolean getRules = true;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "german":
                    Configuration.setLang(Configuration.GERMAN);
                    break;
                case "english":
                    Configuration.setLang(Configuration.ENGLISH);
                    break;
                case "-inDir":
                    Configuration.setFilesDir(args[i + 1]);
                    break;
                case "-outDir":
                    Configuration.setOutputDir(args[i + 1]);
                    break;
                case "-modal":
                    if (args[i + 1].equalsIgnoreCase(Configuration.PCFG)
                            | args[i + 1].equalsIgnoreCase(Configuration.FACTORED)) {
                        Configuration.setModal(args[i + 1]);
                    } else {
                        System.out
                                .println("Please use 'pcfg' or 'factored' as modal description");
                        System.exit(-1);
                        return;
                    }
                    break;
                case "-rule":
                    Configuration.setRuleFile(new File(args[i + 1]));
                    getRules = false;
                    break;
                //case "-toFile":
                //  topicAnnotator = true;
                // break;
                case "-help":
                    System.out
                            .println("Help: java -jar Annotator.jar -all <Language> -rules <RuleFile> -in <InputDir> -out <OutputDir> -m <Modal> [-toFile]");
                    System.exit(-1);
                    return;
            }
        }

        if (getRules)
            Configuration.retrieveRuleFiles(path + "/" + Configuration.CONFIGDIR);

        try {
            pipeline.runPipeline();
            _log.info("Successfully run pipeline");
        } catch (AnnotatorConfigurationException
                | ResourceInitializationException e) {
            _log.error("Parsing could not be completed, due to internal error",
                    e.getMessage(), e);
        }


    }

    private static void runAnnoLabLearner(String[] args) {
        String[] sortedArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-rule":
                    sortedArgs[0] = args[i + 1];
                    break;
                case "-in":
                    sortedArgs[1] = args[i + 1];
                    break;
                case "-out":
                    sortedArgs[2] = args[i + 1];
                    break;
                case "-help":
                    System.out
                            .println("Help: java -jar Annotator.jar -learn -rules <RuleFile> -in <InputFile> -out <OutputFile>");
                    System.exit(-1);
            }
        }

        try {
            Learner.main(sortedArgs);
            _log.info("Successfully run pipeline");
        } catch (Exception e) {
            _log.error("Parsing could not be completed, due to internal error",
                    e.getMessage(), e);
            System.exit(-1);
        }

    }

}

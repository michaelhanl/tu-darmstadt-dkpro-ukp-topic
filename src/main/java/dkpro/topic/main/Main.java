/*
 * Copyright (c) 2013. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package dkpro.topic.main;

import dkpro.topic.utils.ConfigParameters;
import dkpro.topic.utils.NamingParameters;
import org.apache.log4j.PropertyConfigurator;
import org.apache.uima.analysis_engine.annotator.AnnotatorConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Main class
 * @author micha.hanl@gmail.com
 * @date 11/6/13
 */
public class Main {

    private static Logger _log = LoggerFactory.getLogger(Main.class);
    public static boolean DEBUG = false;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("please specify parameters!");
            System.out
                    .println("help: 'java -jar <jarFile> -parser | -topic | -all ... [-toFile]'");
            return;
        }
        try {
            bootstrap();
        } catch (IOException e) {
            System.out.println("Properties could not be loaded due to: '" + e.getMessage()+"'");
            return;
        }

        System.out.println("Running pipeline ... ");
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "-all":
                    _log.info("running entire annotation engine");
                    runPipeline(args);
                    break;
                case "-parser":
                    _log.info("running stanford parser engine");
                    runStanfordParser(args);
                    break;
                case "-learn":
                    // TODO: doesn't work. compilation/rule failure in source code -- fix before submission!
                    _log.info("running annotator learning engine");
                    runAnnoLabLearner(args);
                    break;
                case "-topic":
                    _log.info("running topic engine only");
                    runTopicEngine(args);
                    break;
            }
        }
    }

    private static void runStanfordParser(String[] args) {
        ConfigParameters params = ConfigParameters.Instances.getConfiguration();
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "german":
                    params.setLang(ConfigParameters.GERMAN);
                    break;
                case "english":
                    params.setLang(ConfigParameters.ENGLISH);
                    break;
                case "-indir":
                    params.setInputDir(args[i + 1]);
                    break;
                case "-outdir":
                    params.setOutputDir(args[i + 1]);
                    break;
                case "-model":
                    if (args[i + 1].equalsIgnoreCase(ConfigParameters.PCFG)
                            | args[i + 1].equalsIgnoreCase(ConfigParameters.FACTORED)) {
                        params.setModel(args[i + 1]);
                        break;
                    } else {
                        System.out
                                .println("Please use 'pcfg' or 'factored' as model description");
                        return;
                    }
                case "-help":
                    System.out
                            .println("Help: java -jar Annotator.jar -parser <Language> -rules <RuleFile> -in <InputDir> -out <OutputDir> -m <Model> [-toFile]");
                    return;

                case "-debug":
                    DEBUG = true;
                return;
            }
        }

        try {
            ParsingPipeline.runStanfordParser(params);
            _log.info("Successfully run pipeline");
        } catch (ResourceInitializationException e) {
            _log.error("Parsing could not be completed, due to internal error",
                    e.getMessage(), e);
        }
    }

    private static void runTopicEngine(String[] args) {
        ConfigParameters params = ConfigParameters.Instances.getConfiguration();
        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "german":
                    params.setLang(ConfigParameters.GERMAN);
                    break;
                case "english":
                    params.setLang(ConfigParameters.ENGLISH);
                    break;
                case "-indir":
                    params.setInputDir(args[i + 1]);
                    break;
                case "-outdir":
                    params.setOutputDir(args[i + 1]);
                    break;
                case "-rule":
                    params.setRuleFile(new File(args[i + 1]));
                    break;
                case "-help":
                    System.out
                            .println("Help: java -jar Annotator.jar -topic <Language> -rules <RuleFile> -inDir <InputDir> -outDir <OutputDir> [-toFile]");
                    System.exit(-1);
                    return;

            }
        }

        try {
            ParsingPipeline.runTopicEngineOnly(params);
            _log.info("Successfully run pipeline");
        } catch (AnnotatorConfigurationException
                | ResourceInitializationException e) {
            _log.error("Parsing could not be completed, due to internal error",
                    e.getMessage(), e);
        }


    }

    private static void runPipeline(String[] args) {
        ConfigParameters params = ConfigParameters.Instances.getConfiguration();

        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "german":
                    params.setLang(ConfigParameters.GERMAN);
                    break;
                case "english":
                    params.setLang(ConfigParameters.ENGLISH);
                    break;
                case "-indir":
                    params.setInputDir(args[i + 1]);
                    break;
                case "-outdir":
                    params.setOutputDir(args[i + 1]);
                    break;
                case "-model":
                    if (args[i + 1].equalsIgnoreCase(ConfigParameters.PCFG)
                            | args[i + 1].equalsIgnoreCase(ConfigParameters.FACTORED)) {
                        params.setModel(args[i + 1]);
                    } else {
                        System.out
                                .println("Please use 'pcfg' or 'factored' as model description");
                        return;
                    }
                    break;
                case "-rule":
                    params.setRuleFile(new File(args[i + 1]));
                    break;
                case "-help":
                    System.out
                            .println("Help: java -jar Annotator.jar -all <Language> -rules <RuleFile> -in <InputDir> -out <OutputDir> -m <Model> [-toFile]");
                    System.exit(-1);
                    return;
            }
        }
        try {
            ParsingPipeline.runFullInterpreter(params);
            _log.info("Successfully run pipeline");
        } catch (ResourceInitializationException e) {
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
                case "-indir":
                    sortedArgs[1] = args[i + 1];
                    break;
                case "-outdir":
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

    public static String bootstrap() throws IOException {
        _log.info("loading properties");
        File f = new File(System.getProperty("java.class.path"));
        String path = f.getAbsoluteFile().getParentFile().getAbsolutePath();
        Properties props = ConfigParameters.Instances.loadProperties(ConfigParameters.LOG4J);

        Properties names = ConfigParameters.Instances.loadProperties(NamingParameters.FILENAME);
        ConfigParameters.Instances.setNamingParameters(names);


        //InputStream in;
//        try {
//            if (new File(s + ConfigParameters.LOG4J).exists()) {
//                in = new FileInputStream(s + ConfigParameters.LOG4J);
//                System.out.println("Loading Logger from file path: "+s);
//            } else {
//                in = NamingParameters.class.getClassLoader()
//                        .getResourceAsStream(ConfigParameters.LOG4J);
//                System.out.println("Loading Logger from class path");
//            }
//            props.load(in);
//        } catch(IOException e){
//            e.printStackTrace();
//        }
        //schema = s + "schema.xsd";
        PropertyConfigurator.configure(props);
        //NamingParameters.loadConfigurationProperties(path);
        return path;
    }

}

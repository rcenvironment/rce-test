/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.optimizer.algorithms;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.OS;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.rcenvironment.commons.TempFileUtils;
import de.rcenvironment.commons.executor.LocalApacheCommandLineExecutor;
import de.rcenvironment.rce.component.ComponentException;
import de.rcenvironment.rce.component.ComponentInstanceInformation;
import de.rcenvironment.rce.component.ConsoleRow;
import de.rcenvironment.rce.component.ConsoleRow.Type;
import de.rcenvironment.rce.components.optimizer.OptimizerComponent;
import de.rcenvironment.rce.components.optimizer.commons.CommonBundleClasspathStub;
import de.rcenvironment.rce.components.optimizer.commons.DakotaMethodConstants;
import de.rcenvironment.rce.components.optimizer.commons.OptimizerComponentConstants;
import de.rcenvironment.rce.notification.DistributedNotificationService;
/**
 * This class provides everything for running the dakota optimizer blackbox.
 *  
 *  @author Sascha Zur
 */
public class DakotaAlgorithm implements Runnable{


    /** */
    public static final int BUFFERLENGTH = 1024;

    private static final Log LOGGER = LogFactory.getLog(DakotaAlgorithm.class);

    private static final int SLEEPTIME = 10;

    private static final int SOCKET_TIMEOUT = 100000;

    private static final String STRING_ENDING_CONSTANT = "\n";

    private String subfolder = "plugins"  + File.separator;

    private String tempFolder = null;

    private String componentName;

    private Map <String, Double> outputValues;

    private ComponentInstanceInformation ci;

    private String myProblemName;   

    private String[] clientMsg = null;

    private boolean stop = false;

    private int port;

    private String algorithm;

    private boolean dakotaFinished;

    private Thread serverThread;

    private LocalApacheCommandLineExecutor executor;

    private OptimizerComponent parent;

    private DistributedNotificationService notificationService;

    private File tempDir;

    public DakotaAlgorithm(String algorithm, Map<String, Double> outputValues2, 
        Map<String, Class<? extends Serializable>> input, ComponentInstanceInformation ci, 
        DistributedNotificationService notificationService, OptimizerComponent parent) throws IOException, ComponentException{
        stop = false;
        this.notificationService = notificationService;
        this.componentName = ci.getIdentifier();
        this.outputValues = outputValues2;
        this.ci = ci;
        myProblemName =  componentName + ".in";
        this.port = parent.port;
        this.algorithm = algorithm;
        dakotaFinished = false;
        this.parent = parent;
        subfolder = copyFiles();
        createDakotaInputFile(input, port);
    }

    private String copyFiles() throws IOException, ComponentException {
        tempDir = TempFileUtils.getDefaultInstance().createManagedTempDir();
        if (tempFolder == null){

            // the fragment bundle with the binaries should not have this executor bundle as host
            // because of the build process. Instead, the Optimizer.common bundle is host and so 
            // the classpath of common + fragment are merged. For getting the resources stream a
            // class from the common bundle (here the CommonBundleClasspathStub) is needed.
            // The same is for the dakota.exe below.
            InputStream jarInput = DakotaAlgorithm.class.getResourceAsStream(
                "/resources/de.rcenvironment.components.optimizer.simulator.jar");

            File jar = new File(tempDir + File.separator + "de.rcenvironment.components.optimizer.simulator.jar");
            OutputStream out = new FileOutputStream(jar);
            jar.createNewFile();
            int len;
            byte[] buf = new byte[BUFFERLENGTH];
            while ((len = jarInput.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            out.close();
            jar.setExecutable(true);
            
            File dakota;
            if (OS.isFamilyWindows()){
                dakota = new File(tempDir + File.separator + "dakota.exe");
            } else if (OS.isFamilyUnix()){
                dakota = new File(tempDir + File.separator + "dakota");
            } else {
                dakota = null;
            }
            dakota.createNewFile();
            String dakotaPath = (String) ci.getConfigurationValue(OptimizerComponentConstants.DAKOTAPATH);

            if (dakotaPath != null && checkForDakotaExe(dakotaPath)){
                File source = new File(dakotaPath);
                copy(source, dakota); 
            } else {
                // see comment above
                InputStream dakotaInput;
                if (OS.isFamilyWindows()) {
                    dakotaInput = CommonBundleClasspathStub.class.getResourceAsStream("/resources/binaries/dakota.exe");
                } else if (OS.isFamilyUnix()){
                    dakotaInput = CommonBundleClasspathStub.class.getResourceAsStream("/resources/binaries/dakota");
                } else {
                    dakotaInput = null;
                }
                if (dakotaInput != null){
                    out = new FileOutputStream(dakota);
                    while ((len = dakotaInput.read(buf)) > 0){
                        out.write(buf, 0, len);
                    }
                    out.close();
                } else {
                    throw new ComponentException("Dakota binary not found! Please load optimizer bundle!");
                }
                dakota.setExecutable(true);
            }
            tempFolder = tempDir + File.separator;
        } else {
            return tempFolder;
        }
        return tempDir.getAbsolutePath() + File.separator;
    }

    private boolean checkForDakotaExe(String dakotaPath) {
        File test = new File(dakotaPath);
        return test.exists() 
            && (test.getAbsolutePath().endsWith("dakota.exe") || test.getAbsolutePath().endsWith("dakota"));
    }

    private void copy(File source, File destination) {
        try {
            FileInputStream fileInputStream = new FileInputStream(source);
            FileOutputStream fileOutputStream = new FileOutputStream(destination);
            FileChannel inputChannel = fileInputStream.getChannel();
            FileChannel outputChannel = fileOutputStream.getChannel();
            transfer(inputChannel, outputChannel, source.length(), true);
            fileInputStream.close();
            fileOutputStream.close();
            destination.setLastModified(source.lastModified());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
    private void transfer(FileChannel fileChannel, ByteChannel byteChannel, long lengthInBytes, boolean verbose)
        throws IOException {
        long overallBytesTransfered = 0L;
        while (overallBytesTransfered < lengthInBytes) {
            long bytesTransfered = 0L;
            bytesTransfered = fileChannel.transferTo(
                overallBytesTransfered, Math.min(BUFFERLENGTH * BUFFERLENGTH, 
                    lengthInBytes - overallBytesTransfered), byteChannel);
            overallBytesTransfered += bytesTransfered;

        }


    }

    private void createDakotaInputFile(Map<String, Class<? extends Serializable>> input,  int port2){
        try {
            createScript();
            File f = new File(subfolder + myProblemName);
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.append("strategy,\n"
                + "\n"
                + "single_method\n"
                + "\n"
                + "tabular_graphics_data\n"
                + "method,\n"); 
            addAlgorithms(fw);
            addMethodIndependentControls(fw);
            addMethodDependentControls(fw);

            fw.append("model,\n"
                + " single\n"
                + "variables,\n");

            //add Vars
            String t = "\t";
            fw.append(" continuous_design = " + outputValues.size() + "\n"
                + "   cdv_initial_point   ");
            for (String key : outputValues.keySet()){
                fw.append(t + outputValues.get(key));
            }

            fw.append("\n   cdv_lower_bounds");
            for (String key : outputValues.keySet()){
                fw.append(t + ci.getOutputMetaData(key).get(OptimizerComponentConstants.META_LOWERBOUND));
            }

            fw.append("\n   cdv_upper_bounds");

            for  (String key : outputValues.keySet()){
                fw.append(t + ci.getOutputMetaData(key).get(OptimizerComponentConstants.META_UPPERBOUND));
            }

            fw.append("\n   cdv_descriptor");
            for (String key : outputValues.keySet()){
                fw.append("\t'" + key + "'");
            }

            fw.append("\n"
                + "interface,\n"
                + " system\n");
            if (OS.isFamilyWindows()){
                fw.append("   analysis_driver = 'dakotaBlackBox.bat'\n");  // je nach OS 
            } else if (OS.isFamilyUnix()){
                fw.append("   analysis_driver = 'dakotaBlackBox.sh'\n");  // je nach OS 
            }
            fw.append("   parameters_file = 'params.in'\n"
                + "   results_file    = 'results.out'\n"

                + "   work_directory directory_tag  named '" + myProblemName + "workdir'\n"
                //                + "   deactivate restart file\n"
                //                                + "   file_save  directory_save\n"   //don't delete working directories
                + "\n"
                + "responses,\n");
            if (countInput(input) == 1){
                fw.append(" num_objective_functions = 1\n");
            } else {
                fw.append(" num_objective_functions = " + countInput(input) + "\n"
                    + " multi_objective_weights = ");
                for (String key : input.keySet()){
                    if ((Integer) ci.getInputMetaData(key).get(OptimizerComponentConstants.META_TYPE) 
                        == OptimizerComponentConstants.PANE_INPUT){
                        fw.append("" + ci.getInputMetaData(key).get(OptimizerComponentConstants.META_WEIGHT) + " ");
                    }
                }
                fw.append(" \n  ");
            }
            if (countConstraint(input) > 0){
                fw.append(" num_nonlinear_inequality_constraints = " + countConstraint(input) + "\n");
                fw.append("nonlinear_inequality_lower_bounds = ");
                for (String key : input.keySet()){
                    if ((Integer) ci.getInputMetaData(key).get(OptimizerComponentConstants.META_TYPE) 
                        == OptimizerComponentConstants.PANE_CONSTRAINTS){
                        fw.append("" + ci.getInputMetaData(key).get(OptimizerComponentConstants.META_LOWERBOUND) + " ");
                    }
                }
                fw.append("\n");
                fw.append("nonlinear_inequality_upper_bounds = ");
                for (String key : input.keySet()){
                    if ((Integer) ci.getInputMetaData(key).get(OptimizerComponentConstants.META_TYPE) 
                        == OptimizerComponentConstants.PANE_CONSTRAINTS){
                        fw.append("" + ci.getInputMetaData(key).get(OptimizerComponentConstants.META_UPPERBOUND) + " ");
                    }
                }
                fw.append("\n");
            }   
            if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)
                || algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)
                || algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)){
                fw.append("no_gradients\n"
                    + " no_hessians\n");
            } else {
                fw.append(" numerical_gradients\n"
                    + "     method_source dakota        \n"
                    + "   interval_type forward         \n"
                    + "   fd_gradient_step_size = 1.e-4     \n"
                    + " no_hessians\n");
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        try {
            File f = new File(subfolder + myProblemName + ".port");
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.append("" + port);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

    }   
    private void addMethodDependentControls(FileWriter fw) throws IOException {
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
            addMethodDependentControlsQN(fw);
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)){
            addMethodDependentControlsApps(fw);
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)){
            addMethodDependentControlsEA(fw);
        } 
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_MOGA)){
            addMethodDependentControlsMOGA(fw);
        }
        if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
            addMethodDependentControlsCC(fw);
        }
        if (algorithm.equals(OptimizerComponentConstants.DOE_LHS)){
            addMethodDependentControlsDOELHS(fw);
        }
    }

    private void addAlgorithms(FileWriter fw) throws IOException {
        // define algorithm
        if (algorithm != null){
            if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_QUASINEWTON)){
                fw.append("optpp_q_newton   \n");
            } else if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_ASYNCH_PATTERN_SEARCH)){
                fw.append("asynch_pattern_search   \n");
            } else if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_EA)){
                fw.append("coliny_ea   \n");
            } else if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_MOGA)){
                fw.append("moga   \n");
            } else if (algorithm.equals(OptimizerComponentConstants.ALGORITHM_COLINY_COBYLA)){
                fw.append("coliny_cobyla   \n");
            }  else if (algorithm.equals(OptimizerComponentConstants.DOE_LHS)){
                fw.append("dace lhs\n");
            }
        } 
    }

    private void addMethodDependentControlsDOELHS(FileWriter fw) throws IOException {
        fw.append("seed " + ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_SEED) + "\n"
            + "samples " + ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_SAMPLES) + "\n"
            + "symbols " + ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_SYMBOLS) + "\n"
        );
        if (((Boolean) ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_FIXED_SEED) != null 
            && ((Boolean) ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_FIXED_SEED)))){
            fw.append("fixed_seed \n");
        }
        if (((Boolean) ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_MAIN_EFFECTS) != null
            && (Boolean) ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_MAIN_EFFECTS))){
            fw.append("main_effects \n");
        }
        if (((Boolean) ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_QUALITY_METRICS)) != null 
            && ((Boolean) ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_QUALITY_METRICS))){
            fw.append("quality_metrics \n");
        }
        if (((Boolean) ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_VARIANCE_BASED_DECOMP)) != null 
            && ((Boolean) ci.getConfigurationValue(DakotaMethodConstants.DOE_LHS_VARIANCE_BASED_DECOMP))){
            fw.append("variance_based_decomp \n");
        }
    }

    private void addMethodDependentControlsMOGA(FileWriter fw) throws IOException {
        fw.append(" population_size = 50\n"
            + " initialization_type = unique_random \n"
            + " mutation_type = replace_uniform\n" 
            //            + " mutation_scale = 0.15\n"
            + " mutation_rate = 0.08\n"
            //            + " below_limit = 6\n"
            //            + " shrinkage_percentage = 0.9\n"
            + " crossover_type shuffle_random num_parents = 2\n"  
            //            + " multi_point_binary = 1\n"
            //            + " multi_point_parameterized_binary = 1\n"
            //            + " multi_point_real = 1\n"
            //            + " num_parents = 2\n"
            //            + " num_offspring = 2\n"
            + " crossover_rate = 0.8\n"
            + " fitness_type domination_count\n"
            + " niching_type distance 0.1 0.2\n"
            //            + " metric_tracker\n"
            //            + " percent_change = 0.1\n"
            //            + " num_generation = 10\n"

        );
    }

    private void addMethodDependentControlsCC(FileWriter fw) throws IOException {
        double initDelta;
        String initDeltaStr = (String) ci.getConfigurationValue(DakotaMethodConstants.CC_INIT_DELTA);
        if (initDeltaStr == null){
            initDelta = Double.parseDouble(DakotaMethodConstants.CC_INIT_DELTA_DEF);
        } else {
            initDelta = Double.parseDouble(initDeltaStr);
        } 
        double thresDelta;
        String thresDeltaStr = (String) ci.getConfigurationValue(DakotaMethodConstants.CC_THRES_DELTA);
        if (thresDeltaStr == null){
            thresDelta = Double.parseDouble(DakotaMethodConstants.CC_THRES_DELTA_DEF);
        } else {
            thresDelta = Double.parseDouble(thresDeltaStr);
        } 

        fw.append("initial_delta = " + initDelta + "\n" 
            + "threshold_delta = " + thresDelta + "\n"
        );
    }

    private void addMethodDependentControlsEA(FileWriter fw) throws IOException {
        double crossRate;
        String crossRateStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_CROSSOVER_RATE);
        if (crossRateStr == null){
            crossRate = Double.parseDouble(DakotaMethodConstants.EA_CROSSOVER_RATE_DEF);
        } else {
            crossRate = Double.parseDouble(crossRateStr);
        } 
        String crossTypeStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_CROSSOVER_TYPE);
        if (crossTypeStr == null){
            crossTypeStr = DakotaMethodConstants.EA_CROSSOVER_TYPE_DEF;
        } 

        String fitTypeStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_FITNESS_TYPE);
        if (fitTypeStr == null){
            fitTypeStr = DakotaMethodConstants.EA_FITNESS_TYPE_DEF;
        }         

        String initTypeStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_INIT_TYPE);
        if (initTypeStr == null){
            initTypeStr = DakotaMethodConstants.EA_INIT_TYPE_DEF;
        } 

        int mutRange;
        String mutRangeStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_MUT_RANGE);
        if (mutRangeStr == null){
            mutRange = Integer.parseInt(DakotaMethodConstants.EA_MUT_RANGE_DEF);
        } else {
            mutRange = Integer.parseInt(mutRangeStr);
        } 

        double mutRate;
        String mutRateStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_MUT_RATE);
        if (mutRateStr == null){
            mutRate = Double.parseDouble(DakotaMethodConstants.EA_MUT_RATE_DEF);
        } else {
            mutRate = Double.parseDouble(mutRateStr);
        } 
        double mutRatio;
        String mutRatioStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_MUT_RATIO);
        if (mutRatioStr == null){
            mutRatio = Double.parseDouble(DakotaMethodConstants.EA_MUT_RATIO_DEF);
        } else {
            mutRatio = Double.parseDouble(mutRatioStr);
        }
        double mutScale;
        String mutScaleStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_MUT_SCALE);
        if (mutScaleStr == null){
            mutScale = Double.parseDouble(DakotaMethodConstants.EA_MUT_SCALE_DEF);
        } else {
            mutScale = Double.parseDouble(mutScaleStr);
        } 
        String mutTypeStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_MUT_TYPE);
        if (mutTypeStr == null){
            mutTypeStr = DakotaMethodConstants.EA_MUT_TYPE_DEF;
        }

        int newSol;
        String newSolStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_NEW_SOL);
        if (newSolStr == null){
            newSol = Integer.parseInt(DakotaMethodConstants.EA_NEW_SOL_DEF);
        } else {
            newSol = Integer.parseInt(newSolStr);
        }
        int population;
        String populationStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_POPULATION);
        if (populationStr == null){
            population = Integer.parseInt(DakotaMethodConstants.EA_POPULATION_DEF);
        } else {
            population = Integer.parseInt(populationStr);
        }
        String repTypeStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_REPLACEMENT_TYPE);
        if (repTypeStr == null){
            repTypeStr = DakotaMethodConstants.EA_REPLACEMENT_TYPE_DEF;
        } 
        int repTypeStrVal;
        String repTypeStrValStr = (String) ci.getConfigurationValue(DakotaMethodConstants.EA_REPLACEMENT_TYPE_VALUE);
        if (repTypeStrValStr == null){
            repTypeStrVal = Integer.parseInt(DakotaMethodConstants.EA_REPLACEMENT_TYPE_VALUE_DEF);
        } else {
            repTypeStrVal = Integer.parseInt(repTypeStrValStr);
        }
        fw.append(" population_size = " + population + "\n"
            + " initialization_type = " + initTypeStr + "\n"
            + " fitness_type = " + fitTypeStr + "\n"
            + " replacement_type " + repTypeStr + " = " + repTypeStrVal + "\n"
            + " new_solutions_generated = " + newSol + "\n"
            + " crossover_type = " + crossTypeStr + "\n"
            + " crossover_rate = " + crossRate + "\n"
            + " mutation_type = " + mutTypeStr + "\n"
            + " mutation_range = " + mutRange + "\n"
            + " mutation_rate = " + mutRate + "\n"
            //            + " dimension_ratio = " + mutRatio + "\n"
            + " mutation_scale = " + mutScale + "\n");
    }

    private void addMethodDependentControlsApps(FileWriter fw) throws IOException {
        double constPenalty;
        String constPenaltyStr = (String) ci.getConfigurationValue(DakotaMethodConstants.APPS_CONST_PENALTY);
        if (constPenaltyStr == null){
            constPenalty = Double.parseDouble(DakotaMethodConstants.APPS_CONST_PENALTY_DEF);
        } else {
            constPenalty = Double.parseDouble(constPenaltyStr);
        }
        double contrFactor;
        String contrFactorStr = (String) ci.getConfigurationValue(DakotaMethodConstants.APPS_CONTR_FACTOR);
        if (contrFactorStr == null){
            contrFactor = Double.parseDouble(DakotaMethodConstants.APPS_CONTR_FACTOR_DEF);
        } else {
            contrFactor = Double.parseDouble(contrFactorStr);
        }
        double initDelta;
        String initDeltaStr = (String) ci.getConfigurationValue(DakotaMethodConstants.APPS_INIT_DELTA);
        if (initDeltaStr == null){
            initDelta = Double.parseDouble(DakotaMethodConstants.APPS_INIT_DELTA_DEF);
        } else {
            initDelta = Double.parseDouble(initDeltaStr);
        }

        String meritStr = (String) ci.getConfigurationValue(DakotaMethodConstants.APPS_MERIT);
        if (meritStr == null){
            meritStr = DakotaMethodConstants.APPS_MERIT_DEF;
        }
        double smooth;
        String smoothStr = (String) ci.getConfigurationValue(DakotaMethodConstants.APPS_SMOOTH);
        if (smoothStr == null){
            smooth = Double.parseDouble(DakotaMethodConstants.APPS_SMOOTH_DEF);
        } else {
            smooth = Double.parseDouble(smoothStr);
        }
        double solTarget;
        String solTargetStr = (String) ci.getConfigurationValue(DakotaMethodConstants.APPS_SOL_TARGET);
        if (solTargetStr == null || solTargetStr.equals("")){
            solTarget = 0;
        } else {
            solTarget = Double.parseDouble(solTargetStr);
        }

        double tresDelta;
        String tresDeltaStr = (String) ci.getConfigurationValue(DakotaMethodConstants.APPS_TRESDELTA);
        if (tresDeltaStr == null){
            tresDelta = Double.parseDouble(DakotaMethodConstants.APPS_TRESDELTA_DEF);
        } else {
            tresDelta = Double.parseDouble(tresDeltaStr);
        }
        fw.append(" initial_delta = " + initDelta + "\n"
            + " threshold_delta = " + tresDelta + "\n"
            + " contraction_factor = " + contrFactor + "\n"
            + " merit_function = " + meritStr + " \n");

        if (!(solTargetStr == null) && !solTargetStr.equals("")) {
            fw.append(" solution_target = " + solTarget);
        }
        fw.append(" constraint_penalty = " + constPenalty + "\n"
            + " smoothing_factor = " + smooth + "\n"
            + "\n");


    }

    private void addMethodDependentControlsQN(FileWriter fw) throws IOException{
        double maxStep;
        String maxStepStr = (String) ci.getConfigurationValue(DakotaMethodConstants.QN_MAX_STEPSIZE);
        if (maxStepStr == null){
            maxStep = Double.parseDouble(DakotaMethodConstants.QN_MAX_STEPSIZE_DEF);
        } else {
            maxStep = Double.parseDouble(maxStepStr);
        }

        double gradTol;
        String gradTolStr = (String) ci.getConfigurationValue(DakotaMethodConstants.QN_GRAD_TOLERANCE);
        if (gradTolStr == null){
            gradTol = Double.parseDouble(DakotaMethodConstants.QN_GRAD_TOLERANCE_DEF);
        } else {
            gradTol = Double.parseDouble(gradTolStr);
        }

        String searchMethod = (String) ci.getConfigurationValue(DakotaMethodConstants.QN_SEARCH_METHOD);
        if (searchMethod == null){
            searchMethod = DakotaMethodConstants.QN_SEARCH_METHOD_DEF;
        }

        String meritFcn = (String) ci.getConfigurationValue(DakotaMethodConstants.QN_MERIT_FCN);
        if (meritFcn == null){
            meritFcn = DakotaMethodConstants.QN_MERIT_FCN_DEF;
        }

        String centPath = (String) ci.getConfigurationValue(DakotaMethodConstants.QN_CENTRAL_PATH);
        if (centPath == null){
            centPath = DakotaMethodConstants.QN_CENTRAL_PATH_DEF;
        }

        double centPar;
        String centParStr = (String) ci.getConfigurationValue(DakotaMethodConstants.QN_CENTERING_PARAMETER);
        if (centParStr == null){
            centPar = Double.parseDouble(DakotaMethodConstants.QN_CENTERING_PARAMETER_DEF);
        } else {
            centPar = Double.parseDouble(centParStr);
        }

        double stepLength;
        String stepLengthStr = (String) ci.getConfigurationValue(DakotaMethodConstants.QN_STEP_TO_BOUND);
        if (stepLengthStr == null){
            stepLength = Double.parseDouble(DakotaMethodConstants.QN_STEP_TO_BOUND_DEF);
        } else {
            stepLength = Double.parseDouble(stepLengthStr);
        }

        fw.append(" search_method = " + searchMethod + "\n"
            + " max_step = " + maxStep + "\n"
            + " gradient_tolerance = " + gradTol + "\n"
            + " merit_function = \"" + meritFcn + "\"\n"
            + " central_path = \"" + centPath + "\"\n"
            + " steplength_to_boundary= " + stepLength + "\n"
            + " centering_parameter = " + centPar + "\n"
            + " \n ");
    }

    private void addMethodIndependentControls(FileWriter fw) throws IOException {
        double eps;
        double epsConst;
        int maxIterations;
        int maxFuncEvaluations;

        // read everything
        String epsStr = (String) ci.getConfigurationValue(OptimizerComponentConstants.TOLERANCE);
        if (epsStr == null){
            eps = Double.parseDouble(OptimizerComponentConstants.TOLERANCE_DEFAULT);
        } else {
            eps = Double.parseDouble(epsStr);  
        }
        String epsConstStr = (String) ci.getConfigurationValue(OptimizerComponentConstants.CONSTRAINTTOLERANCE);
        if (epsConstStr == null){
            epsConst = Double.parseDouble(OptimizerComponentConstants.CONSTTOLERANCE_DEFAULT);
        } else {
            epsConst = Double.parseDouble(epsConstStr);  
        }
        String iterationsStr = (String) ci.getConfigurationValue(OptimizerComponentConstants.ITERATIONS);
        if (iterationsStr == null){
            maxIterations = Integer.parseInt(OptimizerComponentConstants.ITERATIONS_DEFAULT);
        } else {
            maxIterations = Integer.parseInt(iterationsStr);  
        }       

        String maxFuncEvaluationsStr = (String) ci.getConfigurationValue(OptimizerComponentConstants.FUNCEVAL);        
        if (maxFuncEvaluationsStr == null){
            maxFuncEvaluations = Integer.parseInt(OptimizerComponentConstants.MAXFUNC_DEFAULT);
        } else {
            maxFuncEvaluations = Integer.parseInt(maxFuncEvaluationsStr);  
        }    


        fw.append(" max_iterations = " + maxIterations + "\n"
            + " max_function_evaluations = " + maxFuncEvaluations + "\n"
            + " convergence_tolerance = " + eps + "\n"
            + " constraint_tolerance = " + epsConst + "\n"
            //            //                + " solution_target = 0 \n"
            + " \n ");

    }

    private void createScript() throws IOException {
        if (OS.isFamilyWindows()){
            // create bat file
            File bat = new File(subfolder + "dakotaBlackBox.bat");
            FileWriter fw = new FileWriter(bat);
            fw.append("\"" + System.getProperty("java.home") + File.separator + "bin" 
                + File.separator + "javaw.exe\" -jar " + subfolder 
                + "de.rcenvironment.components.optimizer.simulator.jar \"%CD%\" \"%1\""); 
            fw.flush();
            fw.close();


        } else if (OS.isFamilyUnix()){
            File sh = new File(subfolder + "dakotaBlackBox.sh");
            if (!sh.exists()){
                FileWriter fw = new FileWriter(sh);
                fw.append("#!/usr/bin/sh \n" 
                    + "java -jar `pwd`/../de.rcenvironment.components.optimizer.simulator.jar `pwd` $1 \n"); 
                fw.append("echo $? \n");
                fw.flush();
                fw.close();
                sh.setExecutable(true);
            }
        }
        
    }

    private int countInput(Map<String, Class<? extends Serializable>> input) {
        int result = 0;
        for (String key : input.keySet()){
            if ((Integer) ci.getInputMetaData(key).get(OptimizerComponentConstants.META_TYPE) 
                == OptimizerComponentConstants.PANE_INPUT){
                result++;
            }
        }
        return result;
    }
    private int countConstraint(Map<String, Class<? extends Serializable>> input) {
        int result = 0;
        for (String key : input.keySet()){
            if ((Integer) ci.getInputMetaData(key).get(OptimizerComponentConstants.META_TYPE) 
                == OptimizerComponentConstants.PANE_CONSTRAINTS){
                result++;
            }
        }
        return result;
    }


    /**
     * Starts the Dakota thread.
     * 
     */
    public void startDakota(){
        try {

            String command;
            if (OS.isFamilyWindows()){
                command = subfolder + "dakota.exe -input " + myProblemName;
            } else if (OS.isFamilyUnix()) {
                command = "cd " + subfolder + " && ./dakota -input " + myProblemName;
            } else {
                command = "";
            }
            executor = new LocalApacheCommandLineExecutor(new File(subfolder));
            executor.start(command);
            startWorkflowConsoleLogginThread(executor.getStdout(), ConsoleRow.Type.STDOUT);
            startWorkflowConsoleLogginThread(executor.getStderr(), ConsoleRow.Type.STDERR);
            try {
                executor.waitForTermination();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
            dakotaFinished = true;

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        try {
            Thread.sleep(SLEEPTIME);
        } catch (InterruptedException e) {
            LOGGER.error("DakotaAlgorithm: InterruptedException " + e.getMessage()); 
        }
    }
    private void startWorkflowConsoleLogginThread(final InputStream inputStream, final Type type) {
        new Thread(){
            private ByteArrayOutputStream baos = new ByteArrayOutputStream();

            public void run() {
                int b;
                try {
                    while ((b = inputStream.read()) != 0 - 1){
                        baos.write(b);
                        if (baos .toString().contains(STRING_ENDING_CONSTANT)) {
                            String line = baos.toString();
                            line = StringUtils.removeEndIgnoreCase(line, STRING_ENDING_CONSTANT);
                            if (StringUtils.isNotBlank(line)) {
                                notificationService.send(ci.getIdentifier() + ConsoleRow.NOTIFICATION_SUFFIX,
                                    new ConsoleRow(ci.getComponentContextName(),
                                        ci.getName(),
                                        type,
                                        line));
                            }
                            baos.reset();
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            };
        }.start();   
    }

    /**
     * 
     * @param myParent : Optimizercomponent that owns this instance
     * @param firstMessage : first message from dakota
     * @param inputVariables : all inputs
     * @param constraintVariables : all constraints
     * @param outputValueMap : the results from dakota
     */

    public void runStep(OptimizerComponent myParent, String[] firstMessage, Map<String, Double> inputVariables, 
        Map<String, Double> constraintVariables,  Map<String, Double> outputValueMap){
        if (clientMsg == null){
            clientMsg = firstMessage;
        }
        try {
            if (!stop){
                printOutputFile(inputVariables, constraintVariables);
                sendMessageToDakotaClient(myParent);
                try {
                    serverThread = null;
                    if (!myParent.dakotaThreadInterrupted){
                        serverThread = runNewServer(myParent);
                    }

                    // Wait for client to connect or termination of dakota thread in parent
                    while (myParent.client == null && !dakotaFinished){
                        try {
                            Thread.sleep(SLEEPTIME);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                    if (serverThread != null){
                        serverThread.interrupt();
                    }

                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
                if (!myParent.dakotaThreadInterrupted && !dakotaFinished){
                    readMessageFromClient(myParent);
                    writeNewOutputForOptimizer(outputValueMap);
                } 
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }


    /**
     * 
     * Reads the output from a dakota file based on the dakota output file format. 
     * 
     * @param outputValueMap
     * @throws IOException
     */
    private void writeNewOutputForOptimizer(Map<String, Double> outputValueMap) throws IOException {
        BufferedReader fr = new BufferedReader(new FileReader(new File(clientMsg[0] + File.separator + clientMsg[1])));
        String[] firstLine =  fr.readLine().split(" ");
        int varCount = 0;
        for (String s : firstLine){
            try {
                varCount = Integer.parseInt(s);
            } catch (NumberFormatException e){
                e.getCause(); 
            }
        }
        Map <String, Double> newOutput = new HashMap<String, Double>();
        for (int i = 0; i < varCount ; i++){
            String x = fr.readLine();
            String[] xStrg = x.split(" ");

            // Search for first not empty field
            int j = 0;
            while (xStrg[j].isEmpty()){
                j++;
            }
            newOutput.put(xStrg[j + 1], Double.parseDouble(xStrg[j]));

        }        
        fr.close();
        for (String key : outputValueMap.keySet()){
            outputValueMap.put(key, newOutput.get(key));   
        }

    }

    private void readMessageFromClient(OptimizerComponent myParent) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(myParent.client.getInputStream()));
        char[] buffer = new char[BUFFERLENGTH];
        int anzahlZeichen = bufferedReader.read(buffer, 0, BUFFERLENGTH);
        String nachricht = new String(buffer, 0, anzahlZeichen);
        clientMsg = nachricht.split("&&");


    }

    private Thread runNewServer(final OptimizerComponent myParent) throws IOException {
        myParent.serverSocket = new ServerSocket(myParent.port);
        myParent.serverSocket.setSoTimeout(SOCKET_TIMEOUT);
        Thread newServerThread = new Thread(){
            public void run(){
                try {
                    myParent.client = myParent.serverSocket.accept();
                } catch (IOException e) {
                    if (dakotaFinished){
                        LOGGER.debug("Socket closed because Dakota finished");
                    } else {
                        LOGGER.error("DakotaAlgorithm: IOException at server accept: " + e.getMessage() +  e.getStackTrace());
                    }
                }
            }
        };
        newServerThread.start();
        return newServerThread;
    }

    /**
     * 
     * Sends a message to the connected dakota client to terminate it.
     * 
     * @param myParent : Component that controls this instance
     * @throws IOException : No connection
     */
    public void sendMessageToDakotaClient(OptimizerComponent myParent) throws IOException {
        if (myParent != null && myParent.client != null){
            PrintWriter printWriter =
                new PrintWriter(
                    new OutputStreamWriter(
                        myParent.client.getOutputStream()));
            printWriter.print("Close");
            printWriter.flush();
            myParent.client.close();
            myParent.client = null;
            myParent.serverSocket.close();        
        }
    }

    private void printOutputFile(Map<String, Double> inputVariables, Map<String, Double> constraintVariables) throws IOException {
        File fo = new File(clientMsg[0] + File.separatorChar + "results.out");
        fo.createNewFile();
        LOGGER.debug(fo.getAbsolutePath());
        FileWriter fw2 = new FileWriter(fo);

        for (String key : inputVariables.keySet()){
            if ((Integer) ci.getInputMetaData(key).get(OptimizerComponentConstants.META_GOAL) == 1){ // Maximize

                // Dakota only minimizes functions so for maximizing you need to minimize -f(x)
                // see Dakota User Manuel Sec. 7.2.4
                fw2.append((0 - 1) * inputVariables.get(key) + " ");                      
            } else {
                fw2.append("" + inputVariables.get(key) + " ");                
            }
        }

        for (String key : constraintVariables.keySet()){
            fw2.append("" + constraintVariables.get(key) + " ");
        }
        fw2.flush();
        fw2.close();
    }
    /**
     * Stops everything.
     */
    public void stop(){
        try {
            sendMessageToDakotaClient(parent);
        } catch (IOException e) {
            LOGGER.warn("Could not send message to Dakota client", e);
        }
        stop = true;

    }

    /**
     * Gets rid of all tmp files.
     */
    public void dispose(){
        try {
            TempFileUtils.getDefaultInstance().disposeManagedTempDirOrFile(tempDir);
        } catch (IOException e) {
            LOGGER.warn("Failed to dispose DakotaProblem", e);
        }
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        startDakota();        
    }


    public boolean isDakotaFinished() {
        return dakotaFinished;
    }


    public void setDakotaFinished(boolean dakotaFinished) {
        this.dakotaFinished = dakotaFinished;
    }
}

/*
 * Copyright (C) 2010-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.rce.components.optimizer.commons;

/**
 * Constants shared by GUI and component.
 *
 * @author Sascha Zur
 */
public final class OptimizerComponentConstants {

    /** Component id (=package name). */
    public static final String COMPONENT_ID = "de.rcenvironment.rce.components.optimizer";

    

    /** Suffix used for publishing Parametric Study notifications. */
    public static final String NOTIFICATION_SUFFIX = ":rce.component.optimizer";
    
 
    /** Configuration key denoting which algorithm should be used. */
    public static final String ALGORITHM = "algorithm";
     
    /** Configuration key denoting which algorithm should be used. */
    public static final String DAKOTAPATH = "dakotapath";
    
    /** Pane selection. */
    public static final int PANE_INPUT = 0;
    
    /** Pane selection. */
    public static final int PANE_OUTPUT = 1;
    
    /** Pane selection. */
    public static final int PANE_CONSTRAINTS = 2;
    
    /** Property key variable lower bound. */
    public static final String META_PACK = "pack";

    /** Property key for startvalue. */
    public static final String META_STARTVALUE = "startValue";

    /** Property key for variable type. */
    public static final String META_TYPE = "type"; // refers to PANE_ number
    
    /** Property key for weight. */
    public static final String META_WEIGHT = "weight";

    /** Property key for goal. */
    public static final String META_GOAL = "goal";

    /** Constant. */
    public static final String META_LOWERBOUND = "lower";
    /** Constant. */
    public static final String META_UPPERBOUND = "upper";
    /** Constant. */
    public static final String META_SOLVEFOR = "solve";

    /** Constant. */
    public static final String ALGORITHM_QUASINEWTON = "Quasi-Newton method";

    /** Constant. */
    public static final String ALGORITHM_ASYNCH_PATTERN_SEARCH = "HOPSPACK Asynch Pattern Search";
    /** Constant. */
    public static final String ALGORITHM_COLINY_EA = "Coliny Evolutionary Algorithm";
  
    /** Constant. */
    public static final String ALGORITHM_MOGA = "MOGA - Multi Objective Genetic Algorithm";
    /** Constant. */

    public static final String ALGORITHM_COLINY_COBYLA = "COBYLA - Constrained Optimization BY Linear Approximations";
    /** Constant. */

    public static final String DOE_LHS = "Latin Hypercube Sampling";
    /** Constant. */
    public static final String DOE_MONTE = "Quasi-Monte Carlo sequences of Halton";


    /** Constant. */
    public static final String TOLERANCE_DEFAULT = ((Double) Math.pow(10, 0 - 5)).toString();
    /** Constant. */
    public static final String CONSTTOLERANCE_DEFAULT = ((Double) Math.pow(10, 0 - 5)).toString();
    /** Constant. */
    public static final String ITERATIONS_DEFAULT = "100";
    /** Constant. */
    public static final String MAXFUNC_DEFAULT = "1000";


    /**  Constant. */
    public static final String TOLERANCE = "tolerance";
    /** Constant. */
    public static final String CONSTRAINTTOLERANCE =  "constTolerance";
    /** Constant. */
    public static final String FUNCEVAL = "funceval";
    /** Configuration key denoting which algorithm should be used. */
    public static final String ITERATIONS = "iterations";







    /**
     * Hide the constructor.
     */
    private OptimizerComponentConstants() {}

}

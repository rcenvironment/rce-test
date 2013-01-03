/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.optimizer.commons;

import java.io.Serializable;

/**
 * Class to transport method dependend controls for dakota.
 *
 * @author Sascha Zur
 */
public class DakotaProperties implements Serializable{

    /***/
    public Serializable qnMaxStep;
    /***/

    public Serializable qnGradTolerance;
    /***/

    public Serializable qnCentPar;    
    /***/

    public Serializable qnCentPath;
    /***/

    public Serializable qnMeritFcn;
    /***/

    public Serializable qnSearchMet;
    /***/

    public Serializable qnStepToBound;
   
    
    /***/
    public Serializable appsConstrPenalty;
    /***/
    public Serializable appsContrFactor;
    /***/
    public Serializable appsInitDelta;
    /***/
    public Serializable appsSmooth;
    /***/
    public Serializable appsSolTarget;
    /***/
    public Serializable appsTresDelta;
    /***/
    public Serializable appsMeritFcn;
    /***/
    public Serializable eaCrossoverType;
    /***/
    public Serializable eaCrossRate;
    /***/
    public Serializable eaFitnessType;
    /***/
    public Serializable eaInitType;
    /***/
    public Serializable eaMutRange;
    /***/
    public Serializable eaMutRate;
    /***/
    public Serializable eaMutRatio;
    /***/
    public Serializable eaMutScale;
    /***/
    public Serializable eaMutType;
    /***/
    public Serializable eaNewSol;
    /***/
    public Serializable eaPopulation;
    /***/
    public Serializable eaReplacementType;
    /***/
    public Serializable eaReplacementTypeValue;
    /***/
    public Serializable ccInitDelta;
    /***/
    public Serializable ccThresDelta;
    /***/
    public Serializable doeLHSSeed;
    /***/
    public Serializable doeLHSFixedSeed;
    /***/
    public Serializable doeLHSSamples;
    /***/
    public Serializable doeLHSSymbols;
    /***/
    public Serializable doeLHSQualityMetrics;
    /***/
    public Serializable doeLHSMainEffects;
    /***/
    public Serializable doeLHSVarianceBasedDecomp;

}

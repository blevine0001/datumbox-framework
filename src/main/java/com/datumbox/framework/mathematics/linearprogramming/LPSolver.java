/**
 * Copyright (C) 2013-2015 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.mathematics.linearprogramming;

import java.util.List;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

/**
 * The LPSolver provides an easy way to formulate and solve Linear Programming
 * problems.
 * 
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class LPSolver {
    
    /**
     * The Result class of the LP problem.
     */
    public static class LPResult {
        private Double objectiveValue;
        private double[] variableValues;
        private double[] dualSolution;
        
        /**
         * Protected Constructor.
         * 
         * @param numberOfVariables
         * @param numberOfConstraints 
         */
        protected LPResult(int numberOfVariables, int numberOfConstraints) {
            objectiveValue=null;
            variableValues=new double[numberOfVariables];
            dualSolution=new double[numberOfVariables+numberOfConstraints+1];
        }
        
        /**
         * Getter for the Objective value.
         * 
         * @return 
         */
        public Double getObjectiveValue() {
            return objectiveValue;
        }
        
        /**
         * Setter for the Objective value.
         * 
         * @param objectiveValue 
         */
        public void setObjectiveValue(Double objectiveValue) {
            this.objectiveValue = objectiveValue;
        }
        
        /**
         * Getter for the values of the Variables.
         * 
         * @return 
         */
        public double[] getVariableValues() {
            return variableValues;
        }
        
        /**
         * Setter for the values of the Variables.
         * 
         * @param variableValues 
         */
        public void setVariableValues(double[] variableValues) {
            this.variableValues = variableValues;
        }

        /**
         * Getter for the Dual solution.
         * 
         * @return 
         */
        public double[] getDualSolution() {
            return dualSolution;
        }
        
        /**
         * Setter for the Dual solution.
         * 
         * @param dualSolution 
         */
        public void setDualSolution(double[] dualSolution) {
            this.dualSolution = dualSolution;
        }
    }
    
    /**
     * The LP Constraint is an internal class which stores the the main body of
     * the constraint, the sign and the right value.
     */
    public static class LPConstraint {
        /**
         * The body of the constraint; the left part of the constraint.
         */
        private final double[] contraintBody;
        
        /**
         * The sign of the constraint: LE (1) for <=, EQ (3) for =, GE (2) for >=
         */
        private final int sign;
        
        /**
         * The value of the constraint; the right part of the constraint.
         */
        private final double value;
        
        /**
         * The constructor of LP Constraint.
         * 
         * @param constraintBody    The array with the parameters of the constrain
         * @param sign              The sign of the constrain
         * @param value             The right part value of the constrain
         */
        public LPConstraint(double[] constraintBody, int sign, double value) {
            this.contraintBody=constraintBody;
            this.sign=sign;
            this.value=value;
        }

        /**
         * Getter for the body of the constraint.
         * 
         * @return 
         */
        public double[] getContraintBody() {
            return contraintBody;
        }
        
        /**
         * Getter for the sign of the constraint.
         * 
         * @return 
         */
        public int getSign() {
            return sign;
        }
        
        /**
         * Getter for the value of the constraint.
         * 
         * @return 
         */
        public double getValue() {
            return value;
        }
    }
    
    /**
     * Pads a zero at the beginning of the array. This is required from the library
     * of lpsolve.
     * 
     * @param array
     * @return 
     */
    private static double[] pad1ZeroInfront(double[] array) {
        double[] paddedArray = new double[array.length+1];
        System.arraycopy(array, 0, paddedArray, 1, array.length);
        return paddedArray;
    }
    
    /**
     * Checks whether the status corresponds to a valid solution
     * 
     * @param status    The status id returned by lpsolve
     * @return          Boolean which indicates if the solution is valid
     */
    private static boolean isSolutionValid(int status) {
        return (status == 0) || (status == 1) || (status == 11) || (status == 12);
    }
    
    /**
     * Solves the LP problem and returns the result.
     * 
     * @param linearObjectiveFunction
     * @param linearConstraintsList
     * @param lowBoundsOfVariables
     * @param upBoundsOfVariables
     * @param strictlyIntegerVariables
     * @param scalingMode
     * @return
     * @throws LpSolveException 
     */
    public static LPResult solve(double[] linearObjectiveFunction, List<LPSolver.LPConstraint> linearConstraintsList, double[] lowBoundsOfVariables, double[] upBoundsOfVariables, boolean[] strictlyIntegerVariables, Integer scalingMode) throws LpSolveException {
        //Important note. All the double[] arrays that are passed to the lpsolve
        //lib MUST start from 1. Do not use the 0 index because it is discarded.
        
        
        int n = linearObjectiveFunction.length; //columns/variables
        int m = linearConstraintsList.size();
        
        LPResult result = new LPResult(n,m);
        
        LpSolve solver = LpSolve.makeLp(0, n);
        solver.setVerbose(LpSolve.NEUTRAL); //set verbose level
        solver.setMaxim(); //set the problem to maximization
        solver.setObjFn(pad1ZeroInfront(linearObjectiveFunction));
        
        if(scalingMode!=null) {
            solver.setScaling(scalingMode);
        }
        
        //lower bounds for the variables
        if(lowBoundsOfVariables!=null && n==lowBoundsOfVariables.length) {
            for(int i=0;i<n;++i) {
                solver.setLowbo(i+1, lowBoundsOfVariables[i]); //plus one is required by the lib
            }
        }
        
        //upper bounds for the variables
        if(upBoundsOfVariables!=null && n==upBoundsOfVariables.length) {
            for(int i=0;i<n;++i) {
                solver.setUpbo(i+1, upBoundsOfVariables[i]); //plus one is required by the lib
            }
        }
        
        //set variables that are strictly integers
        if(strictlyIntegerVariables!=null && n==strictlyIntegerVariables.length) {
            for(int i=0;i<n;++i) {
                solver.setInt(i+1, strictlyIntegerVariables[i]); //plus one is required by the lib
            }
        }
        
        for(LPSolver.LPConstraint constraint : linearConstraintsList) {
            solver.addConstraint(pad1ZeroInfront(constraint.getContraintBody()), constraint.getSign(), constraint.getValue());
        }
        
        
        //solve the problem
        int status = solver.solve();
        if(isSolutionValid(status)==false) {
            solver.setScaling(LpSolve.SCALE_NONE); //turn off automatic scaling
            status = solver.solve();
            if(isSolutionValid(status)==false) {
                throw new RuntimeException("LPSolver: "+solver.getStatustext(status));
                //solver.printLp();
            }
        }
                
        
        result.setObjectiveValue((Double) solver.getObjective());
        solver.getVariables(result.getVariableValues());
        solver.getDualSolution(result.getDualSolution());
        
        //delete problem and free the memory
        solver.deleteLp();
        
        return result;
    }
}

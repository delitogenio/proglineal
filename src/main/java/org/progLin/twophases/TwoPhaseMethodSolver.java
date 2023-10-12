package org.progLin.twophases;



import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;

import org.progLin.apachelibmod.SimplexSolver;
import org.progLin.model.LinearConstraintData;
import org.progLin.interfaces.ConstraintCreator;
import org.progLin.interfaces.ContraintCreatorImpl;

import java.util.List;

public class TwoPhaseMethodSolver {
    private int numVariables;
    private double[] objectiveCoefficients;
    private List<LinearConstraintData> constraintDataList;
    private ConstraintCreator constraintCreator;
    private GoalType goalType;

    public TwoPhaseMethodSolver(int numVariables, double[] objectiveCoefficients, List<LinearConstraintData> constraintDataList, GoalType goalType) {
        this.numVariables = numVariables;
        this.objectiveCoefficients = objectiveCoefficients;
        this.constraintDataList = constraintDataList;
        this.constraintCreator = new ContraintCreatorImpl();
        this.goalType = goalType;
    }

    public void solve() throws OptimizationException {
        // Crear una lista para almacenar las restricciones de la Fase 1
        List<LinearConstraint> phase1Constraints = constraintCreator.createConstraints(this.constraintDataList);

        // Resolver la Fase 1 utilizando el método Simplex
        SimplexSolver phase1Solver = new SimplexSolver();
        LinearObjectiveFunction phase1ObjectiveFunction = new LinearObjectiveFunction(new double[numVariables], 0);
        RealPointValuePair phase1Solution = phase1Solver.optimize(
                phase1ObjectiveFunction,
                phase1Constraints,
               this.goalType, true
        );

        // Verificar si la Fase 1 encontró un punto factible
        if (phase1Solution.getValue() > 0) {
            System.out.println("El problema es infactible.");
            return;
        }

        // Crear una lista para almacenar las restricciones de la Fase 2
        List<LinearConstraint> phase2Constraints = constraintCreator.createConstraints(this.constraintDataList);

        // Resolver la Fase 2 utilizando el método Simplex
        SimplexSolver phase2Solver = new SimplexSolver();
        LinearObjectiveFunction phase2ObjectiveFunction = new LinearObjectiveFunction(objectiveCoefficients, 0);
        RealPointValuePair phase2Solution = phase2Solver.optimize(
                phase2ObjectiveFunction,
                phase2Constraints,
                this.goalType,true
        );

        // Mostrar la solución óptima de la Fase 2
        double[] solutionPoint = phase2Solution.getPoint();
        System.out.println("Solución óptima:");
        for (int i = 0; i < numVariables; i++) {
            System.out.println("x" + (i + 1) + " = " + solutionPoint[i]);
        }
        System.out.println("Valor óptimo = " + phase2Solution.getValue());
    }



}

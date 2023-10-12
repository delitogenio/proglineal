package org.progLin.simplex;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.progLin.apachelibmod.SimplexSolver;
import org.progLin.interfaces.ConstraintCreator;
import org.progLin.interfaces.ContraintCreatorImpl;
import org.progLin.model.LinearConstraintData;

import java.util.List;

public class LinearProgrammingSolverMod {
    private int numVariables;
    private double[] objectiveCoefficients;
    private List<LinearConstraintData> constraintDataList;
    private ConstraintCreator constraintCreator;

    private GoalType goalType;

    public LinearProgrammingSolverMod(int numVariables, double[] objectiveCoefficients, List<LinearConstraintData> constraintDataList, GoalType goalType) {
        this.numVariables = numVariables;
        this.objectiveCoefficients = objectiveCoefficients;
        this.constraintDataList = constraintDataList;
        this.constraintCreator = new ContraintCreatorImpl();
        this.goalType = goalType;
    }

    public void solve() throws OptimizationException {
        // Crear un objeto LinearObjectiveFunction para la función objetivo
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(objectiveCoefficients, 0);

        // Crear una lista de restricciones lineales
        List<LinearConstraint> constraints = constraintCreator.createConstraints(this.constraintDataList);

        // Crear un objeto SimplexSolver para resolver el problema
        SimplexSolver solver = new SimplexSolver();

        // Resolver el problema
        RealPointValuePair solution = solver.optimize(objectiveFunction, constraints, this.goalType, true);

        // Mostrar la solución óptima
        double[] solutionPoint = solution.getPoint();
        System.out.println("Solución óptima:");
        for (int i = 0; i < numVariables; i++) {
            System.out.println("x" + (i + 1) + " = " + solutionPoint[i]);
        }
        System.out.println("Valor óptimo = " + solution.getValue());
    }

}

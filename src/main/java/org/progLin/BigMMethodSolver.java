package org.progLin;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.SimplexSolver;
import org.progLin.interfaces.ConstraintCreator;
import org.progLin.interfaces.ContraintCreatorImpl;


import java.util.List;

public class BigMMethodSolver {

    private int numVariables;
    private double[] objectiveCoefficients;
    private List<LinearConstraintData> constraintDataList;

    private final ConstraintCreator constraintCreator;

    public BigMMethodSolver(int numVariables, double[] objectiveCoefficients, List<LinearConstraintData> constraintDataList) {
        this.numVariables = numVariables;
        this.objectiveCoefficients = objectiveCoefficients;
        this.constraintDataList = constraintDataList;
        this.constraintCreator = new ContraintCreatorImpl();
    }

    public void solve() throws OptimizationException {
        // Crear una lista para almacenar las restricciones
        List<LinearConstraint> constraints = constraintCreator.createConstraints(this.constraintDataList);

        // Resolver el problema utilizando el método de M grande (Big M method)
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(objectiveCoefficients, 0);

        // Configurar el límite de iteraciones máximas (por ejemplo, 100)
        solver.setMaxIterations(100);

        RealPointValuePair solution = solver.optimize(
                objectiveFunction,
                constraints,
                GoalType.MAXIMIZE,true
        );

        // Mostrar la solución óptima
        double[] solutionPoint = solution.getPoint();
        System.out.println("Solución óptima:");
        for (int i = 0; i < numVariables; i++) {
            System.out.println("x" + (i + 1) + " = " + solutionPoint[i]);
        }
        System.out.println("Valor óptimo = " + solution.getValue());
    }

}


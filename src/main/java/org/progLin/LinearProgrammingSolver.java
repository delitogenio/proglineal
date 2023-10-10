package org.progLin;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.Relationship;
import org.apache.commons.math.optimization.linear.SimplexSolver;
import org.progLin.interfaces.ConstraintCreator;
import org.progLin.interfaces.ContraintCreatorImpl;

import java.util.ArrayList;
import java.util.List;

public class LinearProgrammingSolver {
    private int numVariables;
    private double[] objectiveCoefficients;
    private List<LinearConstraintData> constraintDataList;
    private ConstraintCreator constraintCreator;

    public LinearProgrammingSolver(int numVariables, double[] objectiveCoefficients, List<LinearConstraintData> constraintDataList) {
        this.numVariables = numVariables;
        this.objectiveCoefficients = objectiveCoefficients;
        this.constraintDataList = constraintDataList;
        this.constraintCreator = new ContraintCreatorImpl();
    }

    public void solve() throws OptimizationException {
        // Crear un objeto LinearObjectiveFunction para la función objetivo
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(objectiveCoefficients, 0);

        // Crear una lista de restricciones lineales
        List<LinearConstraint> constraints = constraintCreator.createConstraints(this.constraintDataList);

        // Crear un objeto SimplexSolver para resolver el problema
        SimplexSolver solver = new SimplexSolver();

        // Resolver el problema
        RealPointValuePair solution = solver.optimize(objectiveFunction, constraints, GoalType.MAXIMIZE, true);

        // Mostrar la solución óptima
        double[] solutionPoint = solution.getPoint();
        System.out.println("Solución óptima:");
        for (int i = 0; i < numVariables; i++) {
            System.out.println("x" + (i + 1) + " = " + solutionPoint[i]);
        }
        System.out.println("Valor óptimo = " + solution.getValue());
    }

    private List<LinearConstraint> createConstraints() {
            List<LinearConstraint> constraints = new ArrayList<>();
        for (LinearConstraintData data : constraintDataList) {
            double[] coefficients = data.getCoefficients();
            String relation = data.getRelation();
            double rhs = data.getRhs();

            Relationship relationship = Relationship.LEQ;
            if (relation.equals("GEQ")) {
                relationship = Relationship.GEQ;
            } else if (relation.equals("EQ")) {
                relationship = Relationship.EQ;
            }

            LinearConstraint constraint = new LinearConstraint(coefficients, relationship, rhs);
            constraints.add(constraint);
        }
        return constraints;
    }
}

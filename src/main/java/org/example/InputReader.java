package org.example;

import org.apache.commons.math.optimization.OptimizationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputReader {
    public static void main(String[] args) throws OptimizationException {
        Scanner scanner = new Scanner(System.in);

        // Solicitar el número de variables
        System.out.print("Ingrese el número de variables: ");
        int numVariables = scanner.nextInt();

        // Solicitar el número de restricciones
        System.out.print("Ingrese el número de restricciones: ");
        int numRestrictions = scanner.nextInt();

        // Crear un arreglo para almacenar los coeficientes de la función objetivo
        double[] objectiveCoefficients = new double[numVariables];

        // Crear una lista para almacenar las restricciones
        List<LinearConstraintData> constraintDataList = new ArrayList<>();

        // Solicitar los coeficientes y si una variable es irrestricta o no
        for (int i = 0; i < numVariables; i++) {
            System.out.print("Coeficiente de x" + (i + 1) + " en la función objetivo: ");
            objectiveCoefficients[i] = scanner.nextDouble();
            System.out.print("¿Es la variable x" + (i + 1) + " irrestricta? (S/N): ");
            boolean unrestricted = scanner.next().equalsIgnoreCase("S");
            constraintDataList.add(new LinearConstraintData(new double[]{1.0}, "GEQ", 0.0, unrestricted));
        }

        // Solicitar los coeficientes y relaciones de las restricciones
        for (int i = 0; i < numRestrictions; i++) {
            System.out.println("Ingrese los coeficientes de la restricción " + (i + 1) + ":");
            double[] coefficients = new double[numVariables];
            for (int j = 0; j < numVariables; j++) {
                System.out.print("Coeficiente de x" + (j + 1) + ": ");
                coefficients[j] = scanner.nextDouble();
            }

            System.out.print("Relación de la restricción (LEQ, GEQ o EQ): ");
            String relation = scanner.next();

            System.out.print("Lado derecho de la restricción: ");
            double rhs = scanner.nextDouble();

            constraintDataList.add(new LinearConstraintData(coefficients, relation, rhs, false));
        }

        // Llamar al solucionador con los datos recopilados
        LinearProgrammingSolver solver = new LinearProgrammingSolver(numVariables, objectiveCoefficients, constraintDataList);
        solver.solve();
    }
}

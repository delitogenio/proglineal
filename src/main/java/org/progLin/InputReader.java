package org.progLin;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.progLin.simplex.LinearProgrammingSolverMod;
import org.progLin.bigM.BigMMethodSolver;
import org.progLin.model.LinearConstraintData;
import org.progLin.simplex.LinearProgrammingSolver;
import org.progLin.twophases.TwoPhaseMethodSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputReader {
    public static void main(String[] args) throws OptimizationException {
        Scanner scanner = new Scanner(System.in);

        // Solicitar al usuario que elija un método de resolución
        System.out.println("Seleccione un método de resolución:");
        System.out.println("1. Método Simplex");
        System.out.println("2. Método de M grande (Big M method)");
        System.out.println("3. Método de dos fases");
        System.out.println("4. Método simplex lib modificada");
        System.out.print("Elija una opción : ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                solveWithSimplex();
                break;
            case 2:
                solveWithBigMMethod();
                break;
            case 3:
                solveWithTwoPhaseMethod();
                break;
            case 4:
                solveWithSimplexMod();
                break;
            default:
                System.out.println("Opción no válida. Saliendo del programa.");
        }

    }

    private static void solveWithSimplex() throws OptimizationException {
        Scanner scanner = new Scanner(System.in);

        GoalType goalType;

        System.out.println("Desea minimizar (1) o maximizar (2)");
        int maxmin = scanner.nextInt();
        if (maxmin == 1){
            goalType = GoalType.MINIMIZE;
        }else goalType = GoalType.MAXIMIZE;

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
        LinearProgrammingSolver solver = new LinearProgrammingSolver(numVariables, objectiveCoefficients, constraintDataList, goalType);
        solver.solve();
    }
    private static void solveWithBigMMethod() throws OptimizationException {
        Scanner scanner = new Scanner(System.in);

        GoalType goalType;

        System.out.println("Desea minimizar (1) o maximizar (2)");
        int maxmin = scanner.nextInt();
        if (maxmin == 1){
            goalType = GoalType.MINIMIZE;
        }else goalType = GoalType.MAXIMIZE;

        // Solicitar al usuario los datos del problema
        System.out.println("Resolviendo con el método de M grande (Big M method)...");
        System.out.print("Ingrese el número de variables: ");
        int numVariables = scanner.nextInt();

        // Crear un arreglo para almacenar los coeficientes de la función objetivo
        double[] objectiveCoefficients = new double[numVariables];

        // Solicitar los coeficientes de la función objetivo
        System.out.println("Ingrese los coeficientes de la función objetivo:");
        for (int i = 0; i < numVariables; i++) {
            System.out.print("Coeficiente de x" + (i + 1) + ": ");
            objectiveCoefficients[i] = scanner.nextDouble();
        }

        // Crear una lista para almacenar las restricciones
        List<LinearConstraintData> constraintDataList = new ArrayList<>();

        // Solicitar los coeficientes y relaciones de las restricciones
        System.out.print("Ingrese el número de restricciones: ");
        int numRestrictions = scanner.nextInt();

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

        // Resolver el problema utilizando el método de M grande (Big M method)
        BigMMethodSolver solver = new BigMMethodSolver(numVariables, objectiveCoefficients, constraintDataList,goalType);
        solver.solve();
    }

   private static void solveWithTwoPhaseMethod() throws OptimizationException {
        Scanner scanner = new Scanner(System.in);

       GoalType goalType;

       System.out.println("Desea minimizar (1) o maximizar (2)");
       int maxmin = scanner.nextInt();
       if (maxmin == 1){
           goalType = GoalType.MINIMIZE;
       }else goalType = GoalType.MAXIMIZE;

        // Solicitar al usuario los datos del problema para el método de las dos fases
        System.out.println("Resolviendo con el método de las dos fases...");
        System.out.print("Ingrese el número de variables originales: ");
        int numVariables = scanner.nextInt();

        // Crear un arreglo para almacenar los coeficientes de la función objetivo
        double[] objectiveCoefficients = new double[numVariables];

        // Solicitar los coeficientes de la función objetivo
        System.out.println("Ingrese los coeficientes de la función objetivo:");
        for (int i = 0; i < numVariables; i++) {
            System.out.print("Coeficiente de x" + (i + 1) + ": ");
            objectiveCoefficients[i] = scanner.nextDouble();
        }

        // Crear una lista para almacenar las restricciones
        List<LinearConstraintData> constraintDataList = new ArrayList<>();

        // Solicitar los coeficientes y relaciones de las restricciones
        System.out.print("Ingrese el número de restricciones: ");
        int numRestrictions = scanner.nextInt();

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

        // Resolver el problema utilizando el método de las dos fases
        TwoPhaseMethodSolver solver = new TwoPhaseMethodSolver(numVariables, objectiveCoefficients, constraintDataList,goalType);
        solver.solve();
    }

    private static void solveWithSimplexMod() throws OptimizationException {
        Scanner scanner = new Scanner(System.in);
        GoalType goalType;

        System.out.println("Desea minimizar (1) o maximizar (2)");
        int maxmin = scanner.nextInt();
        if (maxmin == 1){
            goalType = GoalType.MINIMIZE;
        }else goalType = GoalType.MAXIMIZE;


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
        LinearProgrammingSolverMod solver = new LinearProgrammingSolverMod(numVariables, objectiveCoefficients, constraintDataList,goalType);
        solver.solve();
    }
}

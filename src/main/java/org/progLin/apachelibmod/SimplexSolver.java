package org.progLin.apachelibmod;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.fraction.FractionConversionException;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.AbstractLinearOptimizer;
import org.apache.commons.math.optimization.linear.NoFeasibleSolutionException;
import org.apache.commons.math.optimization.linear.UnboundedSolutionException;
import org.apache.commons.math.util.MathUtils;
import org.progLin.interfaces.printers.PrintTableu;
import org.progLin.interfaces.printers.PrintTableuImpl;

public class SimplexSolver extends AbstractLinearOptimizer {
    private static final double DEFAULT_EPSILON = 1.0E-6;
    protected final double epsilon;

    private PrintTableu printer;

    public SimplexSolver() {
        this(1.0E-6);
    }

    public SimplexSolver(double epsilon) {
        this.epsilon = epsilon;
    }

    private Integer getPivotColumn(SimplexTableau tableau) {
        double minValue = 0.0;
        Integer minPos = null;

        // Encuentra la columna pivote con el valor más negativo en la fila 0 (función objetivo).
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; ++i) {
            if (MathUtils.compareTo(tableau.getEntry(0, i), minValue, this.epsilon) < 0) {
                minValue = tableau.getEntry(0, i);
                minPos = i;
            }
        }

        return minPos;
    }

    private Integer getPivotRow(SimplexTableau tableau, int col) {
        List<Integer> minRatioPositions = new ArrayList();
        double minRatio = Double.MAX_VALUE;

        // Encuentra la fila pivote utilizando la relación del mínimo cociente (ratio).
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); ++i) {
            double rhs = tableau.getEntry(i, tableau.getWidth() - 1);
            double entry = tableau.getEntry(i, col);

            if (MathUtils.compareTo(entry, 0.0, this.epsilon) > 0) {
                double ratio = rhs / entry;

                if (MathUtils.equals(ratio, minRatio, this.epsilon)) {
                    minRatioPositions.add(i);
                } else if (ratio < minRatio) {
                    minRatio = ratio;
                    minRatioPositions = new ArrayList();
                    minRatioPositions.add(i);
                }
            }
        }

        if (minRatioPositions.size() == 0) {
            return null;
        } else {
            if (minRatioPositions.size() > 1) {
                // Maneja casos especiales cuando hay múltiples filas con el mismo cociente mínimo.
                // Utiliza las variables artificiales para resolver empates.
            }

            return (Integer) minRatioPositions.get(0);
        }
    }


    protected void doIteration(SimplexTableau tableau) throws OptimizationException {
        this.incrementIterationsCounter();  // Incrementa el contador de iteraciones.
        this.printer = new PrintTableuImpl();

        System.out.println("Iteracion " + this.getIterations());

        // Encuentra la columna pivote con el valor más negativo en la fila 0 (función objetivo).
        Integer pivotCol = this.getPivotColumn(tableau);
        int printCol = pivotCol + 1;
        System.out.println("La columna que salió es "+ printCol);

        // Encuentra la fila pivote utilizando la relación del mínimo cociente (ratio).
        Integer pivotRow = this.getPivotRow(tableau, pivotCol);
        int printRow = pivotRow + 1;
        System.out.println("La fila que salió es " + printRow);

        if (pivotRow == null) {
            // Si no se puede encontrar una fila pivote (solución no acotada), lanza una excepción.
            throw new UnboundedSolutionException();
        } else {
            double pivotVal = tableau.getEntry(pivotRow, pivotCol);

            // Divide la fila pivote por el valor de la celda pivote para hacer que el valor pivote sea 1.
            tableau.divideRow(pivotRow, pivotVal);

            // Realiza operaciones en otras filas para hacer que las demás celdas en la columna pivote sean 0.
            for (int i = 0; i < tableau.getHeight(); ++i) {
                if (i != pivotRow) {
                    double multiplier = tableau.getEntry(i, pivotCol);
                    // Calcula un múltiplo del valor en la columna pivote para cancelar otras celdas.
                    tableau.subtractRow(i, pivotRow, multiplier);
                    // Resta la fila pivote multiplicada por el múltiplo a la fila actual.
                }
            }
        }
        try {
            this.printer.printTableau(tableau);
        } catch (FractionConversionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void solvePhase1(SimplexTableau tableau) throws OptimizationException {
        this.printer = new PrintTableuImpl();
        if (tableau.getNumArtificialVariables() != 0) {
            // Resuelve la fase 1 del problema simplex si hay variables artificiales.
            while (!tableau.isOptimal()) {
                this.doIteration(tableau);
            }

            if (!MathUtils.equals(tableau.getEntry(0, tableau.getRhsOffset()), 0.0, this.epsilon)) {
                throw new NoFeasibleSolutionException();
            }
        }
    }

    public RealPointValuePair doOptimize() throws OptimizationException {
        SimplexTableau tableau = new SimplexTableau(this.function, this.linearConstraints, this.goal, this.nonNegative, this.epsilon);
        // Crea un objeto SimplexTableau y lo inicializa.

        PrintTableuImpl printer = new PrintTableuImpl();

        try {
            printer.printTableau(tableau);
        } catch (FractionConversionException e) {
            throw new RuntimeException(e);
        }

        // Resuelve la fase 1 (si es necesario) y luego realiza iteraciones hasta encontrar la solución óptima.
        this.solvePhase1(tableau);

        tableau.dropPhase1Objective();

        while (!tableau.isOptimal()) {
            this.doIteration(tableau);
        }

        // Imprime el resultado y devuelve la solución.
        System.out.println("Ultima iteración" + this.getIterations());

        try {
            printer.printTableau(tableau);
        } catch (FractionConversionException e) {
            throw new RuntimeException(e);
        }

        return tableau.getSolution();
    }

}


package org.progLin.apachelibmod;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.util.ArrayList;
import java.util.Iterator;
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

        for(int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; ++i) {
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

        for(int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); ++i) {
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
                Iterator i$ = minRatioPositions.iterator();

                while(i$.hasNext()) {
                    Integer row = (Integer)i$.next();

                    for(int i = 0; i < tableau.getNumArtificialVariables(); ++i) {
                        int column = i + tableau.getArtificialVariableOffset();
                        if (MathUtils.equals(tableau.getEntry(row, column), 1.0, this.epsilon) && row.equals(tableau.getBasicRow(column))) {
                            return row;
                        }
                    }
                }
            }

            return (Integer)minRatioPositions.get(0);
        }
    }

    protected void doIteration(SimplexTableau tableau) throws OptimizationException {
        this.incrementIterationsCounter();
        System.out.println("Doing " + this.getIterations());
        Integer pivotCol = this.getPivotColumn(tableau);
        Integer pivotRow = this.getPivotRow(tableau, pivotCol);
        if (pivotRow == null) {
            throw new UnboundedSolutionException();
        } else {
            double pivotVal = tableau.getEntry(pivotRow, pivotCol);
            tableau.divideRow(pivotRow, pivotVal);

            for(int i = 0; i < tableau.getHeight(); ++i) {
                if (i != pivotRow) {
                    double multiplier = tableau.getEntry(i, pivotCol);
                    tableau.subtractRow(i, pivotRow, multiplier);
                }
            }

        }
        this.printer = new PrintTableuImpl();
        try {
            printer.printTableau(tableau);
        } catch (FractionConversionException e) {
            throw new RuntimeException(e);
        }

    }

    protected void solvePhase1(SimplexTableau tableau) throws OptimizationException {
        if (tableau.getNumArtificialVariables() != 0) {
            while(!tableau.isOptimal()) {
                this.doIteration(tableau);
            }

            if (!MathUtils.equals(tableau.getEntry(0, tableau.getRhsOffset()), 0.0, this.epsilon)) {
                throw new NoFeasibleSolutionException();
            }
        }
    }

    public RealPointValuePair doOptimize() throws OptimizationException {
        SimplexTableau tableau = new SimplexTableau(this.function, this.linearConstraints, this.goal, this.nonNegative, this.epsilon);
        System.out.println("Tablero simplex creado");
        PrintTableuImpl printer = new PrintTableuImpl();
        try {
            printer.printTableau(tableau);
        } catch (FractionConversionException e) {
            throw new RuntimeException(e);
        }
        this.solvePhase1(tableau);
        tableau.dropPhase1Objective();
        while(!tableau.isOptimal()) {
            this.doIteration(tableau);
        }

        return tableau.getSolution();
    }
}


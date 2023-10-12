package org.progLin.apachelibmod;

import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math.optimization.linear.Relationship;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.util.MathUtils;
import org.progLin.interfaces.printers.PrintTableu;
import org.progLin.interfaces.printers.PrintTableuImpl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class SimplexTableau implements Serializable {
    private static final String NEGATIVE_VAR_COLUMN_LABEL = "x-";
    private static final long serialVersionUID = -1369660067587938365L;
    private final LinearObjectiveFunction f;
    private final List<LinearConstraint> constraints;
    private final boolean restrictToNonNegative;
    private final List<String> columnLabels = new ArrayList();
    private transient RealMatrix tableau;
    private final int numDecisionVariables;
    private final int numSlackVariables;
    private int numArtificialVariables;
    private final double epsilon;

    private final PrintTableu printer;

    SimplexTableau(LinearObjectiveFunction f, Collection<LinearConstraint> constraints, GoalType goalType, boolean restrictToNonNegative, double epsilon) {
        this.f = f;
        this.constraints = this.normalizeConstraints(constraints);
        this.restrictToNonNegative = restrictToNonNegative;
        this.epsilon = epsilon;
        this.numDecisionVariables = f.getCoefficients().getDimension() + (restrictToNonNegative ? 0 : 1);
        this.numSlackVariables = this.getConstraintTypeCounts(Relationship.LEQ) + this.getConstraintTypeCounts(Relationship.GEQ);
        this.numArtificialVariables = this.getConstraintTypeCounts(Relationship.EQ) + this.getConstraintTypeCounts(Relationship.GEQ);
        this.tableau = this.createTableau(goalType == GoalType.MAXIMIZE);
        this.printer = new PrintTableuImpl();
        System.out.println("Simplex board created");
        this.initializeColumnLabels();
    }

    protected void initializeColumnLabels() {
        // Agrega la etiqueta "W" si hay 2 funciones objetivo (fase 1).
        if (this.getNumObjectiveFunctions() == 2) {
            this.columnLabels.add("W");
        }

        // Agrega la etiqueta "Z" para la función objetivo.
        this.columnLabels.add("Z");

        // Agrega etiquetas para las variables de decisión originales (x0, x1, x2, ...).
        for (int i = 0; i < this.getOriginalNumDecisionVariables(); ++i) {
            this.columnLabels.add("x" + i);
        }

        // Agrega la etiqueta "x-" si no se restringe a ser no negativa.
        if (!this.restrictToNonNegative) {
            this.columnLabels.add("x-");
        }

        // Agrega etiquetas para las variables de holgura (s0, s1, s2, ...).
        for (int j = 0; j < this.getNumSlackVariables(); ++j) {
            this.columnLabels.add("s" + j);
        }

        // Agrega etiquetas para las variables artificiales (a0, a1, a2, ...).
        for (int k = 0; k < this.getNumArtificialVariables(); ++k) {
            this.columnLabels.add("a" + k);
        }

        // Agrega la etiqueta "RHS" (lado derecho de las restricciones).
        this.columnLabels.add("RHS");
    }


    protected RealMatrix createTableau(boolean maximize) {

        // Calcula el ancho y alto de la matriz del tableau.
        int width = this.numDecisionVariables + this.numSlackVariables + this.numArtificialVariables + this.getNumObjectiveFunctions() + 1;
        int height = this.constraints.size() + this.getNumObjectiveFunctions();

        // Crea una matriz para representar el tableau del algoritmo simplex.
        Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(height, width);

        // Configura la primera fila del tableau (fila 0) si hay dos funciones objetivo.
        if (this.getNumObjectiveFunctions() == 2) {
            matrix.setEntry(0, 0, -1.0);
        }

        // Determina la fila objetivo (z) en función de si se está maximizando o minimizando.
        int zIndex = this.getNumObjectiveFunctions() == 1 ? 0 : 1;
        matrix.setEntry(zIndex, zIndex, maximize ? 1.0 : -1.0);

        // Copia los coeficientes de la función objetivo en la fila objetivo (z).
        RealVector objectiveCoefficients = maximize ? this.f.getCoefficients().mapMultiply(-1.0) : this.f.getCoefficients();
        this.copyArray(objectiveCoefficients.getData(), matrix.getDataRef()[zIndex]);
        // Configura la entrada correspondiente al término constante de la función objetivo.
        matrix.setEntry(zIndex, width - 1, maximize ? this.f.getConstantTerm() : -1.0 * this.f.getConstantTerm());

        // Si no se restringe a las variables no negativas, realiza ajustes en la fila objetivo (z).
        if (!this.restrictToNonNegative) {
            matrix.setEntry(zIndex, this.getSlackVariableOffset() - 1, getInvertedCoeffiecientSum(objectiveCoefficients));
        }

// Inicializa contadores para las variables de holgura y artificiales.
        int slackVar = 0;
        int artificialVar = 0;

        // Itera a través de las restricciones y las agrega al tableau.
        for(int i = 0; i < this.constraints.size(); ++i) {
            LinearConstraint constraint = (LinearConstraint) this.constraints.get(i);
            int row = this.getNumObjectiveFunctions() + i;

            // Copia los coeficientes de la restricción en la fila correspondiente del tableau.
            this.copyArray(constraint.getCoefficients().getData(), matrix.getDataRef()[row]);

            // Realiza ajustes si no se restringen las variables no negativas.
            if (!this.restrictToNonNegative) {
                matrix.setEntry(row, this.getSlackVariableOffset() - 1, getInvertedCoeffiecientSum(constraint.getCoefficients()));
            }

            // Configura el valor RHS de la restricción en la fila del tableau.
            matrix.setEntry(row, width - 1, constraint.getValue());

            // Agrega variables de holgura (slack) según la relación de la restricción.
            if (constraint.getRelationship() == Relationship.LEQ) {
                matrix.setEntry(row, this.getSlackVariableOffset() + slackVar++, 1.0);
            } else if (constraint.getRelationship() == Relationship.GEQ) {
                matrix.setEntry(row, this.getSlackVariableOffset() + slackVar++, -1.0);
            }

            // Agrega variables artificiales según la relación de la restricción.
            if (constraint.getRelationship() == Relationship.EQ || constraint.getRelationship() == Relationship.GEQ) {
                matrix.setEntry(0, this.getArtificialVariableOffset() + artificialVar, 1.0);
                matrix.setEntry(row, this.getArtificialVariableOffset() + artificialVar++, 1.0);
                matrix.setRowVector(0, matrix.getRowVector(0).subtract(matrix.getRowVector(row)));
            }
        }

        // Devuelve el tableau del algoritmo simplex creado.
        return matrix;
    }

    public List<LinearConstraint> normalizeConstraints(Collection<LinearConstraint> originalConstraints) {
        // Crea una lista para almacenar las restricciones normalizadas.
        List<LinearConstraint> normalized = new ArrayList();

        // Itera a través de las restricciones originales.
        Iterator i$ = originalConstraints.iterator();

        while (i$.hasNext()) {
            // Obtiene una restricción original.
            LinearConstraint constraint = (LinearConstraint) i$.next();

            // Normaliza la restricción y la agrega a la lista de restricciones normalizadas.
            normalized.add(this.normalize(constraint));
        }

        // Devuelve la lista de restricciones normalizadas.
        return normalized;
    }


    private LinearConstraint normalize(LinearConstraint constraint) {
        return constraint.getValue() < 0.0 ? new LinearConstraint(constraint.getCoefficients().mapMultiply(-1.0), constraint.getRelationship().oppositeRelationship(), -1.0 * constraint.getValue()) : new LinearConstraint(constraint.getCoefficients(), constraint.getRelationship(), constraint.getValue());
    }

    protected final int getNumObjectiveFunctions() {
        return this.numArtificialVariables > 0 ? 2 : 1;
    }

    private int getConstraintTypeCounts(Relationship relationship) {
        int count = 0;
        Iterator i$ = this.constraints.iterator();

        while(i$.hasNext()) {
            LinearConstraint constraint = (LinearConstraint)i$.next();
            if (constraint.getRelationship() == relationship) {
                ++count;
            }
        }

        return count;
    }

    protected static double getInvertedCoeffiecientSum(RealVector coefficients) {
        double sum = 0.0;
        double[] arr$ = coefficients.getData();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            double coefficient = arr$[i$];
            sum -= coefficient;
        }

        return sum;
    }

    protected Integer getBasicRow(int col) {
        Integer row = null;

        for(int i = 0; i < this.getHeight(); ++i) {
            if (MathUtils.equals(this.getEntry(i, col), 1.0, this.epsilon) && row == null) {
                row = i;
            } else if (!MathUtils.equals(this.getEntry(i, col), 0.0, this.epsilon)) {
                return null;
            }
        }

        return row;
    }

    protected void dropPhase1Objective() {
        if (this.getNumObjectiveFunctions() != 1) {
            List<Integer> columnsToDrop = new ArrayList();
            columnsToDrop.add(0);

            // Primer bucle: Identifica las columnas a eliminar relacionadas con variables artificiales.
            int i;
            for (i = this.getNumObjectiveFunctions(); i < this.getArtificialVariableOffset(); ++i) {
                // Si la entrada en la fila 0 y la columna i es positiva, se agrega la columna a la lista de eliminación.
                if (MathUtils.compareTo(this.tableau.getEntry(0, i), 0.0, this.epsilon) > 0) {
                    columnsToDrop.add(i);
                }
            }

            // Segundo bucle: Identifica las columnas a eliminar relacionadas con variables artificiales no básicas.
            int j;
            for (j = 0; j < this.getNumArtificialVariables(); ++j) {
                int k = j + this.getArtificialVariableOffset();
                // Si la fila básica de la columna k es nula, se agrega la columna a la lista de eliminación.
                if (this.getBasicRow(k) == null) {
                    columnsToDrop.add(k);
                }
            }

            // Crea una nueva matriz para el tableau sin las columnas marcadas para eliminación.
            double[][] matrix = new double[this.getHeight() - 1][this.getWidth() - columnsToDrop.size()];

            // Tercer bucle: Copia las entradas no eliminadas al nuevo tableau.
            for (int m = 1; m < this.getHeight(); ++m) {
                int col = 0;

                for (int n = 0; n < this.getWidth(); ++n) {
                    if (!columnsToDrop.contains(n)) {
                        matrix[m - 1][col++] = this.tableau.getEntry(m, n);
                    }
                }
            }
            System.out.println("Columnas a eliminar por variables extras innecesarias: " + columnsToDrop);

            // Cuarto bucle: Elimina las etiquetas de columna correspondientes a las columnas eliminadas.
            for (int l = columnsToDrop.size() - 1; l >= 0; --l) {
                Integer remove = columnsToDrop.get(l);
                String toRemove = this.columnLabels.get(remove);
                this.columnLabels.remove(toRemove);
            }

            // Reemplaza el tableau original con el nuevo tableau sin las columnas eliminadas y establece el número de variables artificiales en 0.
            this.tableau = new Array2DRowRealMatrix(matrix);
            this.numArtificialVariables = 0;
        }
    }


    private void copyArray(double[] src, double[] dest) {
        System.arraycopy(src, 0, dest, this.getNumObjectiveFunctions(), src.length);
    }

    boolean isOptimal() {
        for(int i = this.getNumObjectiveFunctions(); i < this.getWidth() - 1; ++i) {
            if (MathUtils.compareTo(this.tableau.getEntry(0, i), 0.0, this.epsilon) < 0) {
                return false;
            }
        }
        return true;
    }

    protected RealPointValuePair getSolution() {
        System.out.println("Solución encontrada, obteniendo resultados");  // Imprime un mensaje de solución encontrada.

        // Encuentra la columna correspondiente a la variable "x-" (si existe).
        int negativeVarColumn = this.columnLabels.indexOf("x-");

        // Encuentra la fila básica correspondiente a la variable "x-" (si existe).
        Integer negativeVarBasicRow = negativeVarColumn > 0 ? this.getBasicRow(negativeVarColumn) : null;

        // Inicializa el valor más negativo en caso de que no haya una variable "x-".
        double mostNegative = negativeVarBasicRow == null ? 0.0 : this.getEntry(negativeVarBasicRow, this.getRhsOffset());

        // Un conjunto para almacenar las filas básicas para evitar duplicados.
        Set<Integer> basicRows = new HashSet();

        // Inicializa un arreglo para almacenar los coeficientes de las variables originales.
        double[] coefficients = new double[this.getOriginalNumDecisionVariables()];

        for (int i = 0; i < coefficients.length; ++i) {
            // Encuentra el índice de la columna correspondiente a la variable "x" (i).
            int colIndex = this.columnLabels.indexOf("x" + i);

            if (colIndex < 0) {
                // Si la variable "x" no se encuentra en el tableau, establece su coeficiente a 0.
                coefficients[i] = 0.0;
            } else {
                // Encuentra la fila básica correspondiente a la variable "x" (i).
                Integer basicRow = this.getBasicRow(colIndex);

                if (basicRows.contains(basicRow)) {
                    // Si la fila básica ya se ha utilizado, el coeficiente es 0.
                    coefficients[i] = 0.0;
                } else {
                    basicRows.add(basicRow);
                    // Calcula el coeficiente de la variable "x" (i) en función de su fila básica y "x-".
                    coefficients[i] = (basicRow == null ? 0.0 : this.getEntry(basicRow, this.getRhsOffset())) - (this.restrictToNonNegative ? 0.0 : mostNegative);
                }
            }
        }
        // Devuelve un objeto RealPointValuePair con los coeficientes y el valor objetivo.
        return new RealPointValuePair(coefficients, this.f.getValue(coefficients));
    }

    protected void divideRow(int dividendRow, double divisor) {
        for(int j = 0; j < this.getWidth(); ++j) {
            this.tableau.setEntry(dividendRow, j, this.tableau.getEntry(dividendRow, j) / divisor);
        }

    }

    protected void subtractRow(int minuendRow, int subtrahendRow, double multiple) {
        this.tableau.setRowVector(minuendRow, this.tableau.getRowVector(minuendRow).subtract(this.tableau.getRowVector(subtrahendRow).mapMultiply(multiple)));
    }

    public final int getWidth() {
        return this.tableau.getColumnDimension();
    }

    public final int getHeight() {
        return this.tableau.getRowDimension();
    }

    public final double getEntry(int row, int column) {
        return this.tableau.getEntry(row, column);
    }

    protected final void setEntry(int row, int column, double value) {
        this.tableau.setEntry(row, column, value);
    }

    protected final int getSlackVariableOffset() {
        return this.getNumObjectiveFunctions() + this.numDecisionVariables;
    }

    protected final int getArtificialVariableOffset() {
        return this.getNumObjectiveFunctions() + this.numDecisionVariables + this.numSlackVariables;
    }

    protected final int getRhsOffset() {
        return this.getWidth() - 1;
    }

    protected final int getNumDecisionVariables() {
        return this.numDecisionVariables;
    }

    protected final int getOriginalNumDecisionVariables() {
        return this.f.getCoefficients().getDimension();
    }

    protected final int getNumSlackVariables() {
        return this.numSlackVariables;
    }

    protected final int getNumArtificialVariables() {
        return this.numArtificialVariables;
    }

    public final double[][] getData() {
        return this.tableau.getData();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof SimplexTableau)) {
            return false;
        } else {
            SimplexTableau rhs = (SimplexTableau)other;
            return this.restrictToNonNegative == rhs.restrictToNonNegative && this.numDecisionVariables == rhs.numDecisionVariables && this.numSlackVariables == rhs.numSlackVariables && this.numArtificialVariables == rhs.numArtificialVariables && this.epsilon == rhs.epsilon && this.f.equals(rhs.f) && this.constraints.equals(rhs.constraints) && this.tableau.equals(rhs.tableau);
        }
    }

    public int hashCode() {
        return Boolean.valueOf(this.restrictToNonNegative).hashCode() ^ this.numDecisionVariables ^ this.numSlackVariables ^ this.numArtificialVariables ^ Double.valueOf(this.epsilon).hashCode() ^ this.f.hashCode() ^ this.constraints.hashCode() ^ this.tableau.hashCode();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        MatrixUtils.serializeRealMatrix(this.tableau, oos);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        MatrixUtils.deserializeRealMatrix(this, "tableau", ois);
    }

}


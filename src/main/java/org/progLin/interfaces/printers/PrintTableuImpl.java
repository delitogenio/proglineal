package org.progLin.interfaces.printers;

import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionConversionException;
import org.apache.commons.math.linear.RealMatrix;
import org.progLin.apachelibmod.SimplexTableau;


public class PrintTableuImpl implements PrintTableu{

    @Override
    public void printTableau(SimplexTableau tableau) throws FractionConversionException {
        Fraction[][] data = new Fraction[tableau.getHeight()][tableau.getWidth()];

        for (int i = 0; i < tableau.getHeight(); i++) {
            for (int j = 0; j < tableau.getWidth(); j++) {
                data[i][j] = new Fraction(tableau.getEntry(i, j));
            }
        }

        for (int i = 0; i < tableau.getHeight(); i++) {
            for (int j = 0; j < tableau.getWidth(); j++) {
                System.out.print(data[i][j] + "            ");
            }
            System.out.println();
        }
    }

    @Override
    public void printMatrix(RealMatrix matrix) {
        double[][] data = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        // Imprimir la matriz
        int numRows = matrix.getRowDimension();
        int numCols = matrix.getColumnDimension();

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                System.out.print(matrix.getEntry(i, j) + "  ");
            }
            System.out.println(); // Nueva línea para la siguiente fila
        }
    }
   }


package org.progLin.interfaces;

import org.progLin.model.LinearConstraintData;

import java.util.List;

public class printInterfaceImpl implements printCoefficientMatrixInt{
    @Override
    public void printCoefficientMatrix(String title, List<LinearConstraintData> constraintDataList) {
        System.out.println(title);
        for (LinearConstraintData data : constraintDataList) {
            double[] coefficients = data.getCoefficients();
            String relation = data.getRelation();
            double rhs = data.getRhs();
            System.out.print("[ ");
            for (int i = 0; i < coefficients.length; i++) {
                System.out.print(coefficients[i] + " ");
            }
            System.out.print("] " + relation + " " + rhs);
            System.out.println();
        }
        System.out.println();
    }
}

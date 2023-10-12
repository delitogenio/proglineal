package org.progLin.interfaces;

import org.progLin.model.LinearConstraintData;

import java.util.List;

public interface printCoefficientMatrixInt {

    void printCoefficientMatrix(String title, List<LinearConstraintData> constraintDataList);
}

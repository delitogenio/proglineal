package org.progLin.interfaces;

import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.progLin.model.LinearConstraintData;

import java.util.List;

public interface ConstraintCreator {

    List<LinearConstraint> createConstraints(List<LinearConstraintData> constraintDataList);
}

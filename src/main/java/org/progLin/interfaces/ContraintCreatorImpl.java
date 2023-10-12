package org.progLin.interfaces;

import org.apache.commons.math.optimization.linear.LinearConstraint;
import org.apache.commons.math.optimization.linear.Relationship;
import org.progLin.model.LinearConstraintData;

import java.util.ArrayList;
import java.util.List;

public class ContraintCreatorImpl implements ConstraintCreator{
    @Override
    public List<LinearConstraint>   createConstraints(List<LinearConstraintData> constraintDataList) {
        List<LinearConstraint> constraints = new ArrayList<>();
        for (LinearConstraintData data : constraintDataList) {
            double[] coefficients = data.getCoefficients();
            String relation = data.getRelation();
            double rhs = data.getRhs();
            boolean unrestricted = data.isUnrestricted();

            if (!unrestricted) {
                Relationship relationship = Relationship.LEQ;
                if (relation.equals("GEQ")) {
                    relationship = Relationship.GEQ;
                } else if (relation.equals("EQ")) {
                    relationship = Relationship.EQ;
                }

                LinearConstraint constraint = new LinearConstraint(coefficients, relationship, rhs);
                constraints.add(constraint);
            }
    }
        return constraints;
    }

}

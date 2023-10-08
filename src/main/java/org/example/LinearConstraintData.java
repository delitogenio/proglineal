package org.example;

public class LinearConstraintData {

    private final double[] coefficients;
    private final String relation;
    private final double rhs;

    private final boolean unrestricted;

    public LinearConstraintData(double[] coefficients, String relation, double rhs, boolean unrestricted) {
        this.coefficients = coefficients;
        this.relation = relation;
        this.rhs = rhs;
        this.unrestricted = unrestricted;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public String getRelation() {
        return relation;
    }

    public double getRhs() {
        return rhs;
    }

    public boolean isUnrestricted() {
        return unrestricted;
    }
}

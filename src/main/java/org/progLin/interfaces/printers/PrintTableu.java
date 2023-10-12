package org.progLin.interfaces.printers;

import org.apache.commons.math.fraction.FractionConversionException;
import org.apache.commons.math.linear.RealMatrix;
import org.progLin.apachelibmod.SimplexTableau;

public interface PrintTableu {

    void printTableau(SimplexTableau tableau) throws FractionConversionException;
    void  printMatrix(RealMatrix matrix);

}

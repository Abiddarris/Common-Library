package com.abiddarris.common.utils;

/**
 * Class that provides math utilities that used in this
 * library
 *
 * @author Abiddarris
 * @since 1.1
 */
public class Maths {
    
    /**
     * Prevent from creating this class
     */
    private Maths() {}
    
    /**
     * Calculate logarithm from given base and value
     *
     * @param base Base
     * @param v Value
     * @return logarithm
     * @since 1.1
     */
    public static double log(double base, double v) {      
        return Math.log(v) / Math.log(base);
    }
    
}

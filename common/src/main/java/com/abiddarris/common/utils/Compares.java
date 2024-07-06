package com.abiddarris.common.utils;

/**
 * Class that compares two primitive data type
 *
 * @author Abiddarris
 * @since 1.0
 */
public class Compares {
    
    /**
     * Compare two {@code Long}s
     *
     * @param obj First value
     * @param obj2 Second value
     * @return result
     * @since 1.0
     */
    public static int compareLong(long obj, long obj2) {
        if(obj == obj2) return 0;     
        if(obj < obj2) return -1; 

        return 1;
    }
    
}
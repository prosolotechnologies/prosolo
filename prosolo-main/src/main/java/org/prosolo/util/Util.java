package org.prosolo.util;

import java.math.BigInteger;

/**
 * Created by stefanvuckovic on 5/23/17.
 */
public class Util {

    public static long getUnboxedLongOrZero(Long l) {
        return l != null ? l : 0;
    }

    public static long convertBigIntegerToLong(BigInteger n) {
        return n != null ? n.longValue() : 0;
    }
}

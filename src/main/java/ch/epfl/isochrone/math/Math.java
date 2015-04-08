package ch.epfl.isochrone.math;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Integer.signum;

/**
 * Math contains some commonly used mathematical functions in form of static
 * methods.
 *
 * @author Jakob Bauer (223590)
 */
public final class Math {

    private Math() {};

    /**
     * Returns the inverse hyperbolic sine of its argument.
     *
     * @param x     the argument.
     * @return      the inverse hyperbolic sine of the argument.
     */
    public static double asinh(double x) {
        return log(x + sqrt(1 + pow(x, 2)));
    }

    /**
     * Returns the haversin of its argument.
     *
     * @param x     the argument.
     * @return      the haversin of the argument.
     */
    public static double haversin(double x) {
        return pow(sin(x / 2.0), 2);
    }

    private static int divT(int n, int d) { return n / d; }

    private static int modT(int n, int d) { return n % d; }

    private static int signFlag(int n, int d) {
        int rt = modT(n,d);
        return (signum(rt) == -signum(d)) ? 1 : 0;
    }

    /**
     * Returns the quotient of the Euclidean division (by default) of n by d.
     *
     * @param n the numerator.
     * @param d the denominator.
     * @return  the quotient of the Euclidean division of n by d.
     */
    public static int divF(int n, int d) {
        return divT(n,d) - signFlag(n,d);
    }

    /**
     * Returns the remainder of the Euclidean division (by default) of n by d.
     *
     * @param n the numerator.
     * @param d the denominator.
     * @return  the remainder of the Euclidean division of n by d.
     */
    public static int modF(int n, int d) {
        return modT(n,d) + signFlag(n,d) * d;
    }
}

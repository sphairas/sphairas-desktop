package org.thespheres.betula.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Boris Heithecker
 */
@XmlTransient
public final class Int2 extends Number implements Comparable<Int2> {

    private final int internalValue;
    private final static NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
    private final static DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.getDefault());

    static {
        nf.setMaximumFractionDigits(1);
        dfs.setGroupingSeparator(' ');
        if(nf instanceof DecimalFormat) {
            ((DecimalFormat) nf).setGroupingUsed(true);
            ((DecimalFormat) nf).setDecimalFormatSymbols(dfs);
        }
    }

    private Int2() {
        internalValue = 0;
    }

    private Int2(int internal) {
        this.internalValue = internal;
    }

    public Int2(double value) {
        this.internalValue = parseDoubleToInternal(value);
    }

    public Int2(String value) throws ParseException {
        this(nf.parse(value).doubleValue());
    }

    public static void validInt2(String text) throws ParseException {
        nf.parse(text);
    }

    public static Int2 fromInternalValue(int i) {
        Int2 ret = new Int2((int) i);
        return ret;
    }

    public int getInternalValue() {
        return internalValue;
    }

    @Override
    public String toString() {
        return nf.format(dvalue());
    }

    public Int2 add(Int2 toAdd) {
        return Int2.fromInternalValue(this.internalValue + toAdd.internalValue);
    }

    public Int2 divideBy(Int2 toDivide) {
        return new Int2((double) this.internalValue / toDivide.internalValue);
    }

    public Int2 multiply(Int2 toMultiply) {
        return Int2.fromInternalValue(this.internalValue * toMultiply.getInternalValue() / 2);
    }

    public Int2 subtractFormThis(Int2 toSubtract) {
        return Int2.fromInternalValue(this.internalValue - toSubtract.internalValue);
    }

    public static Int2 sum(Int2[] toAdd) {
        Int2 ret = new Int2();
        for (Int2 i : toAdd) {
            ret = ret.add(i);
        }
        return ret;
    }

    /*
     *  value * 100 / maximum 
     */
    public static Int2 percentageOfValue(Int2 value, Int2 maximum) {
        return new Int2((double) (value.internalValue * 100) / maximum.internalValue);
    }

    /*
     *  percentage * maximum / 100
     */
    public static Int2 valueOfPercentage(Int2 percentage, Int2 maximum) {
        return new Int2((double) (percentage.internalValue * maximum.internalValue) / 400);
    }

    @Override
    public int intValue() {
        return Double.valueOf(dvalue()).intValue();
    }

    @Override
    public long longValue() {
        return Double.valueOf(dvalue()).longValue();
    }

    @Override
    public float floatValue() {
        return Double.valueOf(dvalue()).floatValue();
    }

    @Override
    public double doubleValue() {
        return dvalue();
    }

    private static int parseDoubleToInternal(double value) {
        long twice = Math.round(value * 2d);
        return Long.valueOf(twice).intValue();
    }

    private double dvalue() {
        return Integer.valueOf(internalValue).doubleValue() / 2;
    }

    @Override
    public int hashCode() {
        return internalValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Int2) {
            return internalValue == ((Int2) obj).getInternalValue();
        }
        return false;
    }
    
    @Override
    public int compareTo(Int2 anotherInteger) {
        int thisVal = this.internalValue;
        int anotherVal = anotherInteger.getInternalValue();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }
}

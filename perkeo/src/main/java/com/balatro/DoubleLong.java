package com.balatro;

public final class DoubleLong {
    // Internally store the bits as a signed long.
    // In C++ union, 'ulong' is often considered 64-bit unsigned,
    // but Java does not have a built-in unsigned long type.
    private long bits;

    /**
     * Default constructor (bits initialized to 0).
     */
    public DoubleLong() {
        this.bits = 0;
    }

    /**
     * Constructor that sets the internal bits from a double.
     */
    public DoubleLong(double dbl) {
        setDouble(dbl);
    }

    /**
     * Constructor that sets the internal bits from a 64-bit value.
     * (Equivalent to the union's 'uint64_t' concept in C++, but stored as a signed long in Java.)
     */
    public DoubleLong(long ulong) {
        this.bits = ulong;
    }

    /**
     * Set the stored bits based on a double value.
     * Uses Double.doubleToLongBits to get the IEEE 754 representation.
     */
    public void setDouble(double dbl) {
        this.bits = Double.doubleToLongBits(dbl);
    }

    /**
     * Retrieve the stored bits as a double using IEEE 754 conversion.
     */
    public double getDouble() {
        return Double.longBitsToDouble(this.bits);
    }

    /**
     * Set the stored bits from a 64-bit long value.
     * Emulates writing to 'ulong' in the union.
     */
    public void setUlong(long ulong) {
        this.bits = ulong;
    }

    /**
     * Retrieve the stored bits as a 64-bit long.
     * Emulates reading from 'ulong' in the union.
     */
    public long getUlong() {
        return this.bits;
    }
}
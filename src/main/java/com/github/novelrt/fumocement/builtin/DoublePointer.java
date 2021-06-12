// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;

/**
 * Represents a {@code double*} stored natively.
 */
public final class DoublePointer extends NativeObject {
    public DoublePointer() {
        super(allocatePointer(), true, DoublePointer::destroyPointer);
    }

    public DoublePointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, DoublePointer::destroyPointer);
    }

    public DoublePointer(long handle, boolean isOwned) {
        super(handle, isOwned, DoublePointer::destroyPointer);
    }

    public DoublePointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, DoublePointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native double getValue(long handle);

    private static native void setValue(long handle, double value);

    public double getValue() {
        return getValue(getHandle());
    }

    public void setValue(double value) {
        setValue(getHandle(), value);
    }
}

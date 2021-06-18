// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;
import com.github.novelrt.fumocement.Pointer;

/**
 * Represents a {@code float*} stored natively.
 */
public final class FloatPointer extends NativeObject {
    public FloatPointer() {
        super(allocatePointer(), true, FloatPointer::destroyPointer);
    }

    public FloatPointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, FloatPointer::destroyPointer);
    }

    public FloatPointer(long handle, boolean isOwned) {
        super(handle, isOwned, FloatPointer::destroyPointer);
    }

    public FloatPointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, FloatPointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native float getValue(long handle);

    private static native void setValue(long handle, float value);

    @Override
    public @Pointer("float*") long getHandle() {
        return super.getHandle();
    }

    public float getValue() {
        return getValue(getHandle());
    }

    public void setValue(float value) {
        setValue(getHandle(), value);
    }
}

// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;

/**
 * Represents a {@code int16_t*} stored natively.
 */
public final class Int16Pointer extends NativeObject {
    public Int16Pointer() {
        super(allocatePointer(), true, Int16Pointer::destroyPointer);
    }

    public Int16Pointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, Int16Pointer::destroyPointer);
    }

    public Int16Pointer(long handle, boolean isOwned) {
        super(handle, isOwned, Int16Pointer::destroyPointer);
    }

    public Int16Pointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, Int16Pointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native short getValue(long handle);

    private static native void setValue(long handle, short value);

    public short getValue() {
        return getValue(getHandle());
    }

    public void setValue(short value) {
        setValue(getHandle(), value);
    }
}

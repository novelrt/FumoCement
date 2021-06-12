// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.


package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;

/**
 * Represents a {@code int8_t*} stored natively.
 */
public final class Int8Pointer extends NativeObject {
    public Int8Pointer() {
        super(allocatePointer(), true, Int8Pointer::destroyPointer);
    }

    public Int8Pointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, Int8Pointer::destroyPointer);
    }

    public Int8Pointer(long handle, boolean isOwned) {
        super(handle, isOwned, Int8Pointer::destroyPointer);
    }

    public Int8Pointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, Int8Pointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native byte getValue(long handle);

    private static native void setValue(long handle, byte value);

    public byte getValue() {
        return getValue(getHandle());
    }

    public void setValue(byte value) {
        setValue(getHandle(), value);
    }
}

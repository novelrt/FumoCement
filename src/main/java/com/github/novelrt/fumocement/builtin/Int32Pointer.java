// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;
import com.github.novelrt.fumocement.Pointer;

/**
 * Represents a {@code int32_t*} stored natively.
 */
public final class Int32Pointer extends NativeObject {
    public Int32Pointer() {
        super(allocatePointer(), true, Int32Pointer::destroyPointer);
    }

    public Int32Pointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, Int32Pointer::destroyPointer);
    }

    public Int32Pointer(long handle, boolean isOwned) {
        super(handle, isOwned, Int32Pointer::destroyPointer);
    }

    public Int32Pointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, Int32Pointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native int getValue(long handle);

    private static native void setValue(long handle, int value);

    @Override
    public @Pointer("int32_t*") long getHandle() {
        return super.getHandle();
    }

    public int getValue() {
        return getValue(getHandle());
    }

    public void setValue(int value) {
        setValue(getHandle(), value);
    }
}

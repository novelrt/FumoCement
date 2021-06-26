// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;
import com.github.novelrt.fumocement.Pointer;

/**
 * Represents a {@code uint16_t*} stored natively.
 */
public final class UInt16Pointer extends NativeObject {
    public UInt16Pointer() {
        super(allocatePointer(), true, UInt16Pointer::destroyPointer);
    }

    public UInt16Pointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, UInt16Pointer::destroyPointer);
    }

    public UInt16Pointer(long handle, boolean isOwned) {
        super(handle, isOwned, UInt16Pointer::destroyPointer);
    }

    public UInt16Pointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, UInt16Pointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native char getUnsignedValue(long handle);

    private static native void setUnsignedValue(long handle, char value);

    @Override
    public @Pointer("uint16_t*") long getHandle() {
        return super.getHandle();
    }

    public char getUnsignedValue() {
        return getUnsignedValue(getHandle());
    }

    public void setUnsignedValue(char value) {
        setUnsignedValue(getHandle(), value);
    }
}

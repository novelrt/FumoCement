// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;
import com.github.novelrt.fumocement.Pointer;
import com.github.novelrt.fumocement.Unsigned;

/**
 * Represents a {@code uint32_t*} stored natively.
 */
public final class UInt32Pointer extends NativeObject {
    public UInt32Pointer() {
        super(allocatePointer(), true, UInt32Pointer::destroyPointer);
    }

    public UInt32Pointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, UInt32Pointer::destroyPointer);
    }

    public UInt32Pointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, UInt32Pointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native @Unsigned int getUnsignedValue(long handle);

    private static native void setUnsignedValue(long handle, @Unsigned int value);

    @Override
    public @Pointer("uint32_t*") long getHandle() {
        return super.getHandle();
    }

    public @Unsigned int getUnsignedValue() {
        return getUnsignedValue(getHandle());
    }

    public void setUnsignedValue(@Unsigned int value) {
        setUnsignedValue(getHandle(), value);
    }

    public @Unsigned long getUnsignedLongValue() {
        return Integer.toUnsignedLong(getUnsignedValue());
    }
}

// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.NativeObject;
import com.github.novelrt.fumocement.Pointer;
import com.github.novelrt.fumocement.Unsigned;

/**
 * Represents a {@code uint64_t*} stored natively.
 */
public final class UInt64Pointer extends NativeObject {
    public UInt64Pointer() {
        super(allocatePointer(), true, UInt64Pointer::destroyPointer);
    }

    public UInt64Pointer(long handle, boolean isOwned) {
        super(handle, isOwned, UInt64Pointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native @Unsigned long getUnsignedValue(long handle);

    private static native void setUnsignedValue(long handle, @Unsigned long value);

    @Override
    public @Pointer("uint64_t*") long getHandle() {
        return super.getHandle();
    }

    public @Unsigned long getUnsignedValue() {
        return getUnsignedValue(getHandle());
    }

    public void setUnsignedValue(@Unsigned long value) {
        setUnsignedValue(getHandle(), value);
    }
}

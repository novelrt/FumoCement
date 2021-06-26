// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.NativeObject;
import com.github.novelrt.fumocement.Pointer;

/**
 * Represents a {@code uintptr_t*} stored natively.
 */
public final class UIntPtrPointer extends NativeObject {
    public UIntPtrPointer() {
        super(allocatePointer(), true, UIntPtrPointer::destroyPointer);
    }

    public UIntPtrPointer(long handle, boolean isOwned) {
        super(handle, isOwned, UIntPtrPointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native @Pointer long getValue(long handle);

    private static native void setValue(long handle, @Pointer long value);

    @Override
    public @Pointer("uintptr_t*") long getHandle() {
        return super.getHandle();
    }

    public @Pointer long getValue() {
        return getValue(getHandle());
    }

    public void setUnsignedValue(@Pointer long value) {
        setValue(getHandle(), value);
    }
}

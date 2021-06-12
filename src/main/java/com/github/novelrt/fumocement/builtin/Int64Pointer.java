// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;

/**
 * Represents a {@code int64_t*} stored natively.
 */
public final class Int64Pointer extends NativeObject {
    public Int64Pointer() {
        super(allocatePointer(), true, Int64Pointer::destroyPointer);
    }

    public Int64Pointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, Int64Pointer::destroyPointer);
    }

    public Int64Pointer(long handle, boolean isOwned) {
        super(handle, isOwned, Int64Pointer::destroyPointer);
    }

    public Int64Pointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, Int64Pointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native long getValue(long handle);

    private static native void setValue(long handle, long value);

    public long getValue() {
        return getValue(getHandle());
    }

    public void setValue(long value) {
        setValue(getHandle(), value);
    }
}

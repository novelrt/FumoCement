// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;
import com.github.novelrt.fumocement.Unsigned;

/**
 * Represents a {@code uint8_t*} stored natively.
 */
public final class UInt8Pointer extends NativeObject {
    public UInt8Pointer() {
        super(allocatePointer(), true, UInt8Pointer::destroyPointer);
    }

    public UInt8Pointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, UInt8Pointer::destroyPointer);
    }

    public UInt8Pointer(long handle, boolean isOwned) {
        super(handle, isOwned, UInt8Pointer::destroyPointer);
    }

    public UInt8Pointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, UInt8Pointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native byte getUnsignedValue(long handle);

    private static native void setUnsignedValue(long handle, @Unsigned byte value);

    public @Unsigned byte getUnsignedValue() {
        return getUnsignedValue(getHandle());
    }

    public void setUnsignedValue(@Unsigned byte value) {
        setUnsignedValue(getHandle(), value);
    }

    public @Unsigned int getUnsignedIntValue() {
        return Byte.toUnsignedInt(getUnsignedValue());
    }
}

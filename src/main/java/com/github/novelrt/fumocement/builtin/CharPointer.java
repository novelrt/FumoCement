// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.


package com.github.novelrt.fumocement.builtin;

import com.github.novelrt.fumocement.DisposalMethod;
import com.github.novelrt.fumocement.NativeObject;
import com.github.novelrt.fumocement.Pointer;
import com.github.novelrt.fumocement.StringDeletionBehaviour;

import java.nio.charset.Charset;

/**
 * Represents a {@code char*} stored natively.
 */
public final class CharPointer extends NativeObject {
    public CharPointer() {
        super(allocatePointer(), true, CharPointer::destroyPointer);
    }

    public CharPointer(DisposalMethod disposalMethod) {
        super(allocatePointer(), true, disposalMethod, CharPointer::destroyPointer);
    }

    public CharPointer(long handle, boolean isOwned) {
        super(handle, isOwned, CharPointer::destroyPointer);
    }

    public CharPointer(long handle, boolean isOwned, DisposalMethod disposalMethod) {
        super(handle, isOwned, disposalMethod, CharPointer::destroyPointer);
    }

    private static native long allocatePointer();

    private static native void destroyPointer(long handle);

    private static native byte getValue(long handle);

    private static native void setValue(long handle, byte value);

    private static native byte[] readAsNullTerminatedString(long handle, boolean deleteString);

    public byte getValue() {
        return getValue(getHandle());
    }

    public void setValue(byte value) {
        setValue(getHandle(), value);
    }

    @Override
    public @Pointer("char*") long getHandle() {
        return super.getHandle();
    }

    public String readAsNullTerminatedString(StringDeletionBehaviour deletionBehaviour) {
        byte[] bytes = readAsNullTerminatedString(getHandle(), deletionBehaviour.isDeletingString());
        return new String(bytes);
    }

    public String readAsNullTerminatedString(StringDeletionBehaviour deletionBehaviour, Charset charset) {
        byte[] bytes = readAsNullTerminatedString(getHandle(), deletionBehaviour.isDeletingString());
        return new String(bytes, charset);
    }
}

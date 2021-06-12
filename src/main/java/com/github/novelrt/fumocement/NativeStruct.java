// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

/**
 * Represents a struct from the native land.
 * <p>
 * This base class is mainly used in code generation for mapping C structs
 * to their Java equivalent.
 * <p>
 * In order to make interop between Java and C easier,
 * the {@link NativeObject#getHandle()} method becomes public. This allows for directly
 * passing pointers to native methods.
 * <p>
 * <b>WARNING:</b> This class (and any of its inheritors) must <b>NOT</b> be used as
 * a public API surface. It must be only used internally as a helper for accessing
 * C structs.
 */
public abstract class NativeStruct extends NativeObject {
    /**
     * Constructs a new {@link NativeStruct} with the given handle, the native resource owning state, and
     * a {@link HandleDeleter} deleting the native handle. It can be {@code null} if the
     * {@code owned} parameter is {@code false}. This object's native resources will be garbage collected.
     *
     * @param handle        the native handle
     * @param owned         whether or not this object owns native resources
     * @param handleDeleter the {@link HandleDeleter} to use in order to delete native resources,
     *                      which can be {@code null} when {@code owned} is false
     */
    public NativeStruct(@Pointer long handle, boolean owned, HandleDeleter handleDeleter) {
        super(handle, owned, handleDeleter);
    }

    /**
     * Constructs a new {@link NativeStruct} with the given handle, the native resource owning state,
     * the {@link DisposalMethod} and a {@link HandleDeleter} deleting the native handle.
     * It can be {@code null} if the {@code owned} parameter is {@code false}.
     *
     * @param handle        the native handle
     * @param owned         whether or not this object owns native resources
     * @param disposalMethod the disposal method to use
     * @param handleDeleter the {@link HandleDeleter} to use in order to delete native resources,
     *                      which can be {@code null} when {@code owned} is false
     */
    public NativeStruct(@Pointer long handle, boolean owned, DisposalMethod disposalMethod, HandleDeleter handleDeleter) {
        super(handle, owned, disposalMethod, handleDeleter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Pointer long getHandle() {
        return super.getHandle();
    }
}

// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

/**
 * A provider for getting a {@link NativeObject} using an handle without owning it.
 *
 * @param <T> the type of native object to provide
 * @see NativeObject
 * @see NativeObject.ResourceOwningState#UNOWNED
 */
public interface UnownedNativeObjectProvider<T extends NativeObject> {
  /**
   * Provides a {@link NativeObject} of type {@code T} with the given handle, without
   * owning it.
   *
   * @param handle the handle to use
   * @return an instance of {@code T} using the given handle
   */
  T provide(@Pointer("T*") long handle);
}

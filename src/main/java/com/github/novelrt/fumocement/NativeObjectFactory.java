// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

/**
 * Represents a factory to create {@link NativeObject} instances.
 *
 * @param <T> the type of {@link NativeObject} to create
 */
@FunctionalInterface
public interface NativeObjectFactory<T extends NativeObject> {
  /**
   * Creates a new instance of a NativeObject of type {@code T}, with the specified
   * handle and the native resource owning state.
   *
   * @param handle the native handle
   * @param owned  whether or not this object owns native resources
   * @return an instance of type {@code T} with the given handle and owning state
   */
  T createInstance(@Pointer long handle, boolean owned);
}

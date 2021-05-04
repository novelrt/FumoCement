// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

/**
 * Cleans native resources using a given handle.
 */
@FunctionalInterface
public interface HandleDeleter {
  /**
   * Cleans any native resources associated to a native handle.
   * @param handle the native handle
   */
  void deleteHandle(@Pointer long handle);
}

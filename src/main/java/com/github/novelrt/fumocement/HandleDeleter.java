// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

@FunctionalInterface
public interface HandleDeleter {
  void deleteHandle(long handle);
}

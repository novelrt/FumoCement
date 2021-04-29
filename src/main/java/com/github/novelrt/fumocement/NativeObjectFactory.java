// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

@FunctionalInterface
public interface NativeObjectFactory<T extends NativeObject> {
  T createInstance(long handle, boolean isOwned);
}

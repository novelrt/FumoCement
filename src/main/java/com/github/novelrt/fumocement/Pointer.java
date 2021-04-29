// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import org.jetbrains.annotations.Nullable;

public class Pointer<T extends NativeObject> extends NativeObject {
  private final NativeObjectFactory<T> factory;

  public Pointer(NativeObjectFactory<T> factory) {
    super(createPointer(), true, Pointer::destroyPointer);
    this.factory = factory;
  }

  private static native long getUnderlyingHandle(long handle);

  private static native long createPointer();

  private static native void destroyPointer(long handle);

  public @Nullable T get() {
    long underlyingHandle = getUnderlyingHandle(getHandle());
    if (underlyingHandle == 0) {
      return null;
    }
    return factory.createInstance(underlyingHandle, false);
  }
}

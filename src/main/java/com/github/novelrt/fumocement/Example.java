// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

public class Example extends NativeObject {
  public Example() {
    super(createStruct(), true, Example::destroyStruct);
  }

  protected Example(long handle, boolean isOwned) {
    super(handle, isOwned, Example::destroyStruct);
  }

  private static native long createStruct();

  private static native void destroyStruct(long handle);

  public static Pointer<Example> createPointer() {
    return new Pointer<>(Example::new);
  }

  public native @Unsigned int unsignedMethod(@Unsigned long size, @Unsigned int count);
}

// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import java.lang.ref.Cleaner;

/**
 * Represents an object managed natively using a handle (a native pointer).
 *
 * <p>
 * <b>Example:</b>
 * <pre>{@code public class Example extends NativeObject {
 *   public Example() {
 *     super(createStruct(), true, Example::destroyStruct);
 *   }
 *
 *   protected Example(long handle, boolean isOwned) {
 *     super(handle, isOwned, Example::destroyStruct);
 *   }
 *
 *   private static native long createStruct();
 *
 *   private static native void destroyStruct(long handle);
 *
 *   public static Pointer<Example> createPointer() {
 *     return new Pointer<>(Example::new);
 *   }
 *
 *   public native void someMethod(int param);
 * }
 * }</pre>
 */
public abstract class NativeObject {
  private static final Cleaner NATIVE_OBJECTS_CLEANER = Cleaner.create();

  private final State state;
  private final boolean isOwned;

  protected NativeObject(long handle, boolean isOwned, HandleDeleter handleDeleter) {
    this.isOwned = isOwned;
    this.state = new State(handle, handleDeleter);
    if (isOwned) {
      NATIVE_OBJECTS_CLEANER.register(this, state);
    }
  }

  protected long getHandle() {
    return state.handle;
  }

  protected boolean isOwned() {
    return isOwned;
  }

  private static class State implements Runnable {
    public final long handle;
    public final HandleDeleter handleDeleter;

    private State(long handle, HandleDeleter handleDeleter) {
      this.handle = handle;
      this.handleDeleter = handleDeleter;
    }

    // Deletion method
    @Override
    public void run() {
      handleDeleter.deleteHandle(handle);
    }
  }
}

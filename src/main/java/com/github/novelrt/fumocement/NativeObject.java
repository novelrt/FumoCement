// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import java.lang.ref.Cleaner;

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

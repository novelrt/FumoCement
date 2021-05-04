// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import java.lang.ref.Cleaner;

/**
 * Represents an object managed natively using a handle, which is a native pointer
 * represented by a {@code long}.
 * <p>
 * Native objects can be either <i>owned</i>, <i>unowned</i>, or <i>cleaned</i>.<br>
 * Owned objects have native resources under the possession of the JVM, meaning that their {@link HandleDeleter}
 * will be ran once the object gets garbage collected, thus releasing any native resources.
 * This routine is ran by a {@link Cleaner}.<br>
 * On the other hand, unowned objects do not have any mechanism running once they
 * get garbage collected. They mainly serve as an access layer to resources managed natively.<br>
 * Finally, a cleaned object has been definitively deleted from the native space, and must not
 * be used anymore.
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
 *   public native void someMethod(int param);
 * }
 * }</pre>
 *
 * @see HandleDeleter
 * @see ResourceOwningState
 */
public abstract class NativeObject implements AutoCloseable {
  // TODO: Provide compatibility with Java 8 and stop using Cleaner.
  private static final Cleaner NATIVE_OBJECTS_CLEANER = Cleaner.create();

  private final State state;

  /**
   * Constructs a new native object with the given handle, the native resource owning state, and
   * a {@link HandleDeleter} deleting the native handle. It can be {@code null} if the
   * {@code owned} parameter is {@code false}.
   *
   * @param handle        the native handle
   * @param owned         whether or not this object owns native resources
   * @param handleDeleter the {@link HandleDeleter} to use in order to delete native resources,
   *                      which can be {@code null} when {@code owned} is false
   */
  protected NativeObject(@Pointer long handle, boolean owned, HandleDeleter handleDeleter) {
    this.state = new State(handle, handleDeleter, owned ? ResourceOwningState.OWNED : ResourceOwningState.UNOWNED);
    if (owned) {
      if (handleDeleter == null) {
        throw new IllegalArgumentException("This NativeObject is owned, but its handleDeleter is null.");
      }
      NATIVE_OBJECTS_CLEANER.register(this, state);
    }
  }

  /**
   * Gets the native handle.
   * <p>
   * This handle is represented as a {@code long} to support 64-bit pointers.
   *
   * @throws IllegalStateException when the native resource this object holds has been cleared
   * @return the native handle
   */
  protected @Pointer long getHandle() {
    if (state.resourceOwningState == ResourceOwningState.CLEARED) {
      throw new IllegalStateException("Cannot get the handle of this NativeObject as its native resource has " +
                                      "already been cleared.");
    }
    return state.handle;
  }

  /**
   * Gets the the current state of native resources owned by this object.
   * <p>
   * This determines if there needs to be a native resource releasing process
   * (a {@link HandleDeleter}) once the object is garbage collected.
   *
   * @return a {@link ResourceOwningState} indicating the current state of native resources
   *         owned by this object
   */
  protected ResourceOwningState getResourceOwningState() {
    return state.resourceOwningState;
  }

  /**
   * Clears any native resources this object holds.
   * <p>
   * This does not have any effect when the current resource owning state is not
   * {@link ResourceOwningState#OWNED}.
   */
  @Override
  public void close() {
    state.deleteNativeResources();
  }

  /**
   * Indicates the state for native resources contained in a {@link NativeObject}.
   */
  public enum ResourceOwningState {
    /**
     * The native resource is not managed by the JVM, the object only serves as a access layer
     * to the resource, without disposing anything.
     */
    UNOWNED,
    /**
     * The native resource is managed by the JVM. It can be cleaned manually as well as it can be
     * cleaned once the object has been garbage collected.
     */
    OWNED,
    /**
     * The native resource has definitively been removed. Any call to {@link NativeObject#getHandle()}
     * will produce an exception.
     */
    CLEARED
  }

  private static class State implements Runnable {
    public final long handle;
    public final HandleDeleter handleDeleter;
    public ResourceOwningState resourceOwningState;

    private State(long handle, HandleDeleter handleDeleter, ResourceOwningState resourceOwningState) {
      this.handle = handle;
      this.handleDeleter = handleDeleter;
      this.resourceOwningState = resourceOwningState;
    }

    // Deletion method
    @Override
    public void run() {
      deleteNativeResources();
    }

    private void deleteNativeResources() {
      if (resourceOwningState == ResourceOwningState.OWNED) {
        handleDeleter.deleteHandle(handle);
        resourceOwningState = ResourceOwningState.CLEARED;
      }
    }
  }
}

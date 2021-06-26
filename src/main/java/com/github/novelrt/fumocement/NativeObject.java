// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.List;

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
    // Update: Is this really necessary? We don't support Android now thanks to Vulkan 1.2.
    private static final Cleaner NATIVE_OBJECTS_CLEANER = Cleaner.create();

    private final BaseState baseState;

    /**
     * Constructs a new native object with the given handle, the native resource owning state,
     * the disposal method, and a {@link HandleDeleter} deleting the native handle.
     * It can be {@code null} if the {@code owned} parameter is {@code false}.
     *
     * @param handle         the native handle
     * @param owned          whether or not this object owns native resources
     * @param disposalMethod the disposal method to use
     * @param handleDeleter  the {@link HandleDeleter} to use in order to delete native resources,
     *                       which can be {@code null} when {@code owned} is false
     */
    protected NativeObject(@Pointer long handle,
                           boolean owned,
                           DisposalMethod disposalMethod,
                           HandleDeleter handleDeleter) {
        baseState = new BaseState(handle, handleDeleter, ResourceOwningState.fromBoolean(owned));
        if (owned) {
            if (handleDeleter == null) {
                throw new IllegalArgumentException("This NativeObject is owned, but its handleDeleter is null.");
            }
            if (disposalMethod == DisposalMethod.GARBAGE_COLLECTED) {
                NATIVE_OBJECTS_CLEANER.register(this, baseState);
            }
        }
    }

    /**
     * Constructs a new native object with the given handle, the native resource owning state, and
     * a {@link HandleDeleter} deleting the native handle. It can be {@code null} if the
     * {@code owned} parameter is {@code false}. This object will be disposed on garbage collection.
     *
     * @param handle        the native handle
     * @param owned         whether or not this object owns native resources
     * @param handleDeleter the {@link HandleDeleter} to use in order to delete native resources,
     *                      which can be {@code null} when {@code owned} is false
     */
    protected NativeObject(@Pointer long handle, boolean owned, HandleDeleter handleDeleter) {
        this(handle, owned, DisposalMethod.GARBAGE_COLLECTED, handleDeleter);
    }

    /**
     * Gets the native handle.
     * <p>
     * This handle is represented as a {@code long} to support 64-bit pointers.
     *
     * @return the native handle
     * @throws IllegalStateException when the native resource this object holds has been cleared
     */
    protected @Pointer long getHandle() {
        if (baseState.resourceOwningState == ResourceOwningState.CLEARED) {
            throw new IllegalStateException("Cannot get the handle of this NativeObject as its native resource has " +
                                            "already been cleared.");
        }
        return baseState.handle;
    }

    /**
     * Gets the native handle even if this object has been cleared.
     *
     * @return the native handle
     */
    protected @Pointer long getHandleUnsafe() {
        return baseState.handle;
    }

    /**
     * Gets the the current baseState of native resources owned by this object.
     * <p>
     * This determines if there needs to be a native resource releasing process
     * (a {@link HandleDeleter}) once the object is garbage collected.
     *
     * @return a {@link ResourceOwningState} indicating the current baseState of native resources
     * owned by this object
     */
    protected ResourceOwningState getResourceOwningState() {
        return baseState.resourceOwningState;
    }

    protected void registerDeletionState(DeletionState deletionState) {
        baseState.addDeletionState(deletionState);
    }

    /**
     * Clears any native resources this object holds.
     * <p>
     * This does not have any effect when the current resource owning baseState is not
     * {@link ResourceOwningState#OWNED}.
     */
    @Override
    public void close() {
        baseState.deleteAllNativeResources();
    }

    /**
     * Returns a string representation of this object containing the hexadecimal handle
     * and the resource owning baseState.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return String.format("%s{handle=%016X, resourceOwningState=%s}",
                getClass().getSimpleName(),
                baseState.handle,
                baseState.resourceOwningState);
    }

    /**
     * Indicates the baseState for native resources contained in a {@link NativeObject}.
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
        CLEARED;

        public static ResourceOwningState fromBoolean(boolean value) {
            return value ? OWNED : UNOWNED;
        }
    }

    protected abstract static class DeletionState implements Runnable {
        protected abstract void deleteNativeResources();
    }

    private static final class BaseState implements Runnable {
        public final long handle;
        public final HandleDeleter handleDeleter;
        public ResourceOwningState resourceOwningState;
        public @Nullable List<DeletionState> otherDeletionStates;

        private BaseState(long handle, HandleDeleter handleDeleter, ResourceOwningState resourceOwningState) {
            this.handle = handle;
            this.handleDeleter = handleDeleter;
            this.resourceOwningState = resourceOwningState;
        }

        @Override
        public void run() {
            deleteAllNativeResources();
        }

        void addDeletionState(DeletionState deletionState) {
            if (otherDeletionStates == null) {
                otherDeletionStates = new ArrayList<>(1);
            }

            otherDeletionStates.add(deletionState);
        }

        protected void deleteAllNativeResources() {
            if (resourceOwningState == ResourceOwningState.OWNED) {
                handleDeleter.deleteHandle(handle);
                if (otherDeletionStates != null) {
                    for (DeletionState otherDeletionState : otherDeletionStates) {
                        otherDeletionState.deleteNativeResources();
                    }
                }
                resourceOwningState = ResourceOwningState.CLEARED;
            }
        }
    }
}

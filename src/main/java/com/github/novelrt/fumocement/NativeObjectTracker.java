// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Contains {@link NativeObject} instances based on its target:
 * <ul>
 *   <li>{@link Target#OWNED_OBJECTS}: targeting only owned objects</li>
 *   <li>{@link Target#UNOWNED_OBJECTS}: targeting only unowned objects</li>
 * </ul>
 * <p>
 * This acts as a cache to avoid multiple native objects having the same handle.
 * Objects contained in this tracker are stored inside {@link WeakReference}s, thus
 * letting them getting garbage collected.
 *
 * @param <T> the type of {@link NativeObject} this tracker contains
 */
public final class NativeObjectTracker<T extends NativeObject> {
  private final ReferenceQueue<? super T> referenceQueue = new ReferenceQueue<>();
  private final Map<@Pointer Long, HandleTrackingWeakReference<? extends T>> map = new WeakHashMap<>();

  private final NativeObjectFactory<? extends T> factory;
  private final Target target;

  /**
   * Create a new instance of {@link NativeObjectTracker}, with the given
   * {@link NativeObjectFactory}.
   *
   * @param factory the factory used to create native objects
   * @param target  which objects should this tracker track
   */
  public NativeObjectTracker(NativeObjectFactory<? extends T> factory, Target target) {
    this.factory = factory;
    this.target = target;
  }

  /**
   * Gets a native object that owns the given handle if this tracker is targeting
   * owned objects ({@link Target#OWNED_OBJECTS}). If it is not
   * present, the tracker will create it for use later.
   *
   * @param handle the native handle
   * @return a native object of type {@code T} that owns the given handle
   */
  public T getOrCreate(@Pointer("T*") long handle) {
    WeakReference<? extends T> reference = map.get(handle);
    T actualValue = reference.get();

    if (actualValue == null) {
      actualValue = factory.createInstance(handle, target.owned);
      putIntoMap(actualValue);
    }

    return actualValue;
  }

  /**
   * Registers this instance into the tracker.
   *
   * @param instance the instance to register
   * @throws IllegalArgumentException when the resource doesn't own its resource according to
   *                                  {@linkplain #getTarget() this tracker's target}
   * @throws IllegalArgumentException when another instance is present in this tracker with
   *                                  the same handle
   */
  public void register(T instance) {
    if (instance.getResourceOwningState() != target.owningState) {
      throw new IllegalArgumentException("Cannot register this instance as does not own its resource.");
    }

    // We still have to check the reference's value as there might be a chance where
    // this reference might not have been deleted earlier.
    HandleTrackingWeakReference<? extends T> existingValue = map.get(instance.getHandle());
    if (existingValue != null && existingValue.get() != null) {
      throw new IllegalArgumentException("Cannot register this instance as another instance is present " +
                                         "with the same handle.");
    }

    putIntoMap(instance);
  }

  /**
   * Gets the target of this tracker.
   *
   * @return the target of this tracker
   */
  public Target getTarget() {
    return target;
  }

  private void putIntoMap(T instance) {
    long handle = instance.getHandle();
    map.put(handle, new HandleTrackingWeakReference<>(instance, handle, referenceQueue));
    cleanGarbageCollectedObjects();
  }

  private void cleanGarbageCollectedObjects() {
    Reference<?> reference;

    while ((reference = referenceQueue.poll()) != null) {
      HandleTrackingWeakReference<?> handleRef = (HandleTrackingWeakReference<?>) reference;
      map.remove(handleRef.getHandle());
    }
  }

  /**
   * Defines which {@link NativeObject}s should be targeted based on their
   * native resource ownership.
   */
  public enum Target {
    /**
     * Only {@link NativeObject}s that <b>are</b> owning their resources should be targeted.
     *
     * @see NativeObject.ResourceOwningState#OWNED
     */
    OWNED_OBJECTS(true, NativeObject.ResourceOwningState.OWNED),
    /**
     * Only {@link NativeObject}s that <b>are not</b> owning their resources should be targeted.
     *
     * @see NativeObject.ResourceOwningState#UNOWNED
     */
    UNOWNED_OBJECTS(false, NativeObject.ResourceOwningState.UNOWNED);

    private final boolean owned;
    private final NativeObject.ResourceOwningState owningState;

    Target(boolean owned, NativeObject.ResourceOwningState owningState) {
      this.owned = owned;
      this.owningState = owningState;
    }
  }

  private static final class HandleTrackingWeakReference<T> extends WeakReference<T> {
    private final long handle;

    public HandleTrackingWeakReference(T referent, long handle, ReferenceQueue<? super T> q) {
      super(referent, q);
      this.handle = handle;
    }

    public long getHandle() {
      return handle;
    }
  }
}

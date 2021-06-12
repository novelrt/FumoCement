// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

/**
 * Contains tools to manipulate pointers, mainly used for going through an array.
 */
public final class Pointers {
    /**
     * The size of the C {@code uintptr_t} type, which is the value of {@code sizeof(uint_ptr_t)}.
     * This represents the size of a pointer.
     * <p>
     * This value typically changes from 32-bit to 64-bit, and varies by the OS as well.
     */
    public static final int UINTPTR_T_SIZE;
    /**
     * A {@code nullptr} whose value is 0.
     */
    public static final @Pointer long NULLPTR = 0;

    static {
        UINTPTR_T_SIZE = getNativeLongSize();
    }

    private Pointers() {
    }

    // This will be implemented in the generated methods as well.
    private static native int getNativeLongSize();

    /**
     * Returns whether or not this pointer is null (= 0).
     *
     * @param pointer the pointer
     * @return whether or not this pointer is null
     */
    public static boolean isNullPointer(@Pointer long pointer) {
        return pointer == 0;
    }

    /**
     * Returns the resulting pointer once the given pointer advances by the given {@code value}.
     *
     * @param pointer the pointer
     * @param value   the advancement value
     * @return the resulting pointer
     */
    public static @Pointer long advance(@Pointer long pointer, long value) {
        return pointer + value;
    }

    /**
     * Returns the resulting pointer once the given pointer advances by the size of
     * the C type {@code int8_t}, which is 8 bits.
     *
     * @param pointer the pointer
     * @return the resulting pointer
     */
    public static @Pointer long advanceInt8(@Pointer long pointer) {
        return pointer + 8;
    }

    /**
     * Returns the resulting pointer once the given pointer advances by the size of
     * the C type {@code int16_t}, which is 16 bits.
     *
     * @param pointer the pointer
     * @return the resulting pointer
     */
    public static @Pointer long advanceInt16(@Pointer long pointer) {
        return pointer + 16;
    }

    /**
     * Returns the resulting pointer once the given pointer advances by the size of
     * the C type {@code int32_t}, which is 32 bits.
     *
     * @param pointer the pointer
     * @return the resulting pointer
     */
    public static @Pointer long advanceInt32(@Pointer long pointer) {
        return pointer + 32;
    }

    /**
     * Returns the resulting pointer once the given pointer advances by the size of
     * the C type {@code int64_t}, which is 64 bits.
     *
     * @param pointer the pointer
     * @return the resulting pointer
     */
    public static @Pointer long advanceInt64(@Pointer long pointer) {
        return pointer + 64;
    }

    /**
     * Returns the resulting pointer once the given pointer advances by the size of
     * the C type {@code uintptr_t}, which is platform-dependent (usually 32 or 64).
     *
     * @param pointer the pointer
     * @return the resulting pointer
     * @see Pointers#UINTPTR_T_SIZE
     */
    public static @Pointer long advanceUintptrT(@Pointer long pointer) {
        return pointer + UINTPTR_T_SIZE;
    }
}

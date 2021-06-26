// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import java.lang.annotation.*;

/**
 * Indicates that the annotated signed type is a unsigned type coming from the native land.
 * <p>
 * On Kotlin, you can use the {@code ULong}, {@code UInt}, {@code UShort} and {@code UByte} types
 * to restore the unsigned semantics.
 * <p>
 * On Java, you're screwed! Nice meme - FumoCement<br>
 * ...That is, unless you use the unsigned number methods in Java 8, such as {@link Long#divideUnsigned(long, long)}.
 */
@Retention(RetentionPolicy.CLASS) // CLASS to make sure decompilers see this too!
@Target(ElementType.TYPE_USE)
@Documented
public @interface Unsigned {}

// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import java.lang.annotation.*;

/**
 * Means that the annotated type is a unsigned type coming from the native land.
 * <p>
 * On Kotlin, you can use the {@code ULong}, {@code UInt}, {@code UShort} and {@code UByte} types
 * to restore the unsigned semantics.
 * <p>
 * On Java, you're screwed! Nice meme - FumoCement
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
@Documented
public @interface Unsigned {}

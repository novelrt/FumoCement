// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

import java.lang.annotation.*;

/**
 * Indicates that the following type — most likely a {@code long} — represents
 * a pointer in the native land.
 * <p>
 * Generics arguments (such as {@code T}, {@code U}, etc.) are allowed when this
 * attribute is used inside a generic member.
 */
@Retention(RetentionPolicy.CLASS) // CLASS to make sure decompilers see this too!
@Target({ElementType.TYPE_USE, ElementType.LOCAL_VARIABLE})
@Documented
public @interface Pointer {
  /**
   * Returns what this type represents in the native land, in C syntax.
   * <p>
   * <b>Examples:</b> {@code SomeStruct*}, {@code Transform**}
   *
   * @return the C-style representation of the type in the native land
   */
  String value() default "void*";
}

// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

/**
 * <h2>A library to make Java interop easier.</h2>
 * <img src="https://i.imgur.com/NiFHFj6.jpeg" width="500" alt="FumoCement logo" />
 * <p>
 * A java and JNI-focused framework library for generating high-performance bindings.
 * Primarily designed for use with the NovelRT engine and ClangSharp.
 * <h2>Performance strategy</h2>
 * One of the goals of FumoCement is to generate performant code to use native APIs.
 * Being designed for game engines such as NovelRT, FumoCement achieves this goal
 * by applying this golden rule:
 * <blockquote><b>Do as little JNI calls as possible; do as much Java calls possible.</b></blockquote>
 * This counts for both JNI calls inside the C++ code, as well as calls to {@code native}
 * methods in Java. FumoCement accomplishes that by:
 * <ul>
 *   <li>
 *     <b>Using {@code native} static methods instead of {@code native} instance methods.</b><br>
 *     By passing the handle directly to the static method, the JNI glue code does not require to call
 *     the {@link com.github.novelrt.fumocement.NativeObject#getHandle()} method, avoiding a
 *     consequent overhead from a JNI call.
 *   </li>
 *   <li>
 *     <b>Caching all instances of {@code jclass}, {@code jmethodID} and {@code jfieldID}</b><br>
 *     Using some C++ template magic, JNI-related values are lazily cached using static initialization,
 *     making the cache really efficient without any runtime cost
 *   </li>
 *   <li>
 *     <b>Using field offsets for struct fields inside a struct</b><br>
 *     With structs being basically a bag of fields with offsets, we can create an instance
 *     of a struct on the java side and compute the handle by adding the offset. Therefore,
 *     we can drastically reduce the count of JNI calls.
 *   </li>
 *   <li><b>...And many more!</b></li>
 * </ul>
 * <h2>Why this name?</h2>
 * <b>WHY NOT?</b>
 */
package com.github.novelrt.fumocement;

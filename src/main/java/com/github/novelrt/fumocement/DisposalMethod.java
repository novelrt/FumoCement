// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

/**
 * Defines how native resources must be disposed.
 */
public enum DisposalMethod {
    /**
     * Native resources will be disposed after the {@link NativeObject} has been garbage collected.
     */
    GARBAGE_COLLECTED,
    /**
     * Native resources will be disposed manually, using {@link NativeObject#close()}.
     */
    MANUAL
}

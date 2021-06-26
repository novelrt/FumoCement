// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

package com.github.novelrt.fumocement;

public enum StringDeletionBehaviour {
    DELETE(true),
    NO_DELETE(false);

    private final boolean deletingString;

    StringDeletionBehaviour(boolean deletingString) {
        this.deletingString = deletingString;
    }

    public boolean isDeletingString() {
        return deletingString;
    }
}

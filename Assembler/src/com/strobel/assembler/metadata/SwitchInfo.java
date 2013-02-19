/*
 * SwitchInfo.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.Instruction;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 3:21 PM
 */
public final class SwitchInfo {
    private int[] _keys;
    private Instruction _defaultTarget;
    private Instruction[] _targets;

    public SwitchInfo() {
    }

    public SwitchInfo(final Instruction defaultTarget, final Instruction[] targets) {
        _keys = null;
        _defaultTarget = defaultTarget;
        _targets = targets;
    }

    public SwitchInfo(final int[] keys, final Instruction defaultTarget, final Instruction[] targets) {
        _keys = keys;
        _defaultTarget = defaultTarget;
        _targets = targets;
    }

    public boolean hasKeys() {
        return _keys != null;
    }

    public int[] getKeys() {
        return _keys;
    }

    public Instruction getDefaultTarget() {
        return _defaultTarget;
    }

    public Instruction[] getTargets() {
        return _targets;
    }

    public void setKeys(final int[] keys) {
        _keys = keys;
    }

    public void setDefaultTarget(final Instruction defaultTarget) {
        _defaultTarget = defaultTarget;
    }

    public void setTargets(final Instruction[] targets) {
        _targets = targets;
    }
}

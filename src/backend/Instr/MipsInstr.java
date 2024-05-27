package backend.Instr;

import backend.MipsBlock;

import java.util.ArrayList;

public abstract class MipsInstr {

    public String tag;
    public MipsBlock parent;

    public MipsInstr(MipsBlock parent, String tag) {
        this.tag = tag;
        this.parent = parent;
        parent.addInstr(this);
    }

    public abstract String string();
}

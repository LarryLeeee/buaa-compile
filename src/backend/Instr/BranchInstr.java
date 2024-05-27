package backend.Instr;

import backend.MipsBlock;

public class BranchInstr extends MipsInstr {
    public String src1;
    public String src2;
    public String label;

    public BranchInstr(MipsBlock parent, String tag, String src1, String src2, String label) {
        super(parent, tag);
        this.src1 = src1;
        this.src2 = src2;
        this.label = label;
    }

    @Override
    public String string() {
        return "\t"+tag + " " + src1 + ", " + src2 + ", " + label;
    }
}

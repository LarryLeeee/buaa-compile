package backend.Instr;

import backend.MipsBlock;

public class MulDivInstr extends MipsInstr {
    public String src1;
    public String src2;

    public MulDivInstr(MipsBlock parent, String tag, String src1, String src2) {
        super(parent, tag);
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String string() {
        return "\t"+tag + " " + src1 + ", " + src2;
    }
}

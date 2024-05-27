package backend.Instr;

import backend.MipsBlock;

public class RegRegInstr extends MipsInstr {
    public String dst;
    public String src1;
    public String src2;

    public RegRegInstr(MipsBlock parent, String tag, String dst, String src1, String src2) {
        super(parent, tag);
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String string() {
        return "\t"+tag + " " + dst + ", " + src1 + ", " + src2;
    }
}

package backend.Instr;

import backend.MipsBlock;

public class LuiInstr extends MipsInstr {
    public String dst;
    public int imm;

    public LuiInstr(MipsBlock parent, String tag, String dst, int imm) {
        super(parent, tag);
        this.dst = dst;
        this.imm = imm;
    }

    @Override
    public String string() {
        return "\tlui " + dst + " " + imm;
    }
}

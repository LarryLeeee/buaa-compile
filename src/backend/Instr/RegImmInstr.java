package backend.Instr;

import backend.MipsBlock;

public class RegImmInstr extends MipsInstr {
    private String dst;
    private String src;
    public int imm;

    public RegImmInstr(MipsBlock parent, String tag, String dst, String src, int imm) {
        super(parent, tag);
        this.tag = tag;
        this.dst = dst;
        this.src = src;
        this.imm = imm;
    }

    @Override
    public String string() {
        return "\t"+tag + " " + dst + ", " + src + ", " + imm;
    }
}

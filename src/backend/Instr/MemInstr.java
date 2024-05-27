package backend.Instr;

import backend.MipsBlock;

public class MemInstr extends MipsInstr {
    public String reg;
    public String addr;
    public int offset;

    public MemInstr(MipsBlock parent, String tag, String reg, String addr, int offset) {
        super(parent, tag);
        this.reg = reg;
        this.addr = addr;
        this.offset = offset;
    }

    @Override
    public String string() {
        return "\t" + tag + " " + reg + ", " + offset + "(" + addr + ")";
    }
}

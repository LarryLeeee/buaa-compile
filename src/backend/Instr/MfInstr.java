package backend.Instr;

import backend.MipsBlock;

public class MfInstr extends MipsInstr {
    public String dst;

    public MfInstr(MipsBlock parent, String tag, String dst) {
        super(parent, tag);
        this.dst = dst;
    }

    @Override
    public String string() {
        return "\t" + tag + " " + dst;
    }
}

package backend.Instr;

import backend.MipsBlock;

public class SyscallInstr extends MipsInstr {

    public int type;

    public SyscallInstr(MipsBlock parent, String tag, String type) {
        super(parent, tag);
        if (type.equals("getint")) {
            this.type = 5;
        } else if (type.equals("putint")) {
            this.type = 1;
        } else {
            this.type = 4;
        }
    }

    @Override
    public String string() {
        return "\tli $v0, " + type + "\n" + "\tsyscall";
    }

}

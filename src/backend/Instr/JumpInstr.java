package backend.Instr;

import backend.MipsBlock;

public class JumpInstr extends MipsInstr {
    public String label;

    public JumpInstr(MipsBlock parent, String tag, String label) {
        super(parent, tag);
        this.label = label;
    }

    @Override
    public String string() {
        switch (tag) {
            case "j":
                return "\tj " + label;
            case "jal":
                return "\tjal " + label;
            case "jr":
                return "\tjr $ra";
            default:
                return null;
        }
    }
}

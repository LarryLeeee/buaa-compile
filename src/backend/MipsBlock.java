package backend;

import backend.Instr.MipsInstr;

import java.util.ArrayList;

public class MipsBlock {

    public String name;
    public MipsFunc parent;
    public ArrayList<MipsInstr> mipsInstrs = new ArrayList<>();

    public MipsBlock(String name, MipsFunc parent) {
        this.name = name;
        this.parent = parent;
        parent.addBlock(this);
    }

    public void addInstr(MipsInstr mipsInstr) {
        mipsInstrs.add(mipsInstr);
    }

    public void getOutPuts(ArrayList<String> outputs) {
        outputs.add(name + ":");
        for (MipsInstr mipsInstr : mipsInstrs) {
            outputs.add(mipsInstr.string());
        }
    }

}

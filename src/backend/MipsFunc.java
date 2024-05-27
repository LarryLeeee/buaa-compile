package backend;

import java.util.ArrayList;

public class MipsFunc {
    public String name;
    public MipsModule parent;
    public ArrayList<MipsBlock> mipsBlocks = new ArrayList<>();
    public int offset = 0;

    public MipsFunc(String name, MipsModule parent) {
        parent.addFunc(this);
        this.name = name;
        this.parent = parent;
    }

    public void addBlock(MipsBlock mipsBlock) {
        mipsBlocks.add(mipsBlock);
    }

    public void getOutPuts(ArrayList<String> outputs) {
        outputs.add(name + ":");
        for (MipsBlock mipsBlock : mipsBlocks) {
            mipsBlock.getOutPuts(outputs);
        }
    }

    public void addOff(int add) {
        offset = offset + add;
    }

    public void subOff(int sub) {
        offset = offset - sub;
    }

}

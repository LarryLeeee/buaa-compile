package ir.Value.Instruction.Mem;

import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Instruction.Instruction;
import ir.Value.Instruction.TAG;

import java.util.ArrayList;

public abstract class MemInst extends Instruction {

    public MemInst(Type type, BasicBlock parent, int numOp, TAG tag) {
        super("", type, parent, numOp, tag);
    }

    public abstract void getOutputs(ArrayList<String> outputs);
}

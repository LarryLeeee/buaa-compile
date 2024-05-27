package ir.Value.Instruction.Terminate;

import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Instruction.Instruction;
import ir.Value.Instruction.TAG;

import java.util.ArrayList;

public class TerminateInst extends Instruction {
    public TerminateInst(Type type, BasicBlock parent, int numOp, TAG tag) {
        super("", type, parent, numOp, tag);
    }


    public void getOutputs(ArrayList<String> outputs) {

    }
}

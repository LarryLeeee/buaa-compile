package ir.Value.Instruction.Terminate;

import ir.Type.Type;
import ir.Type.VoidType;
import ir.Value.BasicBlock;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class RetInst extends TerminateInst {

    public RetInst(Type type, BasicBlock parent, Value value) {
        super(type, parent, 1, TAG.ret);
        addOperand(value);
        if (value != null) {
            value.addUser(this);
        }
    }

    public void getOutputs(ArrayList<String> outputs) {
        if (type instanceof VoidType) {
            outputs.add("\tret void");
        } else {
            outputs.add("\t" + "ret i32 " + operands.get(0).getName());
        }
    }

}

package ir.Value.Instruction.Mem;

import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class ZextInst extends MemInst {

    public ZextInst(String name, Type type, BasicBlock parent, int numOp, Value value) {
        super(type, parent, numOp, TAG.zext);
        this.name = name;
        addOperand(value);
        value.addUser(this);
    }

    public void getOutputs(ArrayList<String> outputs) {
        outputs.add("\t" + name + " = " + "zext i1 " + operands.get(0).name + " to i32");
    }

}

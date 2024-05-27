package ir.Value.Instruction.IO;

import ir.Type.IntegerType;
import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Instruction.Instruction;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class Putint extends Instruction {
    public Putint(Value value, BasicBlock parent) {
        super(value.name, IntegerType.i32, parent, 0, TAG.putint);
        addOperand(value);
        value.addUser(this);
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        outputs.add("\t" + "call void @putint(i32 " + name + ")");
    }
}

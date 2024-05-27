package ir.Value.Instruction.IO;

import ir.Type.IntegerType;
import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Instruction.Instruction;
import ir.Value.Instruction.TAG;

import java.util.ArrayList;

public class Getint extends Instruction {
    public Getint(String name, BasicBlock parent) {
        super(name, IntegerType.i32, parent, 0, TAG.getint);
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        outputs.add("\t" + name + " = call i32 @getint()");
    }
}

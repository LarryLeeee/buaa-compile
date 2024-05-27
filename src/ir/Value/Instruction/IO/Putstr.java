package ir.Value.Instruction.IO;

import ir.Type.IntegerType;
import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Instruction.Instruction;
import ir.Value.Instruction.TAG;
import ir.Value.Str;

import java.util.ArrayList;

public class Putstr extends Instruction {

    public Str str;

    public Putstr(String name, Str str, BasicBlock parent) {
        super(name, IntegerType.i8, parent, 0, TAG.putstr);
        this.str = str;
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        String sb = "\t" + name + " = getelementptr inbounds [" + str.length +
                " x i8], [" + str.length + " x i8]* " + str.name + ", i32 0, i32 0";
        outputs.add(sb);
        outputs.add("\tcall void @putstr(i8* ".concat(name).concat(")"));
    }
}

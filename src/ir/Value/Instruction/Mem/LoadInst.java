package ir.Value.Instruction.Mem;

import ir.Type.PointerType;
import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class LoadInst extends MemInst {

    public LoadInst(String name, BasicBlock parent, Value value) {
        super(((PointerType) (value.type)).pointType, parent, 2, TAG.load);
        this.name = name;
        addOperand(value);
        value.addUser(this);
    }

    public void getOutputs(ArrayList<String> outputs) {
        outputs.add("\t" + name + " = load " + type.string() + ", " + operands.get(0).type.string() + " " + operands.get(0).name);
    }

}

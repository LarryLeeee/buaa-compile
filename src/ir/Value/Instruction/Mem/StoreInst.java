package ir.Value.Instruction.Mem;

import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Constant.ConstantInt;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class StoreInst extends MemInst {

    public Value left;
    public Value right;

    //把left值给right
    public StoreInst(BasicBlock parent, Value left, Value right) {
        super(null, parent, 0, TAG.store);
        this.left = left;
        this.right = right;
        addOperand(left);
        left.addUser(this);
        addOperand(right);
        right.addUser(this);
    }

    public void getOutputs(ArrayList<String> outputs) {
        outputs.add("\t" + "store " + left.type.string() + " " + left.name + ", " + right.type.string() + " " + right.name);
    }

}

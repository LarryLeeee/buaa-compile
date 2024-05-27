package ir.Value.Instruction.Mem;

import frontend.MyPair;
import ir.Type.IntegerType;
import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class PhiInst extends MemInst {

    public PhiInst(String name, BasicBlock parent, ArrayList<MyPair<BasicBlock, Value>> phis) {
        super(IntegerType.i32, parent, phis.size(), TAG.phi);
        this.name = name;
        for (MyPair<BasicBlock, Value> phi : phis) {
            addOperand(phi.getValue());
            phi.getValue().addUser(this);
            addOperand(phi.getKey());
            phi.getKey().addUser(this);
        }
    }

    public void addPhi(ArrayList<MyPair<BasicBlock, Value>> phis){
        for (MyPair<BasicBlock, Value> phi : phis) {
            addOperand(phi.getValue());
            phi.getValue().addUser(this);
            addOperand(phi.getKey());
            phi.getKey().addUser(this);
        }
    }

    public PhiInst(String name, BasicBlock parent) {
        super(IntegerType.i32, parent, 0, TAG.phi);
        this.name = name;
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t").append(this.name).append(" = phi i32 ");
        for (int i = 0; i < operands.size(); i += 2) {
            sb.append("[ ")
                    .append(operands.get(i).name)
                    .append(", ")
                    .append(operands.get(i + 1).name)
                    .append(" ],");
        }
        sb.deleteCharAt(sb.length() - 1);
        outputs.add(sb.toString());
    }
}

package ir.Value.Instruction.Terminate;

import ir.Value.BasicBlock;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class BrInst extends TerminateInst {

    public String judge;

    public BrInst(Value cond, BasicBlock trueBlock, BasicBlock falseBlock, BasicBlock parent) {
        super(null, parent, 1, TAG.br);
        addOperand(cond);
        addOperand(trueBlock);
        if (trueBlock != null) {
            parent.addSuc(trueBlock);
            trueBlock.addPre(parent);
        }
        addOperand(falseBlock);
        if (cond != null) {
            numOp = 3;
            cond.addUser(this);
            parent.addSuc(falseBlock);
            falseBlock.addPre(parent);
        }
    }

    public void set(BasicBlock block) {
        operands.set(1, block);
        block.addPre(parent);
        parent.addSuc(block);
    }

    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t").append("br ");
        if (numOp == 1) {
            sb.append(operands.get(1).type.string()).append(" ").append(operands.get(1).name);
        } else {
            sb.append("i1 ").append(operands.get(0).name).append(",").append(" ")
                    .append(operands.get(1).type.string()).append(" ").append(operands.get(1).name).append(",").append(" ")
                    .append(operands.get(2).type.string()).append(" ").append(operands.get(2).name);
        }
        outputs.add(sb.toString());
    }

}

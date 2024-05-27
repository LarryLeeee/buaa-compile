package ir.Value.Instruction;

import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Value;

import java.util.ArrayList;

public class BinaryInst extends Instruction {

    public BinaryInst(String name, Type type, BasicBlock parent, Value left, Value right, TAG tag) {
        super(name, type, parent, 2, tag);
        addOperand(left);
        left.addUser(this);
        addOperand(right);
        right.addUser(this);
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t").append(name).append(" = ");
        switch (this.tag) {
            case add:
                sb.append("add i32 ");
                break;
            case sub:
                sb.append("sub i32 ");
                break;
            case mul:
                sb.append("mul i32 ");
                break;
            case sdiv:
                sb.append("sdiv i32 ");
                break;
            case mod:
                sb.append("srem i32 ");
                break;
            case lt:
                sb.append("icmp slt i32 ");
                break;
            case le:
                sb.append("icmp sle i32 ");
                break;
            case ge:
                sb.append("icmp sge i32 ");
                break;
            case gt:
                sb.append("icmp sgt i32 ");
                break;
            case eq:
                sb.append("icmp eq i32 ");
                break;
            case ne:
                sb.append("icmp ne i32 ");
                break;
            case and:
                sb.append("and i32 ");
                break;
            default:
                break;
        }
        sb.append(operands.get(0).name).append(",").append(operands.get(1).name);
        outputs.add(sb.toString());
    }

}

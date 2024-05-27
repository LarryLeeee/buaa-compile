package ir.Value.Instruction.Terminate;

import ir.Type.FunctionType;
import ir.Type.VoidType;
import ir.Value.BasicBlock;
import ir.Value.Function;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class CallInst extends TerminateInst {

    public Function function;
    public ArrayList<Value> params;

    //name是分配的虚拟寄存器
    public CallInst(String name, BasicBlock parent, ArrayList<Value> params, Function function) {
        super(function.getRetType(), parent, params.size() + 1, TAG.call);
        this.function = function;
        addOperand(function);
        this.name = name;
        this.params = params;
        for (Value param : params) {
            addOperand(param);
            param.addUser(this);
        }
    }

    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t");
        Function function = ((Function) (operands.get(0)));
        if (((FunctionType) (function.type)).retType instanceof VoidType) {
            sb.append("call void @").append(function.name).append("(");
        } else {
            sb.append(name).append(" = call i32 @").append(function.name).append("(");
        }
        for (int i = 1; i < operands.size(); i++) {
            sb.append(operands.get(i).type.string()).append(" ").append(operands.get(i).name);
            if (i < operands.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        outputs.add(sb.toString());
    }

}

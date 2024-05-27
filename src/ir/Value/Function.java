package ir.Value;

import frontend.MyPair;
import ir.Type.FunctionType;
import ir.Type.Type;
import ir.Type.VoidType;

import java.util.ArrayList;

public class Function extends Value {

    public ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
    public ArrayList<MyPair<String, Type>> params = new ArrayList<>();
    public boolean recursive = false;
    public int time = 0;//被调用次数

    public Function(String name, Type type, Module parent) {
        super(name, type);
        parent.addFunction(this);
        ArrayList<Type> paramTypes = ((FunctionType) (this.type)).params;
        for (int i = 0; i < paramTypes.size(); i++) {
            params.add(new MyPair<>("%v" + i, paramTypes.get(i)));
        }
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.string()).append(name).append("(");
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getValue().string()).append(" ").append(params.get(i).getKey());
            if (i < params.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("){");
        outputs.add(sb.toString());
        for (BasicBlock basicBlock : basicBlocks) {
            outputs.add("");
            basicBlock.getOutputs(outputs);
        }
//        if (((FunctionType) (type)).retType instanceof VoidType) {
//            outputs.add("\tret void");
//        }
        outputs.add("}");
    }

    public Type getRetType() {
        return ((FunctionType) type).retType;
    }

}

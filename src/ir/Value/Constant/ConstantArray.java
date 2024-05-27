package ir.Value.Constant;

import ir.Type.Type;

import java.util.ArrayList;

public class ConstantArray extends Constant {

    public ArrayList<Constant> constantArrays;

    public ConstantArray(Type type, ArrayList<Constant> constantArrays) {
        super(type, constantArrays.size());
        this.constantArrays = constantArrays;
    }

    public String string() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.string()).append("[");
        int size = constantArrays.size();
        for (int i = 0; i < size; i++) {
            sb.append(constantArrays.get(i).string());
            if (i < size - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}

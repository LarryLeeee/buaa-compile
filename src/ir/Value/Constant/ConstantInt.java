package ir.Value.Constant;

import ir.Type.IntegerType;
import ir.Type.Type;

public class ConstantInt extends Constant {
    public int value;

    public ConstantInt(Type type, int numOp, int value) {
        super(type, numOp);
        this.value = value;
        this.name = String.valueOf(value);
    }

    public ConstantInt gen(int value) {
        return new ConstantInt(IntegerType.i32, 0, value);
    }

    public String string() {
        return "i32 " + value;
    }

}

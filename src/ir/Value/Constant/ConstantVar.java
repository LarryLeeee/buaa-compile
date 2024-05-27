package ir.Value.Constant;

import ir.Type.Type;
import ir.Value.User;

public class ConstantVar extends User {

    public int value;

    public ConstantVar(String name, Type type, int numOp, int value) {
        super(name, type, numOp);
        this.value = value;
    }

}

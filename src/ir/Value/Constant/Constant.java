package ir.Value.Constant;

import ir.Type.Type;
import ir.Value.User;

public abstract class Constant extends User {

    public Constant(Type type, int numOp) {
        super("", type, numOp);
    }

    public abstract String string();
}

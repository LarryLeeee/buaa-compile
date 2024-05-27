package ir.Value;

import ir.Type.Type;

import java.util.ArrayList;

public class User extends Value {

    public Function parent;
    public ArrayList<Value> operands;
    public int numOp;

    public User(String name, Type type, int numOp) {
        super(name, type);
        this.numOp = numOp;
        operands = new ArrayList<>();
    }

    public User(String name, Type type) {
        super(name, type);
    }

    public void addOperand(Value value) {
        operands.add(value);
    }
}

package ir.Value;

import ir.Type.Type;
import ir.Value.Constant.ConstantInt;

import java.util.ArrayList;

public class Value {

    public String name;
    public Type type;
    public ArrayList<User> users;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        users = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConstInt() {
        return this instanceof GlobalVariable &&
                ((GlobalVariable) this).value instanceof ConstantInt && ((GlobalVariable) this).isConst;
    }

    public void addUser(User user) {
        users.add(user);
    }

}

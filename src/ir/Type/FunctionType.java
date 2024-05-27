package ir.Type;

import java.util.ArrayList;

public class FunctionType extends Type {
    public Type retType;
    public ArrayList<Type> params;

    public FunctionType(Type retType, ArrayList<Type> params) {
        this.retType = retType;
        this.params = params;
    }

    @Override
    public String string() {
        return "define dso_local " + retType.string()+" @";
    }
}

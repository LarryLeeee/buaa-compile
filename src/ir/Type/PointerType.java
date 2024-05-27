package ir.Type;

public class PointerType extends Type {

    public Type pointType;//gv、alloca、gep都是指针类型

    public PointerType(Type type) {
        pointType = type;
    }

    public String string() {
        return pointType.string() + "*";
    }
}

package ir.Type;

public class VoidType extends Type {

    public static VoidType voidType = new VoidType();

    public VoidType() {

    }

    public int getSize() {
        return 0;
    }

    public String string(){
        return "void";
    }
}

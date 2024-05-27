package ir.Type;

public class LabelType extends Type {

    public static Type labeltype = new LabelType();

    public LabelType() {

    }

    public String string() {
        return "label ";
    }

    public int getSize() {
        return 0;
    }
}

package ir.Type;

public class IntegerType extends Type {
    public static final IntegerType i32 = new IntegerType(32);//int
    public static final IntegerType i8 = new IntegerType(8);//char
    public static final IntegerType i1 = new IntegerType(1);//bool
    private int numBits;

    public IntegerType(int numBits) {
        this.numBits = numBits;
    }

    public int getSize() {
        return 0;
    }

    public int getNumBits() {
        return numBits;
    }

    public String string() {
        return (numBits == 32) ? "i32" : "i1";
    }
}

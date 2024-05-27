package ir.Type;

public abstract class Type {

    public boolean isIntegerTy() {
        return this instanceof IntegerType;
    }

    public boolean isI32() {
        return this.isIntegerTy()&&((IntegerType) this).getNumBits() == 32;
    }

    public boolean isI1() {
        return this.isIntegerTy()&&((IntegerType) this).getNumBits() == 1;
    }

    public abstract String string();

}
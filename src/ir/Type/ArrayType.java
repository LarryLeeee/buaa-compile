package ir.Type;

public class ArrayType extends Type {

    public int numOfElement;
    public Type typeOfElement;
    public int size;//一共有几个int，用于后端

    public ArrayType(int numOfElement, Type typeOfElement) {
        this.numOfElement = numOfElement;
        this.typeOfElement = typeOfElement;
        size = (typeOfElement instanceof IntegerType) ? numOfElement : ((ArrayType) typeOfElement).size * numOfElement;
    }

    public String string() {
        return "[" + numOfElement + " x " + typeOfElement.string() + "]";
    }
}

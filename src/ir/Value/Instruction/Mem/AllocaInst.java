package ir.Value.Instruction.Mem;

import ir.Type.ArrayType;
import ir.Type.IntegerType;
import ir.Type.PointerType;
import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Constant.ConstantArray;
import ir.Value.Instruction.TAG;

import java.util.ArrayList;

public class AllocaInst extends MemInst {

    public boolean isConst = false;
    public ConstantArray constantArray;
    public Type rType;

    //这里name既是指令的名字，也代表着变量名
    public AllocaInst(String name, Type type, BasicBlock parent) {
        super(new PointerType(type), parent, 1, TAG.alloca);
        this.name = name;
    }

    public void getOutputs(ArrayList<String> outputs) {
        outputs.add("\t" + name + " = alloca " + ((PointerType) (type)).pointType.string());
    }

    public boolean isInteger() {
        return ((PointerType) type).pointType instanceof IntegerType;
    }

    public boolean isArray() {
        Type pType = ((PointerType) type).pointType;
        if (pType instanceof ArrayType) {
            return ((ArrayType) pType).typeOfElement instanceof IntegerType;
        } else {
            return false;
        }
    }

    public int getSize() {
        if (isInteger()) {
            return 1;
        } else if (isArray()) {
            return ((ArrayType) ((PointerType) type).pointType).numOfElement;
        } else if (isTwoDim()) {
            ArrayType type1 = (ArrayType) ((PointerType) type).pointType;
            ArrayType type2 = (ArrayType) (type1.typeOfElement);
            return type1.size * type2.size;
        } else if (isPointer()) {
            return ((ArrayType) rType).numOfElement;
        } else if (isDimPointer()) {
            ArrayType type1 = (ArrayType) rType;
            ArrayType type2 = (ArrayType) (type1.typeOfElement);
            return type1.size * type2.size;
        } else return 0;
    }

    public boolean isTwoDim() {
        Type pType = ((PointerType) type).pointType;
        if (pType instanceof ArrayType) {
            return ((ArrayType) pType).typeOfElement instanceof ArrayType;
        } else {
            return false;
        }
    }

    public boolean isPointer() {
        Type pType = ((PointerType) type).pointType;
        if (pType instanceof PointerType) {
            return ((PointerType) pType).pointType instanceof IntegerType;
        } else {
            return false;
        }
    }

    public boolean isDimPointer() {
        Type pType = ((PointerType) type).pointType;
        if (pType instanceof PointerType) {
            return ((PointerType) pType).pointType instanceof ArrayType;
        } else {
            return false;
        }
    }

}

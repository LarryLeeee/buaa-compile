package ir.Value.Instruction.Mem;

import frontend.MyPair;
import ir.Type.ArrayType;
import ir.Type.IntegerType;
import ir.Type.PointerType;
import ir.Type.Type;
import ir.Value.BasicBlock;
import ir.Value.Constant.ConstantInt;
import ir.Value.Instruction.TAG;
import ir.Value.Value;

import java.util.ArrayList;

public class GepInst extends MemInst {

    public ArrayList<Value> indexes;//第一个是pointerType，后面的是index;此外，根本不需要Type，因为type是pointerType指向的类型

    public GepInst(String name, Type type, BasicBlock parent, ArrayList<Value> indexes) {
        super(type, parent, indexes.size(), TAG.gep);
        this.name = name;
        this.indexes = indexes;
        for (Value value : indexes) {
            addOperand(value);
            if (!(value instanceof ConstantInt)) {
                value.addUser(this);
            }
        }
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t").append(name).append("= getelementptr ")
                .append(((PointerType) (indexes.get(0).type)).pointType.string());
        for (Value value : indexes) {
            sb.append(", ").append(value.type.string()).append(" ").append(value.name);
        }
        outputs.add(sb.toString());
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
        } else if (isArray() && isTwoDim()) {
            return ((ArrayType) ((PointerType) type).pointType).size;
        } else return 0;
    }

    public int getSize2() {
        if (isTwoDim()) {
            ArrayType type1 = (ArrayType) ((PointerType) type).pointType;
            ArrayType type2 = (ArrayType) (type1.typeOfElement);
            return type2.size;
        } else return 0;
    }

    public int getOff() {
        if (indexes.get(1) instanceof ConstantInt) {
            return ((ConstantInt) indexes.get(1)).value;
        } else return 0;
    }

    public int getOff2() {
        if (indexes.size() < 3) {
            return 0;
        } else if (indexes.get(2) instanceof ConstantInt) {
            return ((ConstantInt) indexes.get(2)).value;
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

    public boolean is0_1() {
        if (indexes.get(1) instanceof ConstantInt) {
            return ((ConstantInt) indexes.get(1)).value == 0;
        } else {
            return false;
        }
    }

    public boolean is0_2() {
        if (indexes.get(2) instanceof ConstantInt) {
            return ((ConstantInt) indexes.get(2)).value == 0;
        } else {
            return false;
        }
    }

}

package ir.Value;

import ir.Type.ArrayType;
import ir.Type.IntegerType;
import ir.Type.PointerType;
import ir.Type.Type;
import ir.Value.Constant.Constant;
import ir.Value.Constant.ConstantArray;
import ir.Value.Constant.ConstantInt;

import java.util.ArrayList;

public class GlobalVariable extends User {

    //全局变量都是pointerType
    public Module parent;
    public boolean isConst;
    public Constant value;
    public ArrayList<Integer> values=new ArrayList<>();

    public GlobalVariable(String name, Type type, boolean isConst, Constant value, Module parent) {
        super(name, new PointerType(type));
        this.isConst = isConst;
        this.value = value;
        this.parent = parent;
        parent.addGlobalVariable(this);
        if (value instanceof ConstantInt) {
            values.add(((ConstantInt) value).value);
        } else if (value instanceof ConstantArray) {
            if (((ArrayType) value.type).typeOfElement instanceof IntegerType) {
                for (Constant constant : ((ConstantArray) value).constantArrays) {
                    values.add(((ConstantInt) constant).value);
                }
            } else {
                for (Constant constantArr : ((ConstantArray) value).constantArrays) {
                    for (Constant c : ((ConstantArray) constantArr).constantArrays) {
                        values.add(((ConstantInt) c).value);
                    }
                }
            }
        }
    }

    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = dso_local ");
        if (isConst) {
            sb.append(" constant ");
        } else {
            sb.append(" global ");
        }
        PointerType type = (PointerType) this.type;
        if (type.pointType instanceof IntegerType) {
            sb.append(type.pointType.string()).append(" ");
            if (value == null) {
                sb.append(0);
            } else {
                sb.append(((ConstantInt) value).value);
            }
        } else {
            if (value == null) {
                sb.append(type.pointType.string()).append(" zeroinitializer");
            } else {
                sb.append(value.string());
            }
        }
        outputs.add(sb.toString());
    }

    public int getValue() {
        return ((ConstantInt) value).value;
    }

}

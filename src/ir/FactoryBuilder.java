package ir;

import ir.Type.*;
import ir.Value.*;
import ir.Value.Constant.Constant;
import ir.Value.Constant.ConstantVar;
import ir.Value.Instruction.BinaryInst;
import ir.Value.Instruction.IO.Getint;
import ir.Value.Instruction.IO.Putint;
import ir.Value.Instruction.IO.Putstr;
import ir.Value.Instruction.Mem.*;
import ir.Value.Instruction.TAG;
import ir.Value.Instruction.Terminate.BrInst;
import ir.Value.Instruction.Terminate.CallInst;
import ir.Value.Instruction.Terminate.RetInst;

import java.util.ArrayList;

public class FactoryBuilder {

    public FactoryBuilder() {

    }

    public Function function(String name, Type retType, ArrayList<Type> funcParams, Module module) {
        return new Function(name, new FunctionType(retType, funcParams), module);
    }

    public ConstantVar constantVar(String name, int value) {
        return new ConstantVar(name, IntegerType.i32, 0, value);
    }

//    public ConstantArray constantArray(Type type, ArrayList<Constant> constantArrays) {
//
//    }

    public GlobalVariable globalVariable(String name, Type type, boolean isConst, Constant value, Module parent) {
        return new GlobalVariable(name, type, isConst, value, parent);
    }

    //instructionBuilder
    public AllocaInst allocaInst(String name, Type type, BasicBlock parent) {
        return new AllocaInst(name, type, parent);
    }

    public StoreInst storeInst(BasicBlock parent, Value left, Value right) {
        return new StoreInst(parent, left, right);
    }

    public LoadInst loadInst(String name, BasicBlock parent, Value value) {
        return new LoadInst(name, parent, value);
    }

    public GepInst GepInst(String name, BasicBlock parent, ArrayList<Value> indexes) {
        Type type = ((PointerType) (indexes.get(0).type)).pointType;
        if (indexes.size() == 3) {
            type = new PointerType(((ArrayType) type).typeOfElement);
        } else {
            type = indexes.get(0).type;
        }
        return new GepInst(name, type, parent, indexes);
    }

    public BinaryInst binaryInst(String name, BasicBlock parent, Value left, Value right, TAG tag) {
        Type type;
        switch (tag) {
            case add:
            case mod:
            case mul:
            case sub:
            case sdiv:
                type = IntegerType.i32;
                break;
            case lt:
            case le:
            case ge:
            case gt:
            case eq:
            case ne:
                type = IntegerType.i1;
                break;
            default:
                type = IntegerType.i8;//不可能
                break;
        }
        return new BinaryInst(name, type, parent, left, right, tag);
    }

    public ZextInst zextInst(String name, BasicBlock parent, Value value) {
        return new ZextInst(name, IntegerType.i32, parent, 1, value);
    }

    public CallInst callInst(String name, BasicBlock parent, ArrayList<Value> params, Function function) {
        return new CallInst(name, parent, params, function);
    }

    public void retInst(Type type, BasicBlock parent, Value value) {
        if (!(parent.getLast() instanceof BrInst || parent.getLast() instanceof RetInst)) {
            new RetInst(type, parent, value);
        }
    }

    public BrInst brInst(Value cond, BasicBlock trueBlock, BasicBlock falseBlock, BasicBlock parent) {
        if (parent.instructions.size() == 0) {
            return new BrInst(cond, trueBlock, falseBlock, parent);
        } else if (parent.getLast() instanceof BrInst || parent.getLast() instanceof RetInst) {
            return null;
        } else {
            return new BrInst(cond, trueBlock, falseBlock, parent);
        }
    }

    public BrInst brInst(String judge, BasicBlock parent) {
        BrInst brInst = new BrInst(null, null, null, parent);
        brInst.judge = judge;
        return brInst;
    }


    public Putint putint(Value value, BasicBlock parent) {
        return new Putint(value, parent);
    }

    public Putstr putstr(String name, Str str, BasicBlock parent) {
        return new Putstr(name, str, parent);
    }

    public Getint getint(String name, BasicBlock parent) {
        return new Getint(name, parent);
    }

    public Str str(String name, int length, String content, Module module) {
        return new Str(name, length, content, module);
    }

    public BasicBlock basicBlock(String name, Function parent) {
        return new BasicBlock(name, parent);
    }

}

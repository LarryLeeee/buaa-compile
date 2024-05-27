package pass;

import frontend.MyPair;
import ir.Type.FunctionType;
import ir.Type.IntegerType;
import ir.Type.PointerType;
import ir.Type.Type;
import ir.Value.*;
import ir.Value.Constant.ConstantInt;
import ir.Value.Instruction.BinaryInst;
import ir.Value.Instruction.IO.Getint;
import ir.Value.Instruction.IO.Putint;
import ir.Value.Instruction.IO.Putstr;
import ir.Value.Instruction.Instruction;
import ir.Value.Instruction.Mem.*;
import ir.Value.Instruction.Terminate.BrInst;
import ir.Value.Instruction.Terminate.CallInst;
import ir.Value.Instruction.Terminate.RetInst;

import java.util.*;


public class Inline {

    public Module module = new Module();
    private Function curFunction;
    private BasicBlock curBlock;
    private HashMap<String, Value> values = new HashMap<>();
    private HashMap<String, GlobalVariable> globalVariables = new HashMap<>();
    private HashMap<String, Str> strs = new HashMap<>();
    private HashMap<String, Function> functions = new HashMap<>();//这里放的应该是新的内联过的函数
    private HashMap<String, BasicBlock> basicBlocks = new HashMap<>();
    //    private HashMap<String, Instruction> values = new HashMap<>();
    private HashMap<String, Value> params = new HashMap<>();//记录映射关系
    private ArrayList<MyPair<BasicBlock, Value>> phis = new ArrayList<>();
    private int regNum = 10;
    private int blockNum = 10;
    private boolean inlining = false;
    private boolean isInline = false;//记录是否是进入被内联函数的第一条指令
    private BasicBlock returnBlock = null;
    private ArrayList<String> tmpBKeys = new ArrayList<>();//记录暂时的块名字
    private ArrayList<String> tmpIKeys = new ArrayList<>();
    private HashMap<String, Instruction> tmpPhis = new HashMap<>();

    public Inline(Module module) {
        for (GlobalVariable g : module.globalVariables) {
            globalVariables.put(g.name, g);
            this.module.addGlobalVariable(g);
        }
        for (Str s : module.outputs) {
            strs.put(s.name, s);
            this.module.addOutput(s);
        }
        module(module);
    }

    public String getRegNum() {
        regNum++;
        return "%v" + regNum + "_inline";
    }

    public String getBlockNum() {
        blockNum++;
        return "%B" + blockNum + "_inline";
    }

    public void module(Module module) {
        for (Function function : module.functions) {
            if (function.time > 0 || function.name.equals("main")) {
                func(this.module, function);
                basicBlocks.clear();
                values.clear();
            }
        }
    }

    public void func(Module parent, Function function) {
        curFunction = new Function(function.name, function.type, parent);
        curFunction.time = function.time;
        curFunction.recursive = function.recursive;
        functions.put(function.name, curFunction);
        for (MyPair<String, Type> param : curFunction.params) {
            values.put(param.getKey(), new Value(param.getKey(), param.getValue()));
        }
        block(function.basicBlocks.get(0));
        for (BasicBlock block : function.basicBlocks) {
            block.isVisit = false;
        }
    }

    public void block(BasicBlock block_) {
        Queue<BasicBlock> queues = new LinkedList<>();
        queues.offer(block_);
        while (queues.size() > 0) {
            BasicBlock block = queues.poll();
            if (isInline) {
                isInline = false;
                basicBlocks.put(block.name, curBlock);
            } else if (basicBlocks.containsKey(block.name)) {
                BasicBlock block1 = basicBlocks.get(block.name);
                if (block1.instructions.size() == 0) {
                    curBlock = block1;
                } else {
                    curBlock = new BasicBlock(getBlockNum(), curFunction);
                    basicBlocks.put(block.name, curBlock);
                }
            } else {
                curBlock = new BasicBlock(getBlockNum(), curFunction);
                basicBlocks.put(block.name, curBlock);
            }
            if (inlining) {
                tmpBKeys.add(block.name);
            }
            for (Instruction i : block.instructions) {
                instr(i);
            }
            for (BasicBlock block1 : block.successors) {
                if (!block1.isVisit) {
                    block1.isVisit = true;
                    queues.offer(block1);
//                    block(block1);
                }
            }
        }
    }

    public void instr(Instruction instr) {
        if (instr instanceof BinaryInst || instr instanceof ZextInst || instr instanceof LoadInst) {
            if (instr.users.isEmpty()) return;
        }//消除没人使用的指令
        if (inlining) {
            tmpIKeys.add(instr.name);
        }
        Instruction instr1 = null;
        switch (instr.tag) {
            case add:
            case mod:
            case mul:
            case sub:
            case sdiv:
            case lt:
            case le:
            case ge:
            case gt:
            case eq:
            case ne:
            case and:
                instr1 = new BinaryInst(getRegNum(), instr.type, curBlock, get(instr, 0), get(instr, 1), instr.tag);
                values.put(instr.name, instr1);
                break;
            case alloca:
                instr1 = new AllocaInst(getRegNum(), ((PointerType) (instr.type)).pointType, curBlock);
                values.put(instr.name, instr1);
                break;
            case load:
                instr1 = new LoadInst(getRegNum(), curBlock, get(instr, 0));
                values.put(instr.name, instr1);
                break;
            case store:
                Value left;
                if (inlining) {
                    left = params.get(instr.operands.get(0).name);
                    if (left == null) {//虽然在内联，但不是传参的store
                        left = get(instr, 0);
                    }
                } else {//不在内联
                    left = get(instr, 0);
                }
                instr1 = new StoreInst(curBlock, left, get(instr, 1));
                values.put(instr.name, instr1);
                break;
            case gep:
                ArrayList<Value> indexes = new ArrayList<>();
                for (int i = 0; i < instr.operands.size(); i++) {
                    Value value = instr.operands.get(i);
                    if (value == null) {
                        indexes.add(null);
                    } else {
                        indexes.add(get(instr, i));
                    }
                }
                instr1 = new GepInst(getRegNum(), instr.type, curBlock, indexes);
                values.put(instr.name, instr1);
                break;
            case zext:
                instr1 = new ZextInst(getRegNum(), instr.type, curBlock, 1, get(instr, 0));
                values.put(instr.name, instr1);
                break;
            case getint:
                instr1 = new Getint(getRegNum(), curBlock);
                values.put(instr.name, instr1);
                break;
            case putint:
                instr1 = new Putint(get(instr, 0), curBlock);
//                values.put(instr.name, instr1);
                break;
            case ret:
                Value value = get(instr, 0);
                if (!inlining) {//没有被内联
                    instr1 = new RetInst(instr.type, curBlock, value);
                    values.put(instr.name, instr1);
                } else {//被内联
                    if (value != null) {
                        phis.add(new MyPair<>(curBlock, value));
                    }
                    instr1 = new BrInst(null, returnBlock, null, curBlock);
                    values.put(instr.name, instr1);
                }
                break;
            case call:
                Function f = functions.get(instr.operands.get(0).name);
                if (!f.recursive) {
                    if (!f.params.isEmpty()) {
                        for (int i = 0; i < instr.operands.size() - 1; i++) {
                            params.put("%v".concat(String.valueOf(i)), get(instr, i + 1));
                        }
                    }
                    isInline = true;
                    inlining = true;
                    returnBlock = new BasicBlock("%B" + (blockNum + f.basicBlocks.size()) + "_inline", curFunction);
                    //在内联的时候，前面的函数已经内联过了，所以块数是固定的
                    block(f.basicBlocks.get(0));
                    for (String name : tmpPhis.keySet()) {
                        Instruction instruction = tmpPhis.get(name);//原来的phi指令
                        Instruction instruction1 = (Instruction) (values.get(instruction.name));//新的phi指令
                        ArrayList<MyPair<BasicBlock, Value>> tempPhis = new ArrayList<>();
                        for (int i = 0; i < instruction.operands.size(); i += 2) {
                            tempPhis.add(new MyPair<>(basicBlocks.get(instruction.operands.get(i + 1).name),
                                    get(instruction, i)));
                        }
                        ((PhiInst) instruction1).addPhi(tempPhis);
                    }
                    tmpPhis.clear();

                    for (BasicBlock block : f.basicBlocks) {
                        block.isVisit = false;
                    }
                    blockNum++;
                    params = new HashMap<>();
                    curBlock = returnBlock;
                    inlining = false;
                    if (((FunctionType) (f.type)).retType == IntegerType.i32) {
                        instr1 = new PhiInst(getRegNum(), curBlock, phis);
                        phis = new ArrayList<>();
                        values.put(instr.name, instr1);
                    }
                    for (String key : tmpBKeys) {
                        basicBlocks.remove(key);
                    }
                    tmpBKeys.clear();
                    for (String key : tmpIKeys) {
                        values.remove(key);
                    }
                    tmpIKeys.clear();
                } else {
                    ArrayList<Value> rParams = new ArrayList<>();
                    for (int i = 1; i < instr.operands.size(); i++) {
                        rParams.add(get(instr, i));
                    }
                    instr1 = new CallInst(getRegNum(), curBlock, rParams, f);
                    values.put(instr.name, instr1);
                }
                break;
            case br:
                Value cond = get(instr, 0);
                BasicBlock falseBlock = null;
                if (instr.operands.get(2) != null) {
                    BasicBlock fB = ((BasicBlock) instr.operands.get(2));
                    if (basicBlocks.containsKey(fB.name)) {
                        falseBlock = basicBlocks.get(fB.name);
                    } else {
                        falseBlock = new BasicBlock(getBlockNum(), curFunction);
                        basicBlocks.put(fB.name, falseBlock);
                    }
                }
                BasicBlock trueBlock;
                BasicBlock fB = ((BasicBlock) instr.operands.get(1));
                if (basicBlocks.containsKey(fB.name)) {
                    trueBlock = basicBlocks.get(fB.name);
                } else {
                    trueBlock = new BasicBlock(getBlockNum(), curFunction);
                    basicBlocks.put(fB.name, trueBlock);
                }
                instr1 = new BrInst(cond, trueBlock, falseBlock, curBlock);
                values.put(instr.name, instr1);
                break;
            case putstr:
                instr1 = new Putstr(getRegNum(), ((Putstr) instr).str, curBlock);
                values.put(instr.name, instr1);
                break;
            case phi:
                instr1 = new PhiInst(getRegNum(), curBlock);
                values.put(instr.name, instr1);
                tmpPhis.put(instr1.name, instr);
                break;
            default:
                break;
        }
    }

    public Value get(Instruction instr, int idx) {
        if (instr.operands.get(idx) == null) {
            return null;
        } else if (instr.operands.get(idx) instanceof ConstantInt) {
            return instr.operands.get(idx);
        } else if (instr.operands.get(idx) instanceof GlobalVariable) {
            return instr.operands.get(idx);
        } else {
            return values.get(instr.operands.get(idx).name);
        }
    }

}

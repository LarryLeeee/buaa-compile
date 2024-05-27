package backend;

import backend.Instr.*;
import ir.Type.IntegerType;
import ir.Type.Type;
import ir.Value.*;
import ir.Value.Constant.ConstantInt;
import ir.Value.Instruction.Instruction;
import ir.Value.Instruction.Mem.AllocaInst;
import ir.Value.Instruction.Mem.GepInst;
import ir.Value.Instruction.Terminate.CallInst;
import ir.Value.Instruction.Terminate.RetInst;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsGen {
    //private int sp = 0x7fffeffc;
    private int data = 0x10010012;//前三个位置分别用来保存v0 a0 和 ra
    //private int off_part = 0;//局部的偏移量,向下生长
    private int off_global = 0;//mars默认地址起始是0x10010000，我们只需要记录偏移量即可，向上生长
    private HashMap<String, MipsFunc> funcs = new HashMap<>();
    private HashMap<String, MipsBlock> blocks = new HashMap<>();
    private HashMap<String, Integer> outputs = new HashMap<>();//存放输出字符串地址的
    private HashMap<String, String> regs = new HashMap<>();//虚拟寄存器对应的mips寄存器
    private HashMap<String, Integer> partMems = new HashMap<>();
    //记录所有局部的指针：名字、地址,实际上是记录不了值的，只能记录地址
    private HashMap<String, Integer> globalMems = new HashMap<>();//记录全局内存
    //只能记录全局！！
    private HashMap<String, Type> types = new HashMap<>();//为了记录指针的类型，以便后面取出来
    private Module module;
    public MipsModule mipsModule;
    private MipsBlock curBlock;
    private MipsFunc curFunc;
    private int[] weights = new int[32];
    private HashMap<String, String> huixie = new HashMap<>();//mips寄存器对应的虚拟寄存器
    //    private HashMap<String, Integer> funcParams = new HashMap<>();//函数用到的参数的栈上地址
    private ArrayList<RegImmInstr> spInstrs = new ArrayList<>();
    private int weight = 0;

    public MipsGen(Module module) {
        this.module = module;
        mipsModule = new MipsModule();
    }

    public String reg() {
        int min = Integer.MAX_VALUE, index = 0;
        for (int i = 0; i < 32; i++) {
            if (!in(i) && weights[i] < min) {
                min = weights[i];
                index = i;
            }
            if (weight < weights[i]) {
                weight = weights[i];
            }
        }
        weights[index] = weight + 1;
        if (huixie.containsKey("$" + index)) {//该寄存器被另外一个虚拟寄存器占用
            String reg = huixie.get("$" + index);//虚拟寄存器
            if (partMems.containsKey(reg) || globalMems.containsKey(reg)) {
                int offset = getMem(reg);
                new MemInstr(curBlock, "sw", "$" + index, "$sp", offset);
            }
        }
        return "$" + index;
    }

    public boolean in(int i) {
        return i == 1 || i == 0 || i == 2 || i == 3 || i == 4 || i == 28 || i == 29 || i == 30 || i == 31;
    }

    public void GenModule() {
        for (GlobalVariable g : module.globalVariables) {
            mipsModule.addGlobal(new Global(g.name, g.values));
            globalMems.put(g.name, data + off_global);
            off_global += g.values.size() * 4;
        }
        for (Str s : module.outputs) {
            STR str = new STR(s);
            mipsModule.addStr(str);
            outputs.put(str.name, data + off_global);
            off_global += str.content.length();
        }
        for (Function function : module.functions) {
            GenFunc(function);
        }
    }

    public void GenFunc(Function function) {
        //为了实现方便，我们将参数全部放在栈上
        MipsFunc mipsFunc = new MipsFunc(function.name, mipsModule);
        curFunc = mipsFunc;
        curFunc.addOff(4 * function.params.size());
        for (BasicBlock basicBlock : function.basicBlocks) {
            MipsBlock mipsBlock = new MipsBlock(basicBlock.name.substring(1), mipsFunc);
            blocks.put(mipsBlock.name, mipsBlock);
        }
        curBlock = blocks.get(function.basicBlocks.get(0).name);
        for (int i = 0; i < function.params.size(); i++) {
            String name = reg();
            new MemInstr(curBlock, "lw", name, "$sp", -4 * i);
            regs.put(function.params.get(i).getKey(), name);
            huixie.put(name, function.params.get(i).getKey());
        }
        curBlock = blocks.get(function.basicBlocks.get(0).name.substring(1));
        spInstrs.add(new RegImmInstr(curBlock, "subi", "$sp", "$sp", 0));
        for (BasicBlock basicBlock : function.basicBlocks) {
            GenBlock(basicBlock);
        }
        for (RegImmInstr instr : spInstrs) {
            instr.imm = curFunc.offset;
        }
    }

    public void GenBlock(BasicBlock basicBlock) {
        curBlock = blocks.get(basicBlock.name.substring(1));
        for (Instruction instruction : basicBlock.instructions) {
            switch (instruction.tag) {
                //binary
                case add:
                    add(instruction);
                    break;
                case sub:
                    sub(instruction);
                    break;
                case mul:
                    mul(instruction);
                    break;
                case sdiv:
                    div(instruction);
                    break;
                case mod:
                    mod(instruction);
                    break;
                case lt:
                    slt(instruction);
                    break;
                case le:
                    sle(instruction);
                    break;
                case ge:
                    sge(instruction);
                    break;
                case gt:
                    sgt(instruction);
                    break;
                case eq:
                    seq(instruction);
                    break;
                case ne:
                    sne(instruction);
                    break;
                //mem
                case alloca:
                    alloca(instruction);
                    break;
                case load:
                    load(instruction);
                    break;
                case store:
                    store(instruction);
                    break;
                case gep:
                    gep(instruction);
                    break;
                case zext:
                    zext(instruction);
                    break;
                //terminate
                case br:
                    br(instruction);
                    break;
                case ret:
                    ret(instruction);
                    break;
                case call:
                    call(instruction);
                    break;
                //IO
                case getint:
                    getint(instruction);
                    break;
                case putstr:
                    putstr(instruction);
                    break;
                case putint:
                    putint(instruction);
                    break;
                default:
                    break;
            }
            System.out.println(curBlock.mipsInstrs.get(curBlock.mipsInstrs.size() - 1).string());
        }
    }

    public void init(String reg, int value) {
        if (-32768 <= value && value <= 32767) {
            new RegImmInstr(curBlock, "addiu", reg, "$0", value);
        } else if (0 <= value && value <= 65535) {
            new RegImmInstr(curBlock, "ori", reg, "$0", value);
        } else if ((value & 0xffff) == 0) {
            new LuiInstr(curBlock, "lui", reg, (value >> 16) & 0xffff);
        } else {
            new LuiInstr(curBlock, "lui", reg, (value >> 16) & 0xffff);
            new RegImmInstr(curBlock, "ori", reg, reg, value & 0xffff);
        }
    }

    public void add(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            int value = ((ConstantInt) left).value + ((ConstantInt) right).value;
            init(name, value);
        } else if (left instanceof ConstantInt) {
            int value = ((ConstantInt) left).value;
            if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                new RegImmInstr(curBlock, "addiu", name, regs.get(right.name), value);
            } else {
                init("$1", value);
                new RegRegInstr(curBlock, "addu", name, regs.get(right.name), "$1");
            }
        } else if (right instanceof ConstantInt) {
            int value = ((ConstantInt) right).value;
            if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                new RegImmInstr(curBlock, "addiu", name, regs.get(left.name), value);
            } else {
                init("$1", value);
                new RegRegInstr(curBlock, "addu", name, regs.get(left.name), "$1");
            }
        } else {
            new RegRegInstr(curBlock, "add", name, regs.get(left.name), regs.get(right.name));
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void sub(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (right instanceof ConstantInt && left instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value - ((ConstantInt) right).value);
        } else if (left instanceof ConstantInt) {
            init("$1", ((ConstantInt) left).value);
            new RegRegInstr(curBlock, "subu", name, "$1", regs.get(right.name));
        } else if (right instanceof ConstantInt) {
            int value = ((ConstantInt) right).value;
            if (-Short.MAX_VALUE <= value && value <= -Short.MIN_VALUE) {
                new RegImmInstr(curBlock, "addiu", name, regs.get(left.name), -value);
            } else {
                init("$1", value);
                new RegRegInstr(curBlock, "subu", name, regs.get(left.name), "$1");
            }
        } else {
            new RegRegInstr(curBlock, "subu", name, regs.get(left.name), regs.get(right.name));
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void mul(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value * ((ConstantInt) right).value);
        } else if (left instanceof ConstantInt) {
            init("$1", ((ConstantInt) left).value);
            new RegRegInstr(curBlock, "mul", name, "$1", regs.get(right.name));
        } else if (right instanceof ConstantInt) {
            init("$1", ((ConstantInt) right).value);
            new RegRegInstr(curBlock, "mul", name, "$1", regs.get(left.name));
        } else {
            new RegRegInstr(curBlock, "mul", name, regs.get(right.name), regs.get(left.name));
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void div(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value / ((ConstantInt) right).value);
        } else if (left instanceof ConstantInt) {
            init("$1", ((ConstantInt) left).value);
            new MulDivInstr(curBlock, "div", "$1", regs.get(right.name));
            new MfInstr(curBlock, "mflo", name);
        } else if (right instanceof ConstantInt) {
            init("$1", ((ConstantInt) right).value);
            new MulDivInstr(curBlock, "div", regs.get(left.name), "$1");
            new MfInstr(curBlock, "mflo", name);
        } else {
            new MulDivInstr(curBlock, "div", regs.get(left.name), regs.get(right.name));
            new MfInstr(curBlock, "mflo", name);
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void mod(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value % ((ConstantInt) right).value);
        } else if (left instanceof ConstantInt) {
            init("$1", ((ConstantInt) left).value);
            new MulDivInstr(curBlock, "div", "$1", regs.get(right.name));
            new MfInstr(curBlock, "mfhi", name);
        } else if (right instanceof ConstantInt) {
            init("$1", ((ConstantInt) right).value);
            new MulDivInstr(curBlock, "div", regs.get(left.name), "$1");
            new MfInstr(curBlock, "mfhi", name);
        } else {
            new MulDivInstr(curBlock, "div", regs.get(left.name), regs.get(right.name));
            new MfInstr(curBlock, "mfhi", name);
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void slt(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value < ((ConstantInt) right).value ? 1 : 0);
        } else if (left instanceof ConstantInt) {
            init("$1", ((ConstantInt) left).value);
            new RegRegInstr(curBlock, "slt", name, "$1", regs.get(right.name));
        } else if (right instanceof ConstantInt) {
            int value = ((ConstantInt) right).value;
            if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                new RegImmInstr(curBlock, "slti", name, regs.get(left.name), ((ConstantInt) right).value);
            } else {
                init("$1", value);
                new RegRegInstr(curBlock, "slt", name, regs.get(left.name), "$1");
            }
        } else {
            new RegRegInstr(curBlock, "slt", name, regs.get(left.name), regs.get(right.name));
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void sle(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value <= ((ConstantInt) right).value ? 1 : 0);
        } else if (left instanceof ConstantInt) {
            int value = ((ConstantInt) left).value;
            if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                new RegImmInstr(curBlock, "slti", "$1", regs.get(right.name), value);
            } else {
                init("$1", value);
                new RegRegInstr(curBlock, "slt", "$1", regs.get(right.name), "$1");
            }
            new RegImmInstr(curBlock, "xori", name, "$1", 1);
        } else if (right instanceof ConstantInt) {
            init("$1", ((ConstantInt) right).value);
            new RegRegInstr(curBlock, "slt", "$1", "$1", regs.get(left.name));
            new RegImmInstr(curBlock, "xori", name, "$1", 1);
        } else {
            new RegRegInstr(curBlock, "slt", "$1", regs.get(right.name), regs.get(left.name));
            new RegImmInstr(curBlock, "xori", name, "$1", 1);
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void shorterSeq(String reg, int value) {
        if (-Short.MAX_VALUE <= value && value <= -Short.MIN_VALUE) {
            new RegImmInstr(curBlock, "addiu", "$1", reg, -value);
        } else if (0 <= value && value <= Short.MAX_VALUE - Short.MIN_VALUE) {
            new RegImmInstr(curBlock, "xori", "$1", reg, value);
        } else {
            init("$1", value);
            new RegRegInstr(curBlock, "xor", "$1", reg, "$1");
        }
    }

    public void sge(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value >= ((ConstantInt) right).value ? 1 : 0);
        } else if (left instanceof ConstantInt) {
            init("$1", ((ConstantInt) left).value);
            new RegRegInstr(curBlock, "slt", name, "$1", regs.get(right.name));
            new RegImmInstr(curBlock, "xori", name, name, 1);
        } else if (right instanceof ConstantInt) {
            int value = ((ConstantInt) right).value;
            if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                new RegImmInstr(curBlock, "slti", name, regs.get(left.name), ((ConstantInt) right).value);
            } else {
                init("$1", value);
                new RegRegInstr(curBlock, "slt", name, regs.get(left.name), "$1");
            }
            new RegImmInstr(curBlock, "xori", name, name, 1);
        } else {
            new RegRegInstr(curBlock, "slt", name, regs.get(left.name), regs.get(right.name));
            new RegImmInstr(curBlock, "xori", name, name, 1);
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void sgt(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value > ((ConstantInt) right).value ? 1 : 0);
        } else if (left instanceof ConstantInt) {
            int value = ((ConstantInt) left).value;
            if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                new RegImmInstr(curBlock, "slti", "$1", regs.get(right.name), value);
            } else {
                init("$1", value);
                new RegRegInstr(curBlock, "slt", "$1", regs.get(right.name), "$1");
            }
        } else if (right instanceof ConstantInt) {
            init("$1", ((ConstantInt) right).value);
            new RegRegInstr(curBlock, "slt", "$1", "$1", regs.get(left.name));
        } else {
            new RegRegInstr(curBlock, "slt", "$1", regs.get(right.name), regs.get(left.name));
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void sne(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value != ((ConstantInt) right).value ? 1 : 0);
        } else if (left instanceof ConstantInt) {
            shorterSeq(regs.get(right.name), ((ConstantInt) left).value);
            new RegRegInstr(curBlock, "sltu", name, "$0", "$1");
        } else if (right instanceof ConstantInt) {
            shorterSeq(regs.get(left.name), ((ConstantInt) right).value);
            new RegRegInstr(curBlock, "sltu", name, "$0", "$1");
        } else {
            new RegRegInstr(curBlock, "xor", "$1", regs.get(left.name), regs.get(right.name));
            new RegRegInstr(curBlock, "sltu", name, "$0", "$1");
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void seq(Instruction instr) {
        Value left = instr.operands.get(0);
        Value right = instr.operands.get(1);
        String name = reg();
        if (left instanceof ConstantInt && right instanceof ConstantInt) {
            init(name, ((ConstantInt) left).value == ((ConstantInt) right).value ? 1 : 0);
        } else if (left instanceof ConstantInt) {
            shorterSeq(regs.get(right.name), ((ConstantInt) left).value);
            new RegImmInstr(curBlock, "sltiu", name, "$1", 1);
        } else if (right instanceof ConstantInt) {
            shorterSeq(regs.get(left.name), ((ConstantInt) right).value);
            new RegImmInstr(curBlock, "sltiu", name, "$1", 1);
        } else {
            new RegRegInstr(curBlock, "xor", "$1", regs.get(left.name), regs.get(right.name));
            new RegImmInstr(curBlock, "sltiu", name, "$1", 1);
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void alloca(Instruction instr) {
        //是在内存上放一个int存数字（指针）
        //如果是一二维数组就再存一下值
        AllocaInst allocaInst = (AllocaInst) instr;
        curFunc.addOff(4);
//        int addr = sp - curFunc.offset;//存指针的地方
        if (allocaInst.isArray() || allocaInst.isInteger() || allocaInst.isTwoDim()) {
            int size = allocaInst.getSize();
            init("$1", curFunc.offset);//新数组的起始地址偏移
            curFunc.addOff(4 * size);
            partMems.put(instr.name, curFunc.offset - 4 * size - 4);
            new RegRegInstr(curBlock, "add", "$1", "$1", "$sp");
            new MemInstr(curBlock, "sw", "$1", "$sp", curFunc.offset - 4 * size - 4);
        } else {
            partMems.put(instr.name, curFunc.offset - 4);
            //函数中的指针，先存着，后面会sw
        }
    }

    public void gep(Instruction instr) {
        //数组来源为局部与全局均有可能，根据栈的规则需要先取局部的再取全局的
        //gep本质上是一个虚拟寄存器，我们要把以前的值取出来，然后存到这个虚拟寄存器对应的内存里面
        //取指令本身不会增加内存偏移量，只会多一个int大小来储存它
        String name = reg();//分配了一个寄存器来存
        GepInst gepInst = ((GepInst) instr);
        int off = 0;
        curFunc.addOff(4);
//        int addr = sp - curFunc.offset;//指针所在的地址，要存到这里面
        //有的时求不出来value值的，因此我们只能统一load
        partMems.put(gepInst.name, curFunc.offset - 4);//记录这个局部指针
        new MemInstr(curBlock, "lw", "$30", "$sp", partMems.get(gepInst.indexes.get(0).name));
        if (!(gepInst.is0_1() && gepInst.is0_2())) {
            //原指针里面存在的值
            if (gepInst.indexes.get(1) instanceof ConstantInt) {//const
                off += gepInst.getSize() * gepInst.getOff();
                if (gepInst.indexes.size() == 2) {
                    init("$1", off * 4);
                    new RegRegInstr(curBlock, "add", "$30", "$30", "$1");
                } else {
                    if (gepInst.indexes.get(2) instanceof ConstantInt) {
                        off += gepInst.getSize2() * gepInst.getOff2();
                        init("$1", off * 4);
                        new RegRegInstr(curBlock, "add", "$30", "$30", "$1");
                    } else {
                        init("$1", off * 4);
                        new RegRegInstr(curBlock, "add", "$30", "$30", "$1");
                        init("$1", gepInst.getSize2());
                        new RegRegInstr(curBlock, "mul", "$1", "$1", regs.get(gepInst.indexes.get(2).name));
                        new RegRegInstr(curBlock, "add", "$30", "$30", "$1");
                    }
                }
            } else {
                // value 或者 value 0,因此计算一次即可
                init("$1", gepInst.getSize());
                new RegRegInstr(curBlock, "mul", "$30", "$1", regs.get(gepInst.indexes.get(1).name));
            }
        }
        //最后之前把"$1"存入值
        new MemInstr(curBlock, "sw", "$30", "$sp", curFunc.offset - 4);
        new RegRegInstr(curBlock, "add", name, "$30", "$0");
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void load(Instruction instr) {
        int offset = getMem(instr.operands.get(0).name);
        String name = reg();
        if (partMems.containsKey(instr.operands.get(0).name)) {//局部
            new MemInstr(curBlock, "lw", name, "$sp", offset);
        } else {//全局
            new MemInstr(curBlock, "lw", name, "$0", offset);
        }
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void store(Instruction instr) {
        //store是往一个地址写东西
        int offset = getMem(instr.operands.get(1).name);
        String reg = "null";
        if (instr.operands.get(0) instanceof ConstantInt) {
            init("$1", ((ConstantInt) instr.operands.get(0)).value);
            reg = "$1";
        }
        if (regs.containsKey(instr.operands.get(0).name)) {
            reg = regs.get(instr.operands.get(0).name);
        }
        if (partMems.containsKey(instr.operands.get(1).name)) {//局部
            new MemInstr(curBlock, "sw", reg, "$sp", offset);
        } else {//全局
            new MemInstr(curBlock, "sw", reg, "$0", data);
        }
    }

    public void zext(Instruction instr) {
        String name = reg();
        new RegRegInstr(curBlock, "add", name, "$0", regs.get(instr.operands.get(0).name));
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void phi(Instruction instr) {

    }

    public int getMem(String name) {
        if (partMems.containsKey(name)) {
            return partMems.get(name);
        } else {
            return globalMems.get(name);
        }
    }

    public void getint(Instruction instr) {
        new SyscallInstr(curBlock, "", "getint");
        String name = reg();
        new RegRegInstr(curBlock, "add", name, "$v0", "$0");
        regs.put(instr.name, name);
        huixie.put(name, instr.name);
    }

    public void putstr(Instruction instr) {
        init("$a0", outputs.get(instr.name));
        new SyscallInstr(curBlock, "", "putstr");
    }

    public void putint(Instruction instr) {
        new RegRegInstr(curBlock, "add", "$a0", "$0", regs.get(instr.name));
        new SyscallInstr(curBlock, "", "putint");
    }

    public void br(Instruction instr) {
        if (instr.operands.get(0) != null) {
            new BranchInstr(curBlock, "bne", regs.get(instr.operands.get(0).name), "$0",
                    instr.operands.get(1).name);
            new JumpInstr(curBlock, "j", instr.operands.get(2).name);
        } else {
            new JumpInstr(curBlock, "j", instr.operands.get(1).name);
        }
    }

    public void ret(Instruction instr) {
        RetInst retInst = ((RetInst) instr);
        if (retInst.type instanceof IntegerType) {
            if (retInst.operands.get(0) instanceof ConstantInt) {
                init("$a0", ((ConstantInt) retInst.operands.get(0)).value);
            } else {
                new RegRegInstr(curBlock, "add", "$v0", "$0",
                        regs.get(retInst.operands.get(0).name));
            }
        }
        spInstrs.add(new RegImmInstr(curBlock, "add", "$sp", "$sp", 0));
        new JumpInstr(curBlock, "jr", "");
    }

    public void call(Instruction instr) {
        CallInst callInst = ((CallInst) instr);
        Function function = callInst.function;
        for (int i = 0; i < callInst.params.size(); i++) {
            new MemInstr(curBlock, "sw", regs.get((callInst.params.get(i)).name), "$sp", -4 * i);
        }
        new JumpInstr(curBlock, "jal", function.name);
        if (callInst.type instanceof IntegerType) {
            String name = reg();
            new RegRegInstr(curBlock, "add", name, "$v0", "$0");
            regs.put(instr.name, name);
            huixie.put(name, instr.name);
        }
    }

}

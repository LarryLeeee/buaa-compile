package frontend;

import frontend.Parser.Block.BLockItem;
import frontend.Parser.Block.Block;
import frontend.Parser.Block.Stmt;
import frontend.Parser.CompUnit;
import frontend.Parser.Decl.*;
import frontend.Parser.Exp.*;
import frontend.Parser.Exp.Number;
import frontend.Parser.Func.*;
import ir.Type.*;
import ir.Value.*;
import ir.FactoryBuilder;
import ir.Value.Constant.Constant;
import ir.Value.Constant.ConstantArray;
import ir.Value.Constant.ConstantInt;
import ir.Value.Constant.ConstantVar;
import ir.Value.Instruction.Instruction;
import ir.Value.Instruction.Mem.AllocaInst;
import ir.Value.Instruction.Mem.GepInst;
import ir.Value.Instruction.TAG;
import ir.Value.Instruction.Terminate.BrInst;
import ir.Value.Instruction.Terminate.RetInst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Visitor {
    private CompUnit compUnit;
    private SymbolTable symbolTable;
    private Module module = new Module();
    private BasicBlock curBlock;
    private Function curFunction;
    private Value tmpValue;
    private int tmpInt;//所有对tmpInt的修改均在子级visit方法中进行
    private ArrayList<Type> tmpFuncParams = new ArrayList<>();
    private ArrayList<Value> tmpArray = new ArrayList<>();//当前的数组，元素可以是一维数组、常数、变量等等
    private MyPair<Integer, Integer> dims = new MyPair<>(0, 0);
    private ConstantInt c = new ConstantInt(IntegerType.i32, 0, 0);
    private FactoryBuilder f;
    Stack<ArrayList<BrInst>> backPatch = new Stack<>();
    //这个用来实现指令回填，关键问题在于continue和break不知道在哪个循环里面，因此要使用一个栈式结构
    private boolean only = false;
    private int regNum = 10;
    private int blockNum = 10;
    private int strNum = 1;
    private boolean isConst = false;//用来标价要计算的表达式是否为常值
    private boolean isGlobal = false;//如果初始化在全局， 那么所有值均为常数，可以直接赋值
    private boolean isCall = false;//因为在C语言和llvm当中，指针类型是不一样的，故在函数调用时需要额外记录
    private boolean isTwo = false;//特判二维数组
    private boolean initConArr = false;//特判初始化局部const数组
    //再次赋值只有在语句Stmt里才有
    private boolean preEnter = false;

    public Visitor(CompUnit compUnit) {
        this.compUnit = compUnit;
        symbolTable = new SymbolTable();
        f = new FactoryBuilder();
    }

    public Module getModule() {
        return module;
    }

    public String getRegNum() {
        regNum++;
        return "%v" + regNum;
    }

    public String getStrNum() {
        strNum++;
        return "@_str_" + strNum;
    }

    public String getBlockNum() {
        blockNum++;
        return "%B" + blockNum;
    }

    public void visitCompUnit() {
        symbolTable.addLayer();
        for (ConstDecl constDecl : compUnit.getConstDecls()) {
            visitConstDecl(constDecl);
        }
        for (VarDecl varDecl : compUnit.getVarDecls()) {
            visitVarDecl(varDecl);
        }
        for (FuncDef funcDef : compUnit.getFuncDefs()) {
            visitFuncDef(funcDef);
        }
        visitFuncDef(compUnit.getMainFuncDef().trans());
    }

    public void visitConstDecl(ConstDecl constDecl) {
        for (ConstDef constDef : constDecl.getConstDefs()) {
            visitConstDef(constDef);
        }
    }

    public void visitVarDecl(VarDecl varDecl) {
        for (VarDef vardef : varDecl.getVarDefs()) {
            visitVarDef(vardef);
        }
    }

    // 对参数插入alloca和store
    public void visitFuncDef(FuncDef funcDef) {
        String name = funcDef.id.getName();
        Type retType = (funcDef.funcType.type.equals("void")) ? VoidType.voidType : IntegerType.i32;
        if (funcDef.funcFParams != null) {//第一遍为了构建函数类型
            preEnter = true;
            visitFuncFParams(funcDef.funcFParams);
        }
        curFunction = f.function(name, retType, tmpFuncParams, module);
        tmpFuncParams = new ArrayList<>();
        symbolTable.put(name, curFunction);
        symbolTable.addLayer();
        curBlock = f.basicBlock(name + "_entry", curFunction);
        if (funcDef.funcFParams != null) {
            visitFuncFParams(funcDef.funcFParams);
        }
        visitBlock(funcDef.block);
        Instruction instr = curBlock.getLast();
        if (instr == null || instr.tag != TAG.ret && instr.tag != TAG.br) {
            if (curFunction.getRetType() instanceof VoidType) {
                f.retInst(VoidType.voidType, curBlock, null);
            } else {
                f.retInst(IntegerType.i32, curBlock, tmpValue);
            }
        }
        symbolTable.popLayer();
    }

    // FuncFParams → FuncFParam { ',' FuncFParam }
    public void visitFuncFParams(FuncFParams funcFParams) {
        if (funcFParams.funcFParams.size() != 0) {
            if (preEnter) {
                preEnter = false;
                for (FuncFParam funcFParam : funcFParams.funcFParams) {
                    visitFuncFParam(funcFParam);
                }
            } else {
                //建立对参数的alloc与store，这里有着统一的形式，
                // 注意，我们第一遍就已经建立好了指向数组的指针当中元素个数，因此不需要再额外建立
                // 直接正常存即可，都没毛病
                for (int i = 0; i < curFunction.params.size(); i++) {
                    MyPair<String, Type> param = curFunction.params.get(i);
                    String name = funcFParams.funcFParams.get(i).id.getName();
                    Value alloc = f.allocaInst(getRegNum(), param.getValue(), curBlock);
                    f.storeInst(curBlock, new Value(param.getKey(), param.getValue()), alloc);
                    symbolTable.put(name, alloc);
                }
            }
        }
    }

    //    FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public void visitFuncFParam(FuncFParam funcFParam) {
        if (funcFParam.type.equals("int")) {
            tmpFuncParams.add(IntegerType.i32);
        } else if (funcFParam.type.equals("Arr")) {
            tmpFuncParams.add(new PointerType(IntegerType.i32));
        } else {
            isConst = true;
            visitConstExp(funcFParam.constExp);
            isConst = false;
            tmpFuncParams.add(new PointerType(new ArrayType(tmpInt, IntegerType.i32)));
        }
    }

    public void visitBlock(Block block) {
        symbolTable.addLayer();
        for (BLockItem bLockItem : block.bLockItems) {
            visitBlockItem(bLockItem);
        }
        symbolTable.popLayer();
    }

    public void visitBlockItem(BLockItem bLockItem) {
        if (bLockItem.type.equals("const")) {
            visitConstDecl(bLockItem.constDecl);
        } else if (bLockItem.type.equals("int")) {
            visitVarDecl(bLockItem.varDecl);
        } else {
            visitStmt(bLockItem.stmt);
        }
    }

    //均为常数，应该直接放进去当作常数传播使用
    //无论常量定义在哪里出现，都直接加入即可
    // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    // constExp的个数代表是0还是12维数组
    public void visitConstDef(ConstDef constDef) {
        String name = constDef.getId().getName();
        ArrayList<ConstExp> initDims = constDef.constExps;
        if (initDims.isEmpty()) {//普通变量
            visitConstInitval(constDef.constInitval);//必然不为null
            if (symbolTable.isGlobal()) {
                symbolTable.put(name, f.globalVariable("@" + name, IntegerType.i32, true, c.gen(tmpInt), module));
                isGlobal = false;
            } else {
                symbolTable.put(name, f.constantVar(name, tmpInt));
            }
        } else {
            visitConstExp(initDims.get(0));
            int dim1 = tmpInt, dim2 = 1;
            if (initDims.size() == 2) {//二维数组
                visitConstExp(initDims.get(1));
                dim2 = tmpInt;
                isTwo = true;
            }
            ArrayType arrTy;
            if (isTwo) {
                arrTy = new ArrayType(dim2, IntegerType.i32);
                arrTy = new ArrayType(dim1, arrTy);
            } else {
                arrTy = new ArrayType(dim1, IntegerType.i32);
            }
            dims = new MyPair<>(dim1, dim2);
            if (symbolTable.isGlobal()) {
                isGlobal = true;
                visitConstInitval(constDef.constInitval);
                isGlobal = false;
                ArrayList<Constant> constants = new ArrayList<>();
                if (isTwo) {
                    for (int i = 0; i < dim1; i++) {
                        ArrayList<Constant> temp = new ArrayList<>();
                        for (int j = 0; j < dim2; j++) {
                            temp.add((Constant) (tmpArray.get(i * dim2 + j)));
                        }
                        constants.add(new ConstantArray(new ArrayType(dim2, IntegerType.i32), temp));
                    }
                } else {
                    for (Value value : tmpArray) {
                        constants.add((Constant) value);
                    }
                }
                tmpArray = new ArrayList<>();
                symbolTable.put(name, f.globalVariable("@" + name, arrTy, true, new ConstantArray(arrTy, constants), module));
            } else {
                AllocaInst allocaInst = f.allocaInst(getRegNum(), arrTy, curBlock);
                allocaInst.isConst = true;
                symbolTable.put(name, allocaInst);

                int t1 = dim1, t2 = dim2;
                visitConstInitval(constDef.constInitval);
                ArrayList<Constant> constants = new ArrayList<>();
                if (isTwo) {
                    for (int i = 0; i < dim1; i++) {
                        ArrayList<Constant> temp = new ArrayList<>();
                        for (int j = 0; j < dim2; j++) {
                            temp.add((Constant) (tmpArray.get(i * dim2 + j)));
                        }
                        constants.add(new ConstantArray(new ArrayType(dim2, IntegerType.i32), temp));
                    }
                } else {
                    for (Value value : tmpArray) {
                        constants.add((Constant) value);
                    }
                }
                allocaInst.constantArray = new ConstantArray(arrTy, constants);
                tmpArray.clear();
                dims = new MyPair<>(t1, t2);

                initConArr = true;
                visitConstInitval(constDef.constInitval);
                initConArr = false;
                ArrayList<Value> indexes = new ArrayList<Value>() {{
                    add(allocaInst);
                    add(c.gen(0));
                    add(c.gen(0));
                }};
                GepInst gepInst = f.GepInst(getRegNum(), curBlock, indexes);
                if (initDims.size() == 2) {
                    GepInst finalGepInst = gepInst;
                    ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                        add(finalGepInst);
                        add(c.gen(0));
                        add(c.gen(0));
                    }};
                    gepInst = f.GepInst(getRegNum(), curBlock, newIndexes);
                }
                //这里处理后tmpArr已经变成一维数组了
                for (int i = 0; i < tmpArray.size(); i++) {
                    GepInst g;
                    if (i == 0) {
                        g = gepInst;
                    } else {
                        GepInst finalGepInst1 = gepInst;
                        int finalI = i;
                        ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                            add(finalGepInst1);
                            add(c.gen(finalI));
                        }};
                        g = f.GepInst(getRegNum(), curBlock, newIndexes);
                    }
                    f.storeInst(curBlock, tmpArray.get(i), g);
                }
                tmpArray = new ArrayList<>();
            }
            isTwo = false;
        }
    }

    //    VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    // Ident '=' 'getint' '(' ')'
    public void visitVarDef(VarDef varDef) {
        String name = varDef.id.getName();
        ArrayList<ConstExp> initDims = varDef.constExps;
        if (varDef.isGetint) {
            AllocaInst allocaInst = f.allocaInst(getRegNum(), IntegerType.i32, curBlock);
            symbolTable.put(name, allocaInst);
            tmpValue = f.getint(getRegNum(), curBlock);
            f.storeInst(curBlock, tmpValue, allocaInst);
        } else {
            if (initDims.isEmpty()) {//普通变量
                if (!symbolTable.isGlobal()) {
                    AllocaInst allocaInst = f.allocaInst(getRegNum(), IntegerType.i32, curBlock);
                    symbolTable.put(name, allocaInst);
                    if (varDef.initVal != null) {
                        visitInitval(varDef.initVal);
                        f.storeInst(curBlock, tmpValue, allocaInst);
                    }
                } else {
                    if (varDef.initVal == null) {
                        symbolTable.put(name, f.globalVariable("@" + name, IntegerType.i32, false, c.gen(0), module));
                    } else {
                        isGlobal = true;
                        visitInitval(varDef.initVal);
                        isGlobal = false;
                        symbolTable.put(name, f.globalVariable("@" + name, IntegerType.i32, false, c.gen(tmpInt), module));
                    }
                }
            } else {
                visitConstExp(initDims.get(0));
                int dim1 = tmpInt, dim2 = 1;
                if (initDims.size() == 2) {//二维数组
                    visitConstExp(initDims.get(1));
                    dim2 = tmpInt;
                    isTwo = true;
                }
                ArrayType arrTy;
                if (isTwo) {
                    arrTy = new ArrayType(dim2, IntegerType.i32);
                    arrTy = new ArrayType(dim1, arrTy);
                } else {
                    arrTy = new ArrayType(dim1, IntegerType.i32);
                }
                dims = new MyPair<>(dim1, dim2);
                if (!symbolTable.isGlobal()) {
                    AllocaInst allocaInst = f.allocaInst(getRegNum(), arrTy, curBlock);
                    symbolTable.put(name, allocaInst);
                    if (varDef.initVal != null) {
                        visitInitval(varDef.initVal);
                        ArrayList<Value> indexes = new ArrayList<Value>() {{
                            add(allocaInst);
                            add(c.gen(0));
                            add(c.gen(0));
                        }};
                        GepInst gepInst = f.GepInst(getRegNum(), curBlock, indexes);
                        if (initDims.size() == 2) {
                            GepInst finalGepInst = gepInst;
                            ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                                add(finalGepInst);
                                add(c.gen(0));
                                add(c.gen(0));
                            }};
                            gepInst = f.GepInst(getRegNum(), curBlock, newIndexes);
                        }
                        for (int i = 0; i < tmpArray.size(); i++) {
                            //没定义的变量初始值是任何后续都会被改变，所以无所谓；
                            //另外一方面，我们可以配套让value初始值为0来解决该问题
                            GepInst g;
                            if (i == 0) {
                                g = gepInst;
                            } else {
                                GepInst finalGepInst1 = gepInst;
                                int finalI = i;
                                ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                                    add(finalGepInst1);
                                    add(c.gen(finalI));
                                }};
                                g = f.GepInst(getRegNum(), curBlock, newIndexes);
                            }
                            f.storeInst(curBlock, tmpArray.get(i), g);
                        }
                        tmpArray = new ArrayList<>();
                    }
                } else {
                    isGlobal = true;
                    visitInitval(varDef.initVal);
                    isGlobal = false;
                    ArrayList<Constant> constants = new ArrayList<>();
                    if (isTwo) {
                        for (int i = 0; i < dim1; i++) {
                            ArrayList<Constant> temp = new ArrayList<>();
                            for (int j = 0; j < dim2; j++) {
                                temp.add((Constant) (tmpArray.get(i * dim2 + j)));
                            }
                            constants.add(new ConstantArray(new ArrayType(dim2, IntegerType.i32), temp));
                        }
                    } else {
                        for (Value value : tmpArray) {
                            constants.add((Constant) value);
                        }
                    }
                    tmpArray = new ArrayList<>();
                    symbolTable.put(name, f.globalVariable("@" + name, arrTy, false, new ConstantArray(arrTy, constants), module));
                }
                isTwo = false;
            }
        }
    }

    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    // 注意，此处处理过后，原来的二维数组将被完全展开为一维数组{}
    //const所有的都有值，而initval需要标记哪些没有值
    public void visitConstInitval(ConstInitval constInitval) {
        if (constInitval.type.equals("Ident")) {
            visitConstExp(constInitval.constExp);
        } else {//llvm当中是数组嵌套数组，因此只有两种形式，此外其是展开为一维，故直接叠加即可
            //此外，这里需要记录每一维的维度信息，用来把没有初始值的地方赋值0
            int dim1 = dims.getKey(), dim2 = dims.getValue();
            int i = 0, size = constInitval.constInitvals.size();
            if (!isTwo) {
                while (i < size) {
                    visitConstInitval(constInitval.constInitvals.get(i));
                    if (!initConArr) {
                        tmpArray.add(c.gen(tmpInt));
                    } else {
                        tmpArray.add(tmpValue);
                    }
                    i++;
                }
                while (i < dim1) {
                    tmpArray.add(c.gen(0));
                    i++;
                }
            } else {
                dims = new MyPair<>(dim2, 1);
                while (i < size) {
                    isTwo = false;
                    visitConstInitval(constInitval.constInitvals.get(i));
                    isTwo = true;
                    i++;
                }
                while (i < dim1) {
                    for (int j = 0; j < dims.getKey(); j++) {
                        tmpArray.add(c.gen(0));
                    }
                    i++;
                }
            }
        }
    }

    //    InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public void visitInitval(InitVal initval) {
        if (initval == null) {//未初始化的全局变量数组
            int dim1 = dims.getKey(), dim2 = dims.getValue();
            if (!isTwo) {
                for (int i = 0; i < dim1; i++) {
                    tmpArray.add(c.gen(0));
                }
            } else {
                dims = new MyPair<>(dim2, 1);
                for (int i = 0; i < dim1; i++) {
                    isTwo = false;
                    visitInitval(initval);
                    isTwo = true;
                }
            }
        } else {
            if (initval.type.equals("Ident")) {
                if (isGlobal) {
                    isConst = true;
                    visitExp(initval.exp);
                    isConst = false;
                } else {
                    visitExp(initval.exp);
                }
            } else {
                int dim1 = dims.getKey(), dim2 = dims.getValue();
                int i = 0, size = initval.initVals.size();
                if (!isTwo) {
                    while (i < size) {
                        visitInitval(initval.initVals.get(i));
                        if (isGlobal) {
                            tmpArray.add(c.gen(tmpInt));
                        } else {
                            tmpArray.add(tmpValue);
                        }
                        i++;
                    }
                    while (i < dim1) {
                        tmpArray.add(c.gen(0));
                        i++;
                    }
                } else {
                    dims = new MyPair<>(dim2, 1);
                    while (i < size) {
                        isTwo = false;
                        visitInitval(initval.initVals.get(i));
                        isTwo = true;
                        i++;
                    }
                    while (i < dim1) {
                        for (int j = 0; j < dims.getKey(); j++) {
                            tmpArray.add(c.gen(0));
                        }
                        i++;
                    }
                }
            }
        }
    }


    public void visitConstExp(ConstExp constExp) {
        if (!initConArr) {
            isConst = true;
        }
        visitAddExp(constExp.addExp);
        isConst = false;
    }

    public void visitAddExp(AddExp addExp) {
        if (!isConst) {
            visitMulExp(addExp.mulExps.get(0));
            Value left = tmpValue;
            for (int i = 0; i < addExp.ops.size(); i++) {
                visitMulExp(addExp.mulExps.get(i + 1));
                Value right = tmpValue;
                if (left.type.isI1()) {
                    left = f.zextInst(getRegNum(), curBlock, left);
                }
                if (right.type.isI1()) {
                    right = f.zextInst(getRegNum(), curBlock, right);
                }
                if (addExp.ops.get(i).getName().equals("+")) {
                    left = f.binaryInst(getRegNum(), curBlock, left, right, TAG.add);
                } else {
                    left = f.binaryInst(getRegNum(), curBlock, left, right, TAG.sub);
                }
            }
            tmpValue = left;
        } else {
            visitMulExp(addExp.mulExps.get(0));
            if (!isConst) {
                visitAddExp(addExp);
            }
            int sum = tmpInt;
            for (int i = 0; i < addExp.ops.size(); i++) {
                visitMulExp(addExp.mulExps.get(i + 1));
                if (!isConst) {
                    visitAddExp(addExp);
                }
                if (addExp.ops.get(i).getName().equals("+")) {
                    sum += tmpInt;
                } else {
                    sum -= tmpInt;
                }
            }
            tmpInt = sum;
        }
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%' | 'bitand' ) UnaryExp
    public void visitMulExp(MulExp mulExp) {
        if (!isConst) {
            visitUnaryExp(mulExp.unaryExps.get(0));
            Value left = tmpValue;
            for (int i = 0; i < mulExp.ops.size(); i++) {
                visitUnaryExp(mulExp.unaryExps.get(i + 1));
                Value right = tmpValue;
                if (left.type.isI1()) {
                    left = f.zextInst(getRegNum(), curBlock, left);
                }
                if (right.type.isI1()) {
                    right = f.zextInst(getRegNum(), curBlock, right);
                }
                if (mulExp.ops.get(i).getName().equals("*")) {
                    left = f.binaryInst(getRegNum(), curBlock, left, right, TAG.mul);
                } else if (mulExp.ops.get(i).getName().equals("/")) {
                    left = f.binaryInst(getRegNum(), curBlock, left, right, TAG.sdiv);
                } else if (mulExp.ops.get(i).getName().equals("%")) {
                    left = f.binaryInst(getRegNum(), curBlock, left, right, TAG.mod);
                } else {
                    left = f.binaryInst(getRegNum(), curBlock, left, right, TAG.and);
                }
            }
            if (left.type.isI1()) {
                left = f.zextInst(getRegNum(), curBlock, left);
            }
            tmpValue = left;
        } else {
            visitUnaryExp(mulExp.unaryExps.get(0));
            int sum = tmpInt;
            for (int i = 0; i < mulExp.ops.size(); i++) {
                visitUnaryExp(mulExp.unaryExps.get(i + 1));
                if (mulExp.ops.get(i).getName().equals("*")) {
                    sum *= tmpInt;
                } else if (mulExp.ops.get(i).getName().equals("/")) {
                    sum /= tmpInt;
                } else if (mulExp.ops.get(i).getName().equals("%")) {
                    sum %= tmpInt;
                } else {
                    sum &= tmpInt;
                }
            }
            tmpInt = sum;
        }
    }

    public void visitUnaryExp(UnaryExp unaryExp) {
        if (!isConst) {
            if (unaryExp.type.equals("FuncR")) {
                visitFunCall(unaryExp);
            } else if (unaryExp.type.equals("PrimaryExp")) {
                visitPrimaryExp(unaryExp.primaryExp);
            } else {
                visitUnaryExp(unaryExp.unaryExp);
                Value value = tmpValue;
                if (value.type.isI1()) {
                    f.zextInst(getRegNum(), curBlock, value);
                }
                if (unaryExp.unaryOp.type.equals("!")) {
                    tmpValue = f.binaryInst(getRegNum(), curBlock, value, c.gen(0), TAG.eq);
                } else if (unaryExp.unaryOp.type.equals("-")) {
                    //里面是否需要再判断一次i1，目前看来不需要
                    tmpValue = f.binaryInst(getRegNum(), curBlock, c.gen(0), value, TAG.sub);
                }
            }
        } else {
            if (unaryExp.type.equals("PrimaryExp")) {
                visitPrimaryExp(unaryExp.primaryExp);
            } else {
                visitUnaryExp(unaryExp.unaryExp);
                if (unaryExp.unaryOp.type.equals("!")) {
                    tmpInt = (tmpInt == 0) ? 1 : 0;
                } else if (unaryExp.unaryOp.type.equals("-")) {
                    tmpInt = -tmpInt;
                }
            }
        }
    }

    //无论是否为常值表达式，最终都可以计算出值来
    public void visitFunCall(UnaryExp unaryExp) {
        Value func = symbolTable.find(unaryExp.id.getName());
        ((Function) func).time = ((Function) func).time + 1;
        ArrayList<MyPair<String, Type>> fParams = ((Function) func).params;
        ArrayList<Value> rParams = new ArrayList<>();
        if (!fParams.isEmpty()) {
            ArrayList<Exp> exps = unaryExp.funcRParams.exps;
            for (int i = 0; i < fParams.size(); i++) {
                Type type = fParams.get(i).getValue();
                isCall = !type.isIntegerTy();
                visitExp(exps.get(i));
                if (!(type instanceof IntegerType)) {//这里的基础是此处tmpValue只会用一次
                    tmpValue.type = type;
                }
                rParams.add(tmpValue);
                isCall = false;
            }
        }
        tmpFuncParams = new ArrayList<>();
        tmpValue = f.callInst(getRegNum(), curBlock, rParams, (Function) func);
        if (func.name.equals(curFunction.name)) {
            curFunction.recursive = true;
        }
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number
    public void visitPrimaryExp(PrimaryExp primaryExp) {
        if (!isConst) {
            if (primaryExp.type.equals("Exp")) {
                visitExp(primaryExp.exp);
            } else if (primaryExp.type.equals("LVal")) {
                if (isCall) {//正在生成 call 指令
                    isCall = false;
                    visitLVal(primaryExp.lVal);
                } else {
                    visitLVal(primaryExp.lVal);
                    if (tmpValue.type instanceof PointerType) {
                        if (!isCall) {
                            tmpValue = f.loadInst(getRegNum(), curBlock, tmpValue);
                        }
                    }
                }
            } else {
                visitNumber(primaryExp.number);
                if (!isConst) {
                    tmpValue = c.gen(tmpInt);
                }
            }
        } else {
            if (primaryExp.type.equals("Exp")) {
                visitExp(primaryExp.exp);
            } else if (primaryExp.type.equals("LVal")) {
                visitLVal(primaryExp.lVal);
            } else {
                visitNumber(primaryExp.number);
                if (!isConst) {
                    tmpValue = c.gen(tmpInt);
                }
            }
        }
    }

    public void visitExp(Exp exp) {
        if (exp != null) {
            visitAddExp(exp.addExp);
        }
    }

    //LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    //在构造函数实参时，需要把tmpValue变成int、int*和数组指针
    //而在函数里面，对于int使用
    public void visitLVal(LVal lVal) {
        Value value = symbolTable.find(lVal.id.getName());
        if (value instanceof ConstantVar) {
            //只有局部const量才会这样
            tmpInt = ((ConstantVar) value).value;
            tmpValue = c.gen(tmpInt);
        } else {//剩下的全是指针类型
            Type pointType = ((PointerType) (value.type)).pointType;
            if (pointType instanceof IntegerType) {//普通变量，函数的int参数
                tmpValue = value;
                if (value.isConstInt() || isGlobal) {//全局const
                    tmpInt = ((GlobalVariable) value).getValue();
                }
            } else if (pointType instanceof ArrayType) {//指向一维数组和二维数组
                if (isGlobal || (value instanceof GlobalVariable && isConst)) {
                    Constant constant = ((GlobalVariable) value).value;
                    boolean tmp = isConst;
                    isConst = true;
                    for (Exp exp : lVal.exps) {
                        visitExp(exp);
                        constant = ((ConstantArray) constant).constantArrays.get(tmpInt);
                    }
                    isConst = tmp;
                    tmpInt = ((ConstantInt) constant).value;
                    tmpValue = c.gen(tmpInt);
                } else if (value instanceof AllocaInst && ((AllocaInst) value).isConst && isConst) {
                    Constant constant = ((AllocaInst) value).constantArray;
                    boolean tmp = isConst;
                    isConst = true;
                    for (Exp exp : lVal.exps) {
                        visitExp(exp);
                        constant = ((ConstantArray) constant).constantArrays.get(tmpInt);
                    }
                    isConst = tmp;
                    tmpInt = ((ConstantInt) constant).value;
                    tmpValue = c.gen(tmpInt);
                } else {
                    if (!lVal.exps.isEmpty()) {
                        isConst = false;
                        for (Exp exp : lVal.exps) {
                            visitExp(exp);
                            Value finalValue = value;
                            ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                                add(finalValue);//基地址
                                add(c.gen(0));//降维处理
                                add(tmpValue);//偏移量
                            }};
                            value = f.GepInst(getRegNum(), curBlock, newIndexes);
                        }
                        if (((ArrayType) (pointType)).typeOfElement instanceof ArrayType && lVal.exps.size() == 1) {
                            Value finalValue4 = value;
                            ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                                add(finalValue4);//基地址
                                add(c.gen(0));//降维处理
                                add(c.gen(0));//偏移量
                            }};
                            value = f.GepInst(getRegNum(), curBlock, newIndexes);
                        }
                        isConst = false;
                        tmpValue = value;
                    } else {//只可能是参数
                        Value finalValue2 = value;
                        ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                            add(finalValue2);//基地址
                            add(c.gen(0));
                            add(c.gen(0));
                        }};
                        tmpValue = f.GepInst(getRegNum(), curBlock, newIndexes);
                    }
                }
            } else {
                pointType = ((PointerType) (pointType)).pointType;
                value = f.loadInst(getRegNum(), curBlock, value);
                if (pointType instanceof IntegerType) {//函数参数中int指针
                    if (!lVal.exps.isEmpty()) {
                        visitExp(lVal.exps.get(0));
                        Value finalValue1 = value;
                        ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                            add(finalValue1);//基地址
                            add(tmpValue);//偏移量
                        }};
                        tmpValue = f.GepInst(getRegNum(), curBlock, newIndexes);
                    } else {
                        tmpValue = value;
                    }
                } else {//函数参数中数组指针
                    if (lVal.exps.isEmpty()) {
                        tmpValue = value;
                    } else {
                        visitExp(lVal.exps.get(0));
                        Value finalValue3 = value;
                        ArrayList<Value> newIndexes = new ArrayList<Value>() {{
                            add(finalValue3);//基地址
                            add(tmpValue);//降维
                            add(c.gen(0));//偏移量
                        }};
                        GepInst gepInst = f.GepInst(getRegNum(), curBlock, newIndexes);
                        for (int i = 1; i < lVal.exps.size(); i++) {
                            visitExp(lVal.exps.get(i));
                            GepInst finalGepInst = gepInst;
                            gepInst = f.GepInst(getRegNum(), curBlock, new ArrayList<Value>() {{
                                add(finalGepInst);//基地址,不能再降维了，因为已经变成int*了
                                add(tmpValue);//偏移量
                            }});
                        }
                        tmpValue = gepInst;
                    }
                }
            }
        }
    }

    public void visitNumber(Number number) {
        tmpInt = number.num.getValue();
    }

    public void visitStmt(Stmt stmt) {
        switch (stmt.type) {
            case "return":
                visitReturn(stmt);
                break;
            case "getint":
                visitLVal(stmt.lVal);
                Value right = tmpValue;
                tmpValue = f.getint(getRegNum(), curBlock);
                f.storeInst(curBlock, tmpValue, right);
                break;
            case "printf":
                gen(stmt);
                break;
            case "LVal":
                visitLVal(stmt.lVal);
                right = tmpValue;
                visitExp(stmt.lValExp);
                Value left = tmpValue;
                f.storeInst(curBlock, left, right);
                break;
            case "Exp":
                visitExp(stmt.exp);
                break;
            case "if":
                visitIf(stmt);
                break;
            case "while":
                visitWhile(stmt);
                break;
            case "break":
                visitBreak(stmt);
                break;
            case "continue":
                visitContinue(stmt);
                break;
            case "Block":
                visitBlock(stmt.block);
                break;
            default:
                break;
        }
    }

    public void visitReturn(Stmt stmt) {
        if (stmt.returnExp != null) {
            visitExp(stmt.returnExp);
            f.retInst(IntegerType.i32, curBlock, tmpValue);
        } else {
            f.retInst(VoidType.voidType, curBlock, null);
        }
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public void visitIf(Stmt stmt) {
        BasicBlock parent = curBlock;
        BasicBlock trueBlock = f.basicBlock(getBlockNum().concat("_if"), curFunction);
        BasicBlock nextBlock = f.basicBlock(getBlockNum().concat("_next"), curFunction);
        BasicBlock falseBlock = stmt.isElse ? f.basicBlock(getBlockNum().concat("_else"), curFunction) : nextBlock;
        stmt.ifCond.lOrExp.trueBlock = trueBlock;
        stmt.ifCond.lOrExp.falseBlock = falseBlock;
        visitLOrExp(stmt.ifCond.lOrExp);
        Value cond = tmpValue;
        curBlock = trueBlock;
        visitStmt(stmt.stmts.get(0));
        f.brInst(null, nextBlock, null, curBlock);
        if (stmt.isElse) {//有else块
            curBlock = falseBlock;
            stmt.stmts.get(1).nextBlock = nextBlock;
            visitStmt(stmt.stmts.get(1));
            f.brInst(null, nextBlock, null, curBlock);
            f.brInst(cond, trueBlock, falseBlock, parent);
            if (!(trueBlock.getLast() instanceof RetInst && falseBlock.getLast() instanceof RetInst)) {
                //最后一条指令都是return时就不需要跳转了
                f.brInst(null, nextBlock, null, trueBlock);
                f.brInst(null, nextBlock, null, falseBlock);
            }
        } else {
            f.brInst(cond, trueBlock, nextBlock, parent);
            f.brInst(null, nextBlock, null, trueBlock);
        }
        curBlock = nextBlock;
    }

    //'while' '(' Cond ')' Stmt
    public void visitWhile(Stmt stmt) {
        BasicBlock parent = curBlock;
        BasicBlock whileBlock = f.basicBlock(getBlockNum().concat("_while"), curFunction);
        BasicBlock condBlock = f.basicBlock(getBlockNum().concat("_if"), curFunction);
        BasicBlock nextBlock = f.basicBlock(getBlockNum().concat("_next"), curFunction);
        backPatch.push(new ArrayList<>());
        f.brInst(null, condBlock, null, parent);
        curBlock = condBlock;
        stmt.whileCond.lOrExp.trueBlock = whileBlock;
        stmt.whileCond.lOrExp.falseBlock = nextBlock;
        visitLOrExp(stmt.whileCond.lOrExp);
        curBlock = whileBlock;
        visitStmt(stmt.stmts.get(0));
        f.brInst(null, condBlock, null, curBlock);
        for (BrInst brInst : backPatch.pop()) {
            if (brInst.judge.equals("continue")) {
                brInst.set(condBlock);
            } else {
                brInst.set(nextBlock);
            }
        }
        curBlock = nextBlock;
    }

    public void visitBreak(Stmt stmt) {
        backPatch.peek().add(f.brInst("break", curBlock));
    }

    public void visitContinue(Stmt stmt) {
        backPatch.peek().add(f.brInst("continue", curBlock));
    }

    public void visitLOrExp(LOrExp lOrExp) {
        for (int i = 0; i < lOrExp.lAndExps.size() - 1; i++) {
            BasicBlock falseBlock = f.basicBlock(getBlockNum(), curFunction);
            lOrExp.lAndExps.get(i).falseBlock = falseBlock;
            lOrExp.lAndExps.get(i).trueBlock = lOrExp.trueBlock;
            visitLAndExp(lOrExp.lAndExps.get(i));
            curBlock = falseBlock;
        }
        int index = lOrExp.lAndExps.size() - 1;
        lOrExp.lAndExps.get(index).falseBlock = lOrExp.falseBlock;
        lOrExp.lAndExps.get(index).trueBlock = lOrExp.trueBlock;
        visitLAndExp(lOrExp.lAndExps.get(index));
    }

    public void visitLAndExp(LAndExp lAndExp) {
        for (int i = 0; i < lAndExp.eqExps.size(); i++) {
            BasicBlock trueBlock = f.basicBlock(getBlockNum(), curFunction);
            only = true;
            visitEqExp(lAndExp.eqExps.get(i));
            if (only) {
                only = false;
                if (tmpValue.type.isI1()) {
                    tmpValue = f.zextInst(getRegNum(), curBlock, tmpValue);
                }
                tmpValue = f.binaryInst(getRegNum(), curBlock, tmpValue, c.gen(0), TAG.ne);
            }
            f.brInst(tmpValue, trueBlock, lAndExp.falseBlock, curBlock);
            curBlock = trueBlock;
        }
        f.brInst(null, lAndExp.trueBlock, null, curBlock);
    }

    public void visitEqExp(EqExp eqExp) {
        visitRelExp(eqExp.relExps.get(0));
        Value left = tmpValue;
        for (int i = 0; i < eqExp.ops.size(); i++) {
            only = false;
            visitRelExp(eqExp.relExps.get(i + 1));
            if (left.type.isI1()) {
                left = f.zextInst(getRegNum(), curBlock, left);
            }
            if (tmpValue.type.isI1()) {
                tmpValue = f.zextInst(getRegNum(), curBlock, tmpValue);
            }
            if (eqExp.ops.get(i).getName().equals("==")) {
                left = f.binaryInst(getRegNum(), curBlock, left, tmpValue, TAG.eq);
            } else {
                left = f.binaryInst(getRegNum(), curBlock, left, tmpValue, TAG.ne);
            }
        }
        tmpValue = left;
    }

    public void visitRelExp(RelExp relExp) {
        visitAddExp(relExp.addExps.get(0));
        Value left = tmpValue;
        for (int i = 0; i < relExp.ops.size(); i++) {
            only = false;
            visitAddExp(relExp.addExps.get(i + 1));
            if (left.type.isI1()) {
                left = f.zextInst(getRegNum(), curBlock, left);
            }
            if (tmpValue.type.isI1()) {
                tmpValue = f.zextInst(getRegNum(), curBlock, tmpValue);
            }
            switch (relExp.ops.get(i).getName()) {
                case ">":
                    left = f.binaryInst(getRegNum(), curBlock, left, tmpValue, TAG.gt);
                    break;
                case "<":
                    left = f.binaryInst(getRegNum(), curBlock, left, tmpValue, TAG.lt);
                    break;
                case "<=":
                    left = f.binaryInst(getRegNum(), curBlock, left, tmpValue, TAG.le);
                    break;
                case ">=":
                    left = f.binaryInst(getRegNum(), curBlock, left, tmpValue, TAG.ge);
                    break;
                default:
                    break;
            }
        }
        tmpValue = left;
    }

    public void gen(Stmt stmt) {
        String s = stmt.fs.getName();
        ArrayList<Exp> exps = stmt.printfExps;
        ArrayList<Value> values = new ArrayList<>();
        for (Exp exp : exps) {
            visitExp(exp);
            values.add(tmpValue);
        }
        int pos = 0, last = 0, index = 0;
        Str str;
        while (last < s.length()) {
            pos = s.indexOf("%d", pos);
            if (pos == -1) {
                break;
            }
            String output = s.substring(last, pos);
            if (output.length() > 0) {
                str = f.str(getStrNum(), output.length(), output, module);
                f.putstr(getRegNum(), str, curBlock);
            }
            f.putint(values.get(index), curBlock);
            index++;
            pos += 2;
            last = pos;
        }
        if (last < s.length()) {
            String output = s.substring(last);
            str = f.str(getStrNum(), output.length(), output, module);
            f.putstr(getRegNum(), str, curBlock);
        }
    }

}

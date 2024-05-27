package errorHandler;

import frontend.Lexer.*;
import frontend.MyPair;
import frontend.Parser.Block.*;
import frontend.Parser.*;
import frontend.Parser.Decl.*;
import frontend.Parser.Exp.*;
import frontend.Parser.Exp.Number;
import frontend.Parser.Func.*;

import java.util.ArrayList;

public class ErrorHandler {
    private ArrayList<Token> tokens;
    private int pos;
    private static SyntacticalType syntacticalType = new SyntacticalType();
    private static LexicalType lexicalType = new LexicalType();
    private CompUnit compUnit;
    private ArrayList<String> prints = new ArrayList<>();
    private ErrorTable errorTable = new ErrorTable();
    public ArrayList<MyPair<String, Integer>> errors = new ArrayList<>();
    private int numWhile = 0;//记录有几层循环块
    private String retType = "";
    private boolean judgeA = true;//判定合法字符串
    private boolean assign = false;
    private boolean funcBlock;//标记是函数的block而非stmt的
    private boolean isDim = false;//是否为数组的维数
    private int dim2 = 0;//专门用来记录第二个维数，一遍对比
    private boolean pre = false;//记录预读lval方法
    private String curType = "";

    public ErrorHandler(ArrayList<Token> tokens) {
        this.tokens = tokens;
        compUnit = new CompUnit();
    }

    public CompUnit getCompUnit() {
        return compUnit;
    }

    public void addToken(int len) {
        for (int i = 0; i < len; i++) {
            Token token = tokens.get(pos);
            prints.add(lexicalType.getToken(token.getType()) + " " + token.getName());
            pos++;
        }
    }

    public ArrayList<String> getPrints() {
        return prints;
    }

    //  CompUnit → {Decl} {FuncDef} MainFuncDef
    public void parseCompUnit() {
        errorTable.addLayer();
        while (tokens.get(pos).getName().equals("const") || tokens.get(pos).getName().equals("int")) {
            if (tokens.get(pos).getName().equals("const")) {
                compUnit.addConstDecl(parseConstDecl());
            } else {
                if (tokens.get(pos + 2).getName().equals("(")) {
                    break;
                }
                compUnit.addVarDecl(parseVarDecl());
            }
        }
        while (tokens.get(pos).getName().equals("void") || tokens.get(pos).getName().equals("int")) {
            if (tokens.get(pos).getName().equals("void")) {
                compUnit.addFuncDef(parseFuncDef());
            } else {
                if (tokens.get(pos + 1).getName().equals("main")) {
                    break;
                }
                compUnit.addFuncDef(parseFuncDef());
            }
        }
        compUnit.addMainFuncDef(parseMainFuncDef());
        prints.add("<CompUnit>");
    }

    //  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public ConstDecl parseConstDecl() {
        ConstDecl constDecl = new ConstDecl();
        addToken(2);
        constDecl.addConstDef(parseConstDef());
        while (tokens.get(pos).getName().equals(",")) {
            addToken(1);
            constDecl.addConstDef(parseConstDef());
        }
        if (!tokens.get(pos).getName().equals(";")) {
            add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
        } else {
            addToken(1);
        }
        prints.add("<ConstDecl>");
        return constDecl;
    }

    //    VarDecl → BType VarDef { ',' VarDef } ';'
    public VarDecl parseVarDecl() {
        VarDecl varDecl = new VarDecl();
        addToken(1);
        varDecl.addVarDef(parseVarDef());
        while (tokens.get(pos).getName().equals(",")) {
            addToken(1);
            varDecl.addVarDef(parseVarDef());
        }
        if (!tokens.get(pos).getName().equals(";")) {
            add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
        } else {
            addToken(1);
        }
        prints.add("<VarDecl>");
        return varDecl;
    }

    // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public ConstDef parseConstDef() {
        ConstDef constDef = new ConstDef();
        Id id = ((Id) tokens.get(pos));
        constDef.addId(id);
        addToken(1);
        if (errorTable.findB(id.getName()) != null) {
            add(new MyPair<>("b", id.getLine()));
        }
        errorTable.put(constDef.id.getName(), constDef);
        int fl = 0;
        while (!tokens.get(pos).getName().equals("=")) {
            fl++;
            addToken(1);
            if (fl > 1) {
                dim2 = 1;
            }
            constDef.addConstExp(parseConstExp());
            if (fl > 1) {
                constDef.dim2 = dim2;
                dim2 = 0;
            }
            if (tokens.get(pos).getName().equals("]")) {
                addToken(1);
            } else {
                add(new MyPair<>("k", tokens.get(pos - 1).getLine()));
            }
        }
        addToken(1);
        constDef.addConstInitval(parseConstInitval());
        prints.add("<ConstDef>");
        return constDef;
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public FuncDef parseFuncDef() {
        FuncDef funcDef = new FuncDef();
        funcDef.addFuncType(parseFuncType());
        Id id = ((Id) tokens.get(pos));
        funcDef.addId(id);
        addToken(2);
        if (errorTable.findB(id.getName()) != null) {
            add(new MyPair<>("b", id.getLine()));
        }
        errorTable.put(funcDef.id.getName(), funcDef);
        errorTable.addLayer();
        if (!(tokens.get(pos).getName().equals(")") || tokens.get(pos).getName().equals("{"))) {
            funcDef.addFuncFParams(parseFuncFParams());
        }
        if (!tokens.get(pos).getName().equals(")")) {
            add(new MyPair<>("j", tokens.get(pos - 1).getLine()));
        } else {
            addToken(1);
        }
        funcBlock = true;
        funcDef.addBlock(parseBlock());
        prints.add("<FuncDef>");
        retType = "";
        errorTable.popLayer();
        return funcDef;
    }

    public FuncType parseFuncType() {
        FuncType funcType = new FuncType();
        funcType.setType(tokens.get(pos).getName());
        retType = funcType.type;
        addToken(1);
        prints.add("<FuncType>");
        return funcType;
    }

    //    MainFuncDef → 'int' 'main' '(' ')' Block
    public MainFuncDef parseMainFuncDef() {
        addToken(3);
        if (tokens.get(pos).getName().equals(")")) {
            addToken(1);
        } else {
            add(new MyPair<>("j", tokens.get(pos - 1).getLine()));
        }
        MainFuncDef mainFuncDef = new MainFuncDef();
        retType = "int";
        errorTable.put("main", mainFuncDef);
        errorTable.addLayer();
        funcBlock = true;
        mainFuncDef.addBlock(parseBlock());
        prints.add("<MainFuncDef>");
        errorTable.popLayer();
        retType = "";
        return mainFuncDef;
    }

    //    ConstExp → AddExp
    public ConstExp parseConstExp() {
        ConstExp constExp = new ConstExp();
        constExp.addAddExp(parseAddExp());
        prints.add("<ConstExp>");
        return constExp;
    }

    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public ConstInitval parseConstInitval() {
        ConstInitval constInitval = new ConstInitval();
        if (tokens.get(pos).getName().equals("{")) {
            if (!tokens.get(pos + 1).getName().equals("}")) {
                while (!tokens.get(pos).getName().equals("}")) {
                    addToken(1);
                    constInitval.addConstInitval(parseConstInitval());
                }
            } else {
                addToken(1);
            }
            addToken(1);
        } else {
            constInitval.addConstExp(parseConstExp());
        }
        prints.add("<ConstInitVal>");
        return constInitval;
    }

    //    VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    public VarDef parseVarDef() {
        VarDef varDef = new VarDef();
        Id id = ((Id) tokens.get(pos));
        varDef.addId(id);
        addToken(1);
        if (errorTable.findB(id.getName()) != null) {
            add(new MyPair<>("b", id.getLine()));
        }
        errorTable.put(varDef.id.getName(), varDef);
        int fl = 0;
        while (tokens.get(pos).getName().equals("[")) {
            fl++;
            addToken(1);
            if (fl > 1) {
                dim2 = 1;
            }
            varDef.addConstExp(parseConstExp());
            if (fl > 1) {
                varDef.dim2 = dim2;
                dim2 = 0;
            }
            if (tokens.get(pos).getName().equals("]")) {
                addToken(1);
            } else {
                add(new MyPair<>("k", tokens.get(pos - 1).getLine()));
            }
        }
        if (tokens.get(pos).getName().equals("=")) {
            addToken(1);
            varDef.addInitVal(parseInitVal());
        }
        prints.add("<VarDef>");
        return varDef;
    }

    //    InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public InitVal parseInitVal() {
        InitVal initVal = new InitVal();
        if (tokens.get(pos).getName().equals("{")) {
            while (!tokens.get(pos).getName().equals("}")) {
                addToken(1);
                initVal.addInitval(parseInitVal());
            }
            addToken(1);
        } else {
            initVal.addExp(parseExp());
        }
        prints.add("<InitVal>");
        return initVal;
    }

    public Exp parseExp() {
        Exp exp = new Exp();
        exp.addAddExp(parseAddExp());
        prints.add("<Exp>");
        return exp;
    }

    // FuncFParams → FuncFParam { ',' FuncFParam }
    public FuncFParams parseFuncFParams() {
        FuncFParams funcFParams = new FuncFParams();
        funcFParams.addFuncFParam(parseFuncFParam());
        while (tokens.get(pos).getName().equals(",")) {
            addToken(1);
            funcFParams.addFuncFParam(parseFuncFParam());
        }
        prints.add("<FuncFParams>");
        return funcFParams;
    }

    //    FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public FuncFParam parseFuncFParam() {
        FuncFParam funcFParam = new FuncFParam();
        addToken(1);//int
        Id id = ((Id) tokens.get(pos));
        funcFParam.addId(id);
        addToken(1);
        if (errorTable.findB(id.getName()) != null) {
            add(new MyPair<>("b", id.getLine()));
        }
        errorTable.put(funcFParam.id.getName(), funcFParam);
        if (tokens.get(pos).getName().equals("[")) {
            funcFParam.type = "Arr";
            addToken(1);
            if (tokens.get(pos).getName().equals("]")) {
                addToken(1);
            } else {
                add(new MyPair<>("k", tokens.get(pos - 1).getLine()));
            }
            while (tokens.get(pos).getName().equals("[")) {
                addToken(1);
                dim2 = 1;
                funcFParam.addConstExp(parseConstExp());
                funcFParam.dim2 = dim2;
                funcFParam.type = funcFParam.type.concat(String.valueOf(dim2));
                dim2 = 0;
                if (tokens.get(pos).getName().equals("]")) {
                    addToken(1);
                } else {
                    add(new MyPair<>("k", tokens.get(pos - 1).getLine()));
                }
            }
        }
        prints.add("<FuncFParam>");
        return funcFParam;
    }

    //Block → '{' { BlockItem } '}'
    public Block parseBlock() {
        Block block = new Block();
        addToken(1);
        if (funcBlock) {
            funcBlock = false;
            while (!tokens.get(pos).getName().equals("}")) {
                block.addBlockItem(parseBlockItem());
            }
            if (retType.equals("int")) {
                if (block.bLockItems.size() == 0) {
                    add(new MyPair<>("g", tokens.get(pos).getLine()));
                } else {
                    BLockItem last = block.getLast();
                    if (!last.type.equals("Stmt")) {
                        add(new MyPair<>("g", tokens.get(pos).getLine()));
                    } else {
                        Stmt stmt = last.stmt;
                        if (!stmt.type.equals("return")) {
                            add(new MyPair<>("g", tokens.get(pos).getLine()));
                        } else if (stmt.returnExp == null) {
                            add(new MyPair<>("g", tokens.get(pos).getLine()));
                        }
                    }
                }
            }
            addToken(1);
        } else {
            errorTable.addLayer();
            while (!tokens.get(pos).getName().equals("}")) {
                block.addBlockItem(parseBlockItem());
            }
            addToken(1);
            errorTable.popLayer();
        }
        prints.add("<Block>");
        return block;
    }

    //    BlockItem → Decl | Stmt
    public BLockItem parseBlockItem() {
        BLockItem bLockItem = new BLockItem();
        String type = tokens.get(pos).getName();
        if (type.equals("const")) {
            bLockItem.setType(type);
            bLockItem.addConstDecl(parseConstDecl());
        } else if (type.equals("int")) {
            bLockItem.setType(type);
            bLockItem.addVarDecl(parseVarDecl());
        } else {
            bLockItem.setType("Stmt");
            bLockItem.addStmt(parseStmt());
        }
        return bLockItem;
    }

    public Stmt parseStmt() {
        Stmt stmt = new Stmt();
        String judge = tokens.get(pos).getName();
        switch (judge) {
            case "if":
                stmt.setType("if");
                addToken(2);
                stmt.addCond(parseCond());
                if (!tokens.get(pos).getName().equals(")")) {
                    add(new MyPair<>("j", tokens.get(pos - 1).getLine()));
                } else {
                    addToken(1);
                }
                stmt.addStmt(parseStmt());
                if (tokens.get(pos).getName().equals("else")) {
                    addToken(1);
                    stmt.isElse = true;
                    stmt.addStmt(parseStmt());
                }
                break;
            case "while":
                numWhile++;
                stmt.setType("while");
                addToken(2);
                stmt.addCond(parseCond());
                if (!tokens.get(pos).getName().equals(")")) {
                    add(new MyPair<>("j", tokens.get(pos - 1).getLine()));
                } else {
                    addToken(1);
                }
                stmt.addStmt(parseStmt());
                numWhile--;
                break;
            case "break":
            case "continue":
                if (numWhile == 0) {
                    add(new MyPair<>("m", tokens.get(pos).getLine()));
                }
                addToken(1);
                if (tokens.get(pos).getName().equals(";")) {
                    addToken(1);
                } else {
                    add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
                }
                stmt.setType(judge);
                break;
            case "return":
                addToken(1);
                stmt.setType(judge);
                if (isFirstOfExp(tokens.get(pos))) {
                    if (retType.equals("void")) {
                        add(new MyPair<>("f", tokens.get(pos - 1).getLine()));
                    }
                    stmt.addExp(parseExp());
                }
                if (!tokens.get(pos).getName().equals(";")) {
                    add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
                } else {
                    addToken(1);
                }
                break;
            case "printf":
                int line = tokens.get(pos).getLine();
                addToken(2);
                stmt.setType(judge);
                stmt.addFS(((FS) tokens.get(pos)));
                int num = scanFS(stmt.fs), sum = 0;
                if (!judgeA) {
                    add(new MyPair<>("a", tokens.get(pos).getLine()));
                    judgeA = true;//judgeA为true代表没错误
                }
                addToken(1);
                while (tokens.get(pos).getName().equals(",")) {
                    addToken(1);
                    stmt.addExp(parseExp());
                    sum++;
                }
                if (num != sum) {//个数不匹配
                    add(new MyPair<>("l", line));
                }
                if (!tokens.get(pos).getName().equals(")")) {
                    add(new MyPair<>("j", tokens.get(pos - 1).getLine()));
                } else {
                    addToken(1);
                }
                if (!tokens.get(pos).getName().equals(";")) {
                    add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
                } else {
                    addToken(1);
                }
                break;
            case "{":
                stmt.setType("Block");
                stmt.addBlock(parseBlock());
                break;
            default:
                Token token = tokens.get(pos);
                if (isFirstOfExp(token)) {
                    stmt.setType("Exp");
                    stmt.addExp(parseExp());
                    if (!tokens.get(pos).getName().equals(";")) {
                        add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
                    } else {
                        addToken(1);
                    }
                } else if (token instanceof Id) {
                    assign = true;
                    stmt.addLVal(parseLVal());
                    assign = false;
                    addToken(1);//=
                    token = tokens.get(pos);
                    if (token instanceof Reserve && token.getName().equals("getint")) {
                        stmt.setType("getint");
                        addToken(2);
                        if (!tokens.get(pos).getName().equals(")")) {
                            add(new MyPair<>("j", tokens.get(pos - 1).getLine()));
                        } else {
                            addToken(1);
                        }
                        if (!tokens.get(pos).getName().equals(";")) {
                            add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
                        } else {
                            addToken(1);
                        }
                    } else {
                        stmt.setType("LVal");
                        stmt.addExp(parseExp());
                        if (!tokens.get(pos).getName().equals(";")) {
                            add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
                        } else {
                            addToken(1);
                        }
                    }
                } else if (token instanceof Sep && ((Sep) token).getC() == ';') {
                    addToken(1);
                } else {
                    add(new MyPair<>("i", tokens.get(pos - 1).getLine()));
                }
                break;
        }
        prints.add("<Stmt>");
        return stmt;
    }

    private boolean isFirstOfExp(Token token) {
        if (token instanceof Sep) {
            return ((Sep) token).getC() == '(';
        } else if (token instanceof Op) {
            String s = ((Op) token).getName();
            return s.equals("+") || s.equals("-") || s.equals("!");
        } else if (token instanceof Id) {
            token = tokens.get(pos + 1);
            if (token instanceof Sep && ((Sep) token).getC() == '(') {
                return true;
            } else if (token instanceof Sep && ((Sep) token).getC() == ';') {
                return true;
            } else if (token instanceof Op && ((Op) token).getName().equals("=")) {
                return false;
            } else if (token instanceof Sep && ((Sep) token).getName().equals("[")) {
                int temp = pos;
                pre = true;
                parseLVal();
                pre = false;
                if (tokens.get(pos).getName().equals("=")) {
                    pos = temp;
                    return false;
                } else {
                    pos = temp;
                    return true;
                }
            } else {
                return true;
            }
        } else return token instanceof Num;
    }

    //LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    public LVal parseLVal() {
        LVal lVal = new LVal();
        Id id = (Id) tokens.get(pos);
        lVal.addId(id);
        Ident i = errorTable.find(id.getName());
        if (i == null) {
            add(new MyPair<>("c", id.getLine()));
        }
        if (i instanceof ConstDef && assign) {
            add(new MyPair<>("h", id.getLine()));
        }
        int num = 0;
        addToken(1);
        while (tokens.get(pos).getName().equals("[")) {
            num++;
            addToken(1);
            isDim = true;
            lVal.addExp(parseExp());
            isDim = false;
            if (tokens.get(pos).getName().equals("]")) {
                addToken(1);
            } else {
                add(new MyPair<>("k", tokens.get(pos - 1).getLine()));
            }
        }
        if (errorTable.types.size() > 0 && !isDim && !pre) {
            if (i instanceof ConstDef) {
                if (((ConstDef) i).type.equals("Ident")) {
                    curType = ("int");
                } else if (((ConstDef) i).type.equals("Arr")) {
                    if (num == 0) {
                        curType = ("Arr");
                    } else {
                        curType = "int";
                    }
                } else {
                    if (num == 0) {
                        curType = "twoDimArr" + ((ConstDef) i).dim2;
                    } else if (num == 1) {
                        curType = ("Arr");
                    } else {
                        curType = ("int");
                    }
                }
            } else if (i instanceof VarDef) {
                if (((VarDef) i).type.equals("Ident")) {
                    curType = "int";
                } else if (((VarDef) i).type.equals("Arr")) {
                    if (num == 0) {
                        curType = ("Arr");
                    } else {
                        curType = ("int");
                    }
                } else {
                    if (num == 0) {
                        curType = "twoDimArr" + ((VarDef) i).dim2;
                    } else if (num == 1) {
                        curType = ("Arr");
                    } else {
                        curType = ("int");
                    }
                }
            } else if (i instanceof FuncDef) {
                curType = ((FuncDef) i).funcType.type;
            } else if (i instanceof FuncFParam) {
                if (((FuncFParam) i).type.equals("int")) {
                    curType = ("int");
                } else if (((FuncFParam) i).type.equals("Arr")) {
                    if (num == 0) {
                        curType = ("Arr");
                    } else {
                        curType = ("int");
                    }
                } else {
                    if (num == 0) {
                        curType = "twoDimArr" + ((FuncFParam) i).dim2;
                    } else if (num == 1) {
                        curType = ("Arr");
                    } else {
                        curType = ("int");
                    }
                }
            }
        }
        prints.add("<LVal>");
        return lVal;
    }

    public Cond parseCond() {
        Cond cond = new Cond();
        cond.addLOrExp(parseLOrExp());
        prints.add("<Cond>");
        return cond;
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number
    public PrimaryExp parsePrimaryExp() {
        PrimaryExp primaryExp = new PrimaryExp();
        if (tokens.get(pos).getName().equals("(")) {
            primaryExp.setType("Exp");
            addToken(1);
            primaryExp.addExp(parseExp());
            if (!tokens.get(pos).getName().equals(")")) {
                add(new MyPair<>("j", tokens.get(pos - 1).getLine()));
            } else {
                addToken(1);
            }
        } else if (tokens.get(pos).getType().equals("IntConst")) {
            primaryExp.setType("IntConst");
            primaryExp.addNumber(parseNumber());
        } else {
            primaryExp.setType("LVal");
            primaryExp.addLVal(parseLVal());
        }
        prints.add("<PrimaryExp>");
        return primaryExp;
    }

    public Number parseNumber() {
        Number number = new Number();
        number.addIntConst(((Num) tokens.get(pos)));
        if (dim2 > 0) {
            dim2 = number.num.getValue();
        }
        addToken(1);
        if (errorTable.types.size() > 0 && !isDim && !pre) {
            curType = ("int");
        }
        prints.add("<Number>");
        return number;
    }

    //    UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'  | UnaryOp UnaryExp
    public UnaryExp parseUnaryExp() {
        UnaryExp unaryExp = new UnaryExp();
        Token token = tokens.get(pos);
        if (syntacticalType.isUnaryOp(token)) {
            unaryExp.setType("UnaryExp");
            unaryExp.addUnaryOp(parseUnaryOp());
            unaryExp.addUnaryExp(parseUnaryExp());
        } else if (token instanceof Num) {
            unaryExp.setType("PrimaryExp");
            unaryExp.addPrimaryExp(parsePrimaryExp());
        } else if (token.getName().equals("(")) {
            unaryExp.setType("PrimaryExp");
            unaryExp.addPrimaryExp(parsePrimaryExp());
        } else if (tokens.get(pos + 1).getName().equals("(")) {
            unaryExp.setType("FuncR");
            Id id = (Id) token;
            unaryExp.addIdent(id);
            Ident i = errorTable.find(id.getName());
            if (!(i instanceof FuncDef)) {
                add(new MyPair<>("c", id.getLine()));
            }
            addToken(2);
            errorTable.addType();
            unaryExp.addFuncRParams(parseFuncRParams());
            if (i instanceof FuncDef) {
                ArrayList<FuncFParam> funcFParams = ((FuncDef) i).funcFParams.funcFParams;
                if (funcFParams.size() != errorTable.topType().size()) {
                    add(new MyPair<>("d", id.getLine()));
                } else {
                    for (int j = 0; j < funcFParams.size(); j++) {
                        if (!(errorTable.topType().get(j).equals(funcFParams.get(j).type))) {
                            add(new MyPair<>("e", id.getLine()));
                            break;
                        }
                    }
                }
            }
            errorTable.popType();
            if (errorTable.types.size() > 0 && !isDim && i instanceof FuncDef && !pre) {
                curType = ((FuncDef) i).funcType.type;
            }
            if (!tokens.get(pos).getName().equals(")")) {
                add(new MyPair<>("j", id.getLine()));
            } else {
                addToken(1);
            }
        } else {
            unaryExp.setType("PrimaryExp");
            unaryExp.addPrimaryExp(parsePrimaryExp());
        }
        prints.add("<UnaryExp>");
        return unaryExp;
    }

    public UnaryOp parseUnaryOp() {
        UnaryOp unaryOp = new UnaryOp();
        unaryOp.setType(tokens.get(pos).getName());
        addToken(1);
        prints.add("<UnaryOp>");
        return unaryOp;
    }

    //FuncRParams → Exp { ',' Exp }
    public FuncRParams parseFuncRParams() {
        FuncRParams funcRParams = new FuncRParams();
        while (isFirstOfExp(tokens.get(pos))) {
            funcRParams.addExp(parseExp());
            errorTable.putType(curType);
            if (tokens.get(pos).getName().equals(",")) {
                addToken(1);
            }
        }
        prints.add("<FuncRParams>");
        return funcRParams;
    }

    public MulExp parseMulExp() {
        MulExp mulExp = new MulExp();
        mulExp.addUnaryExp(parseUnaryExp());
        prints.add("<MulExp>");
        while (syntacticalType.isMulOp(tokens.get(pos))) {
            mulExp.addOp(((Op) tokens.get(pos)));
            addToken(1);
            mulExp.addUnaryExp(parseUnaryExp());
            prints.add("<MulExp>");
        }
        return mulExp;
    }

    public AddExp parseAddExp() {
        AddExp addExp = new AddExp();
        addExp.addMulExp(parseMulExp());
        prints.add("<AddExp>");
        while (syntacticalType.isAddOp(tokens.get(pos))) {
            addExp.addOp(((Op) tokens.get(pos)));
            addToken(1);
            addExp.addMulExp(parseMulExp());
            prints.add("<AddExp>");
        }
        return addExp;
    }

    public RelExp parseRelExp() {
        RelExp relExp = new RelExp();
        relExp.addAddExp(parseAddExp());
        prints.add("<RelExp>");
        while (syntacticalType.isRelOp(tokens.get(pos))) {
            relExp.addOp(((Op) tokens.get(pos)));
            addToken(1);
            relExp.addAddExp(parseAddExp());
            prints.add("<RelExp>");
        }
        return relExp;
    }

    public EqExp parseEqExp() {
        EqExp eqExp = new EqExp();
        eqExp.addRelExp(parseRelExp());
        prints.add("<EqExp>");
        while (syntacticalType.isEqOp(tokens.get(pos))) {
            eqExp.addOp(((Op) tokens.get(pos)));
            addToken(1);
            eqExp.addRelExp(parseRelExp());
            prints.add("<EqExp>");
        }
        return eqExp;
    }

    public LAndExp parseLAndExp() {
        LAndExp lAndExp = new LAndExp();
        lAndExp.addEqExp(parseEqExp());
        prints.add("<LAndExp>");
        while (syntacticalType.isLAndOp(tokens.get(pos))) {
            lAndExp.addOp(((Op) tokens.get(pos)));
            addToken(1);
            lAndExp.addEqExp(parseEqExp());
            prints.add("<LAndExp>");
        }
        return lAndExp;
    }

    public LOrExp parseLOrExp() {
        LOrExp lOrExp = new LOrExp();
        lOrExp.addLAndExp(parseLAndExp());
        prints.add("<LOrExp>");
        while (syntacticalType.isLOrOp(tokens.get(pos))) {
            lOrExp.addOp(((Op) tokens.get(pos)));
            addToken(1);
            lOrExp.addLAndExp(parseLAndExp());
            prints.add("<LOrExp>");
        }
        return lOrExp;
    }

    //<Char> → <FormatChar> | <NormalChar>
    // %d 或者 32、33、40-126 \当且仅当\n
    private int scanFS(FS fs) {
        String content = fs.content;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '%') {
                if (!(i < content.length() - 1 && content.charAt(i + 1) == 'd')) {// %d
                    judgeA = false;
                }
            } else if (c == 92) {// \n
                if (!(i < content.length() - 1 && content.charAt(i + 1) == 'n')) {
                    judgeA = false;
                }
            } else {
                if (!(c == 32 || c == 33 || (40 <= c && c <= 126))) {
                    judgeA = false;
                }
            }
        }
        return (content.length() - content.replace("%d", "").length()) / 2;
        //返回%d个数
    }

    public void add(MyPair<String, Integer> error) {
        if (errors.size() == 0) {
            errors.add(error);
        } else {
            int line = errors.get(errors.size() - 1).getValue();
            if (line != error.getValue()) {
                errors.add(error);
            }
        }
    }

}

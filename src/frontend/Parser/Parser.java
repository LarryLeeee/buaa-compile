package frontend.Parser;

import frontend.Lexer.*;
import frontend.Parser.Block.*;
import frontend.Parser.Decl.*;
import frontend.Parser.Exp.*;
import frontend.Parser.Exp.Number;
import frontend.Parser.Func.*;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokens;
    private int pos;
    private static SyntacticalType syntacticalType = new SyntacticalType();
    private static LexicalType lexicalType = new LexicalType();
    private CompUnit compUnit;
    private ArrayList<String> prints = new ArrayList<>();

    public Parser(ArrayList<Token> tokens) {
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
        addToken(1);
        prints.add("<ConstDecl>");
        return constDecl;
    }

    //    VarDecl → BType VarDef { ',' VarDef } ';'
    public VarDecl parseVarDecl() {
        VarDecl varDecl = new VarDecl();
        addToken(1);
        varDecl.addVarDef(parseVarDef());
        while (!tokens.get(pos).getName().equals(";")) {
            addToken(1);
            varDecl.addVarDef(parseVarDef());
        }
        addToken(1);
        prints.add("<VarDecl>");
        return varDecl;
    }

    // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public ConstDef parseConstDef() {
        ConstDef constDef = new ConstDef();
        constDef.addId(((Id) tokens.get(pos)));
        addToken(1);
        while (!tokens.get(pos).getName().equals("=")) {
            addToken(1);
            constDef.addConstExp(parseConstExp());
            addToken(1);
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
        funcDef.addId(((Id) tokens.get(pos)));
        addToken(2);
        if (!tokens.get(pos).getName().equals(")")) {
            funcDef.addFuncFParams(parseFuncFParams());
        }
        addToken(1);
        funcDef.addBlock(parseBlock());
        prints.add("<FuncDef>");
        return funcDef;
    }

    public FuncType parseFuncType() {
        FuncType funcType = new FuncType();
        funcType.setType(tokens.get(pos).getName());
        addToken(1);
        prints.add("<FuncType>");
        return funcType;
    }

    //    MainFuncDef → 'int' 'main' '(' ')' Block
    public MainFuncDef parseMainFuncDef() {
        addToken(4);
        MainFuncDef mainFuncDef = new MainFuncDef();
        mainFuncDef.addBlock(parseBlock());
        prints.add("<MainFuncDef>");
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
    // Ident '=' 'getint' '(' ')'
    public VarDef parseVarDef() {
        VarDef varDef = new VarDef();
        varDef.addId(((Id) tokens.get(pos)));
        addToken(1);
        while (tokens.get(pos).getName().equals("[")) {
            addToken(1);
            varDef.addConstExp(parseConstExp());
            addToken(1);
        }
        if (tokens.get(pos).getName().equals("=")) {
            addToken(1);
            if (tokens.get(pos).getName().equals("getint")) {
                varDef.isGetint = true;
                addToken(3);
            } else {
                varDef.addInitVal(parseInitVal());
            }
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
        addToken(1);
        funcFParam.addId(((Id) tokens.get(pos)));
        addToken(1);
        if (tokens.get(pos).getName().equals("[")) {
            funcFParam.type = "Arr";
            addToken(2);
            while (tokens.get(pos).getName().equals("[")) {
                addToken(1);
                funcFParam.addConstExp(parseConstExp());
                addToken(1);
            }
        }
        prints.add("<FuncFParam>");
        return funcFParam;
    }

    //Block → '{' { BlockItem } '}'
    public Block parseBlock() {
        Block block = new Block();
        addToken(1);
        while (!tokens.get(pos).getName().equals("}")) {
            block.addBlockItem(parseBlockItem());
        }
        addToken(1);
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
                addToken(1);
                stmt.addStmt(parseStmt());
                if (tokens.get(pos).getName().equals("else")) {
                    addToken(1);
                    stmt.isElse = true;
                    stmt.addStmt(parseStmt());
                }
                break;
            case "while":
                stmt.setType("while");
                addToken(2);
                stmt.addCond(parseCond());
                addToken(1);
                stmt.addStmt(parseStmt());
                break;
            case "break":
            case "continue":
                addToken(2);
                stmt.setType(judge);
                break;
            case "return":
                addToken(1);
                stmt.setType(judge);
                if (!tokens.get(pos).getName().equals(";")) {
                    stmt.addExp(parseExp());
                }
                addToken(1);
                break;
            case "printf":
                addToken(2);
                stmt.setType(judge);
                stmt.addFS(((FS) tokens.get(pos)));
                addToken(1);
                while (tokens.get(pos).getName().equals(",")) {
                    addToken(1);
                    stmt.addExp(parseExp());
                }
                addToken(2);
                break;
            case "{":
                stmt.setType("Block");
                stmt.addBlock(parseBlock());
                break;
            default:
                int i = pos, flag = 0;
                while (!tokens.get(i).getName().equals(";")) {
                    if (tokens.get(i).getName().equals("=")) {
                        flag = 1;
                    }
                    i++;
                }
                if (flag == 1) {
                    stmt.setType("LVal");
                    stmt.addLVal(parseLVal());
                    addToken(1);
                    if (tokens.get(pos).getName().equals("getint")) {
                        stmt.setType("getint");
                        addToken(4);
                    } else {
                        stmt.addExp(parseExp());
                        addToken(1);
                    }
                } else {
                    stmt.setType("Exp");
                    if (!tokens.get(pos).getName().equals(";")) {
                        stmt.addExp(parseExp());
                    }
                    addToken(1);
                }
                break;
        }
        prints.add("<Stmt>");
        return stmt;
    }

    //LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    public LVal parseLVal() {
        LVal lVal = new LVal();
        lVal.addId(((Id) tokens.get(pos)));
        addToken(1);
        while (tokens.get(pos).getName().equals("[")) {
            addToken(1);
            lVal.addExp(parseExp());
            addToken(1);
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
            addToken(1);
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
        addToken(1);
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
            unaryExp.addIdent(((Id) token));
            addToken(2);
            if (!tokens.get(pos).getName().equals(")")) {
                unaryExp.addFuncRParams(parseFuncRParams());
            }
            addToken(1);
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
        funcRParams.addExp(parseExp());
        while (tokens.get(pos).getName().equals(",")) {
            addToken(1);
            funcRParams.addExp(parseExp());
        }
        prints.add("<FuncRParams>");
        return funcRParams;
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%' | 'bitand' ) UnaryExp
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

}

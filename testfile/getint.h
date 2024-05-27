#include<stdio.h>

int getint() {
    int n;
    scanf("%d", &n);
    return n;
}

// CompUnit → {Decl} {FuncDef} MainFuncDef
// MainFuncDef → Block → BlockItem →Decl|Stmt
//
// 进入Decl模块
// Decl → constDel|varDel
// constDel → const int 一至多个ConstDef
// 多个需要处理的有：0、1、2、3（看要求是[]还是{}）
// ConstDef → 标识符（普通变量、一维数组、二维数组）= 常量初值
// 常量初值→常量表达式初值|一维数组初值|二维数组初值，初值里面仍然包含常量初值
// varDel→ int varDef 一至多个
// varDef → 普通变量|一维数组|二维数组 = 变量初值
// 变量初值→ 分别对应普通变量|一维数组|二维数组
//
// 进入表达式模块
// 常量表达式 → 加减表达式 → 乘除模表达式：需要覆盖加减和乘除模 → 基元为一元表达式
// 一元表达式 → 基本表达式|函数调用(传参)|单目运算符 一元表达式：覆盖有无参数两种情况
// 单目运算符：+ - ! 仅出现在条件表达式中
// 基本表达式→(表达式)|左值表达式|数字
// 左值表达式→1.普通变量 2.一维数组 3.二维数组
// 表达式→加减表达式
//
// 进入函数模块
// 函数调用需要覆盖函数实参无参数以及多个，以及数组传参和部分数组传参
// FuncDef → void\int 标识符 零至多个形参 Block
// 函数形参→ int 变量|一维数组|二维数组
//
// 进入语句模块Stmt
// Stmt → 左值表达式 = 表达式 | 语句块
// |if(Cond) Stmt [else Stmt] 需要覆盖有无else两种情况
// |while(Cond) Stmt
// |break;|continue;
// |return [Exp] 覆盖有无表达式两种情况
// |左值表达式 = getint();
// printf(格式化字符串，一个至多个要输出的) 覆盖一至多个要输出的情况
//
// 进入条件表达式模块
// Cond → LOrExp → LAndExp | LOrExp||LAndExp 均需要覆盖！
// LAndExp → EqExp | LAndExp '&&' EqExp  均需覆盖
// EqExp → RelExp| EqExp==/!= RelExp 均需要覆盖
// RelExp → 加减表达式|RelExp ('<' | '>' | '<=' | '>=') 加减表达式 均需要覆盖
//
// 标识符→非数字|标识符 非数字|标识符 数字
// 注释:单行多行
// 数字：所有非负：0和一至多位
//
// B: 所有包含数组的均为
// A:LOrExp → LAndExp | LOrExp||LAndExp 均需要覆盖！
// LAndExp → EqExp | LAndExp '&&' EqExp  均需覆盖
//
// 语义：
// 数组至多二维，且数量和定义一定相同
// I/O
// 顶层变量、常量、函数名字定义不可以重复，作用域
// 定义数组维数时，可以用表达式，此外必须已知
// 初值：可以使用引用变量来初始化，
// 有返回值的函数最后一句必然显示return
// 一元表达式：int a = +-+i：类似于递归下降表达式定义
//
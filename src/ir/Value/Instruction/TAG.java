package ir.Value.Instruction;

public enum TAG {
    //terminate
    br, ret, call,
    //mem
    alloca, load, store, gep, phi, zext, memphi,
    //binary
    add, sub, mul, sdiv, mod, lt, le, ge, gt, eq, ne,and,
    //IO
    getint, putstr, putint,
}

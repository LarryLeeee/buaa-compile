package ir.Value;

import ir.Type.LabelType;
import ir.Value.Instruction.Instruction;

import java.util.ArrayList;

public class BasicBlock extends Value {

    public Function parent;
    public ArrayList<Instruction> instructions;
    public ArrayList<BasicBlock> predecessors = new ArrayList<>();
    public ArrayList<BasicBlock> successors = new ArrayList<>();
    public boolean isVisit = false;


    public BasicBlock(String name, Function parent) {
        super(name, LabelType.labeltype);
        instructions = new ArrayList<>();
        this.parent = parent;
        parent.addBasicBlock(this);
    }

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    public void getOutputs(ArrayList<String> outputs) {
        outputs.add(name.substring(1) + ":");
        for (Instruction instr : instructions) {
            instr.getOutputs(outputs);
        }
    }

    public Instruction getLast() {
        int len = instructions.size();
        if (len == 0) {
            return null;
        }
        return instructions.get(len - 1);
    }

    public void addPre(BasicBlock block) {
        predecessors.add(block);
    }

    public void addSuc(BasicBlock block) {
        successors.add(block);
    }

}

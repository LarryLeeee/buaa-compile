import backend.MipsGen;
import errorHandler.ErrorHandler;
import frontend.Lexer.Lexer;
import frontend.Lexer.Token;
import frontend.MyPair;
import frontend.Parser.Parser;
import frontend.Visitor;
import ir.Value.Module;
import pass.Inline;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;

public class Compiler {

    public static void main(String[] args) {
        try {
            Lexer lexer = new Lexer();
            boolean checkError = false;
            boolean isInline = true;
            boolean mem2reg = false;
            File file1 = new File("testfile.txt");
            if (file1.isFile() && file1.exists()) {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file1));
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    lexer.setInput(lineTxt);
                }
                read.close();
            } else {
                System.out.println("File not found!");
            }
            ArrayList<Token> tokens = lexer.getTokens();
            if (checkError) {
                File file2 = new File("error.txt");
                FileWriter fileWriter = new FileWriter(file2);
                ErrorHandler errorHandler = new ErrorHandler(tokens);
                errorHandler.parseCompUnit();
                for (MyPair<String, Integer> error : errorHandler.errors) {
                    fileWriter.write(error.getValue() + " " + error.getKey() + "\n");
                }
                fileWriter.close();
            } else {
                File file2 = new File("llvm_ir.txt");
                FileWriter fileWriter = new FileWriter(file2);
                Parser parser = new Parser(tokens);
                parser.parseCompUnit();
                Visitor visitor = new Visitor(parser.getCompUnit());
                visitor.visitCompUnit();
                Module module = visitor.getModule();
                if (isInline) {
                    Inline inline = new Inline(module);
                    module = inline.module;
                }

                ArrayList<String> outputs = new ArrayList<>();
                module.getOutputs(outputs);
                for (String string : outputs) {
                    fileWriter.write(string + "\n");
                }
                fileWriter.close();

                File file3 = new File("mips.txt");
                FileWriter fileWriter_ = new FileWriter(file3);
                MipsGen mipsGen = new MipsGen(module);
                mipsGen.GenModule();
                ArrayList<String> mips = new ArrayList<>();
                mipsGen.mipsModule.getOutPuts(mips);
                for (String string : mips) {
                    fileWriter_.write(string + "\n");
                }
                fileWriter_.close();

            }
        } catch (Exception e) {
            System.out.println("Error!");
            e.printStackTrace();
        }
    }
}



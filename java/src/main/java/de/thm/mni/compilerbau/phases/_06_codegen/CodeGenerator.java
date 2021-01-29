package de.thm.mni.compilerbau.phases._06_codegen;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.phases._04b_semant.ProcedureBodyChecker;
import de.thm.mni.compilerbau.phases._05_varalloc.VarAllocator;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.utils.NotImplemented;
import de.thm.mni.compilerbau.utils.SplError;

import java.io.PrintWriter;

/**
 * This class is used to generate the assembly code for the compiled program.
 * This code is emitted via the {@link CodePrinter} in the output field of this class.
 */
public class CodeGenerator {
    private final CodePrinter output;

    public CodeGenerator(PrintWriter output) {
        this.output = new CodePrinter(output);
    }

    /**
     * Emits needed import statements, to allow usage of the predefined functions and sets the correct settings
     * for the assembler.
     */
    private void assemblerProlog() {
        output.emitImport("printi");
        output.emitImport("printc");
        output.emitImport("readi");
        output.emitImport("readc");
        output.emitImport("exit");
        output.emitImport("time");
        output.emitImport("clearAll");
        output.emitImport("setPixel");
        output.emitImport("drawLine");
        output.emitImport("drawCircle");
        output.emitImport("_indexError");
        output.emit("");
        output.emit("\t.code");
        output.emit("\t.align\t4");

    }

    public void generateCode(Program program, SymbolTable table) {
        assemblerProlog();
        program.accept(new CodeVisitor(table));
    }

    class CodeVisitor extends DoNothingVisitor {
        SymbolTable symbolTable;
        SymbolTable globalTable;
        int registerPointer = 8;
        String label;
        int labelCount;
        Register zero = new Register(0);
        Register fp = new Register(25);
        Register sp = new Register(29);
        Register ret = new Register(31);

        CodeVisitor(SymbolTable symbolTable) {
            this.symbolTable = symbolTable;
            this.labelCount = 0;
        }

        CodeVisitor(SymbolTable symbolTable, SymbolTable globalTable, int labelCount) {
            this.symbolTable = symbolTable;
            this.globalTable = globalTable;
            this.labelCount = labelCount;
        }

        public void visit(Program program) {
            program.declarations.forEach(dec -> dec.accept(this));
        }

        public void visit(ProcedureDeclaration procedureDeclaration) {
            ProcedureEntry entry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
            SymbolTable localTable = entry.localTable;
            CodeVisitor vistor = new CodeVisitor(localTable, symbolTable, labelCount);
            output.emit("");
            output.emitExport(procedureDeclaration.name.toString());
            output.emitLabel(procedureDeclaration.name.toString());
            int framesize = entry.localVarAreaSize + 4;
            if (entry.outgoingAreaSize > -1) framesize += entry.outgoingAreaSize + 4;
            int fpOffset = entry.outgoingAreaSize > -1 ? entry.outgoingAreaSize + 4 : 0;
            int retOffset = -(entry.localVarAreaSize + 8);
            output.emitInstruction("sub", sp, sp, framesize, "allocate frame");
            output.emitInstruction("stw", fp, sp, fpOffset, "save old frame pointer");
            output.emitInstruction("add", fp, sp, framesize, "setup new frame pointer");
            if (entry.outgoingAreaSize > -1) output.emitInstruction("stw", ret, fp, retOffset, "save return register");
            procedureDeclaration.body.forEach(statement -> statement.accept(vistor));
            if (entry.outgoingAreaSize > -1)
                output.emitInstruction("ldw", ret, fp, retOffset, "restore return register");
            output.emitInstruction("ldw", fp, sp, fpOffset, "restore old frame pointer");
            output.emitInstruction("add", sp, sp, framesize, "release frame");
            output.emitInstruction("jr", ret, "return");
            labelCount = vistor.labelCount;
        }

        public void visit(IntLiteral intLiteral) {
            output.emitInstruction("add", new Register(registerPointer), zero, intLiteral.value);
            registerPointer++;
        }

        public void visit(BinaryExpression binaryExpression) {
            binaryExpression.leftOperand.accept(this);
            binaryExpression.rightOperand.accept(this);
            switch (binaryExpression.operator) {
                case ADD:
                    output.emitInstruction("add", new Register(registerPointer - 2), new Register(registerPointer - 2), new Register(registerPointer - 1));
                    break;
                case SUB:
                    output.emitInstruction("sub", new Register(registerPointer - 2), new Register(registerPointer - 2), new Register(registerPointer - 1));
                    break;
                case MUL:
                    output.emitInstruction("mul", new Register(registerPointer - 2), new Register(registerPointer - 2), new Register(registerPointer - 1));
                    break;
                case DIV:
                    output.emitInstruction("div", new Register(registerPointer - 2), new Register(registerPointer - 2), new Register(registerPointer - 1));
                    break;
                case EQU:
                    output.emitInstruction("bne", new Register(registerPointer - 2), new Register(registerPointer - 1), label);
                    break;
                case NEQ:
                    output.emitInstruction("beq", new Register(registerPointer - 2), new Register(registerPointer - 1), label);
                    break;
                case LST:
                    output.emitInstruction("bge", new Register(registerPointer - 2), new Register(registerPointer - 1), label);
                    break;
                case LSE:
                    output.emitInstruction("bgt", new Register(registerPointer - 2), new Register(registerPointer - 1), label);
                    break;
                case GRE:
                    output.emitInstruction("blt", new Register(registerPointer - 2), new Register(registerPointer - 1), label);
                    break;
                case GRT:
                    output.emitInstruction("ble", new Register(registerPointer - 2), new Register(registerPointer - 1), label);
                    break;
            }
            if (!(binaryExpression.operator.isArithmetic())) registerPointer--;
            registerPointer--;
        }

        public void visit(VariableExpression variableExpression) {
            variableExpression.variable.accept(this);
            output.emitInstruction("ldw", new Register(registerPointer - 1), new Register(registerPointer - 1), 0);
        }

        public void visit(AssignStatement assignStatement) {
            assignStatement.target.accept(this);
            assignStatement.value.accept(this);
            output.emitInstruction("stw", new Register(registerPointer - 1), new Register(registerPointer - 2), 0);
            registerPointer -= 2;
        }

        public void visit(ArrayAccess arrayAccess) {
            arrayAccess.array.accept(this);
            arrayAccess.index.accept(this);
            output.emitInstruction("add", new Register(registerPointer), zero, arrayAccess.array.dataType.byteSize / 4);
            output.emitInstruction("bgeu", new Register(registerPointer - 1), new Register(registerPointer), "_indexError");
            registerPointer--;
            output.emitInstruction("mul", new Register(registerPointer), new Register(registerPointer), arrayAccess.dataType.byteSize);
            output.emitInstruction("add", new Register(registerPointer - 1), new Register(registerPointer - 1), new Register(registerPointer));
        }

        public void visit(WhileStatement whileStatement) {
            String label0 = "L" + labelCount++;
            String label1 = "L" + labelCount++;
            output.emitLabel(label0);
            label = label1;
            whileStatement.condition.accept(this);
            whileStatement.body.accept(this);
            output.emitInstruction("j", label0);
            output.emitLabel(label1);
        }

        public void visit(IfStatement ifStatement) {
            String label0 = "L" + labelCount++;
            label = label0;
            ifStatement.condition.accept(this);
            ifStatement.thenPart.accept(this);
            if (!(ifStatement.elsePart instanceof EmptyStatement)) {
                String label1 = "L" + labelCount++;
                output.emitInstruction("j", label1);
                output.emitLabel(label0);
                ifStatement.elsePart.accept(this);
                output.emitLabel(label1);
            } else output.emitLabel(label0);
        }

        public void visit(CallStatement callStatement) {

            ProcedureEntry procedureEntry = (ProcedureEntry) globalTable.lookup(callStatement.procedureName);
            for (int i = 0; i < callStatement.argumentList.size(); i++) {
                if (procedureEntry.parameterTypes.get(i).isReference) ((VariableExpression) callStatement.argumentList.get(i)).variable.accept(this);
                else callStatement.argumentList.get(i).accept(this);
                output.emitInstruction("stw", new Register(registerPointer - 1), sp, procedureEntry.parameterTypes.get(i).offset, "store arg #" + i);
                registerPointer--;
            }
            output.emitInstruction("jal", callStatement.procedureName.toString());
        }

        public void visit(CompoundStatement compoundStatement) {
            compoundStatement.statements.forEach(statement -> statement.accept(this));
        }

        public void visit(NamedVariable namedVariable) {
            VariableEntry variableEntry = (VariableEntry) symbolTable.lookup(namedVariable.name);
            output.emitInstruction("add", new Register(registerPointer), fp, variableEntry.offset);
            if(variableEntry.isReference) output.emitInstruction("ldw", new Register(registerPointer), new Register(registerPointer), 0);
            registerPointer++;
        }
    }

}

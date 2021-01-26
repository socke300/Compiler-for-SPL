package de.thm.mni.compilerbau.phases._05_varalloc;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.phases._04b_semant.ProcedureBodyChecker;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.PrimitiveType;
import de.thm.mni.compilerbau.utils.NotImplemented;
import de.thm.mni.compilerbau.utils.SplError;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is used to calculate the memory needed for variables and stack frames of the currently compiled SPL program.
 * Those value have to be stored in their corresponding fields in the {@link ProcedureEntry}, {@link VariableEntry} and
 * {@link ParameterType} classes.
 */
public class VarAllocator {
    public static final int REFERENCE_BYTESIZE = 4;

    private final boolean showVarAlloc;

    public VarAllocator(boolean showVarAlloc) {
        this.showVarAlloc = showVarAlloc;
    }

    public void allocVars(Program program, SymbolTable table) {
        //TODO (assignment 5): Allocate stack slots for all parameters and local variables
        program.accept(new VarAllocatorVisitor(table));
        program.accept(new OutgoingAreaVisitor(table));

        //TODO: Uncomment this when the above exception is removed!
        if (showVarAlloc) System.out.println(formatVars(program, table));
    }

    class VarAllocatorVisitor extends DoNothingVisitor {
        SymbolTable symbolTable;

        VarAllocatorVisitor(SymbolTable symbolTable) {
            this.symbolTable = symbolTable;
        }

        public void visit(Program program) {
            program.declarations.forEach(dec -> dec.accept(this));
        }

        public void visit(ProcedureDeclaration procedureDeclaration) {
            ProcedureEntry entry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
            SymbolTable localTable = entry.localTable;
            int offset = 0;
            for (int i = 0; i < procedureDeclaration.variables.size(); i++) {
                VariableEntry variableEntry = (VariableEntry) localTable.lookup(procedureDeclaration.variables.get(i).name);
                offset -= procedureDeclaration.variables.get(i).typeExpression.dataType.byteSize;
                variableEntry.offset = offset;
            }
            entry.localVarAreaSize = -1 * offset;
            offset = 0;
            for (int i = 0; i < entry.parameterTypes.size(); i++) {
                VariableEntry parameterEntry = (VariableEntry) localTable.lookup(procedureDeclaration.parameters.get(i).name);
                parameterEntry.offset = offset;
                entry.parameterTypes.get(i).offset = offset;
                if (procedureDeclaration.parameters.get(i).isReference)
                    offset += 4;
                else
                    offset += procedureDeclaration.parameters.get(i).typeExpression.dataType.byteSize;
            }
            entry.argumentAreaSize = offset;
        }
    }

    class OutgoingAreaVisitor extends DoNothingVisitor {
        SymbolTable symbolTable;
        int dataSize = -1;

        OutgoingAreaVisitor(SymbolTable symbolTable) {
            this.symbolTable = symbolTable;
        }

        public void visit(Program program) {
            program.declarations.forEach(dec -> dec.accept(this));
        }

        public void visit(ProcedureDeclaration procedureDeclaration) {
            ProcedureEntry entry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name);
            SymbolTable localTable = entry.localTable;
            OutgoingAreaVisitor vistor = new OutgoingAreaVisitor(localTable);
            procedureDeclaration.body.forEach(statement -> statement.accept(vistor));
            entry.outgoingAreaSize = vistor.dataSize;
        }

        public void visit(CallStatement callStatement) {
            ProcedureEntry entry = (ProcedureEntry) symbolTable.lookup(callStatement.procedureName);
            if (dataSize < entry.argumentAreaSize)
                dataSize = entry.argumentAreaSize;
        }

    }


    /**
     * Formats the variable allocation to a human readable format
     *
     * @param program The abstract syntax tree of the program
     * @param table   The symbol table containing all symbols of the spl program
     * @return A human readable string describing the allocated memory
     */
    private String formatVars(Program program, SymbolTable table) {
        return program.declarations.stream().filter(dec -> dec instanceof ProcedureDeclaration).map(dec -> (ProcedureDeclaration) dec).map(procDec -> {
            ProcedureEntry entry = (ProcedureEntry) table.lookup(procDec.name);

            return String.format("Variable allocation for procedure '%s'\n%s\nsize of argument area = %s\n%s%ssize of localvar area = %d\nsize of outgoing area = %d\n",
                    procDec.name,
                    IntStream.range(0, entry.parameterTypes.size()).mapToObj(i -> String.format("arg %d: sp + %d", i, entry.parameterTypes.get(i).offset)).collect(Collectors.joining()),
                    entry.argumentAreaSize,
                    procDec.parameters.stream().map(parDec -> String.format("param '%s': fp + %d\n", parDec.name, ((VariableEntry) entry.localTable.lookup(parDec.name)).offset)).collect(Collectors.joining()),
                    procDec.variables.stream().map(varDec -> String.format("var '%s': fp - %d\n", varDec.name, -((VariableEntry) entry.localTable.lookup(varDec.name)).offset)).collect(Collectors.joining()),
                    entry.localVarAreaSize,
                    entry.outgoingAreaSize
            );
        }).collect(Collectors.joining("\n"));
    }
}

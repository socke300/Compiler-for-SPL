package de.thm.mni.compilerbau.phases._04a_tablebuild;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.absyn.visitor.Visitor;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.types.Type;
import de.thm.mni.compilerbau.utils.NotImplemented;
import de.thm.mni.compilerbau.utils.SplError;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is used to create and populate a {@link SymbolTable} containing entries for every symbol in the currently
 * compiled SPL program.
 * Every declaration of the SPL program needs its corresponding entry in the {@link SymbolTable}.
 * <p>
 * Calculated {@link Type}s can be stored in and read from the dataType field of the {@link Expression},
 * {@link TypeExpression} or {@link Variable} classes.
 */
public class TableBuilder {
    private final boolean showTables;

    public TableBuilder(boolean showTables) {
        this.showTables = showTables;
    }

    public SymbolTable buildSymbolTable(Program program) {
        //TODO (assignment 4a): Initialize a symbol table with all predefined symbols and fill it with user-defined symbols
        SymbolTable symbolTable = TableInitializer.initializeGlobalTable();
        program.accept(new TableBuilderVisitor(symbolTable));
        return symbolTable;
    }

    class TableBuilderVisitor extends DoNothingVisitor {
        SymbolTable symbolTable;

        TableBuilderVisitor (SymbolTable symbolTable){
            this.symbolTable = symbolTable;
        }

        public void visit(TypeDeclaration typeDeclaration){
            typeDeclaration.typeExpression.accept(this);
            symbolTable.enter(new TypeEntry(typeDeclaration.name, typeDeclaration.typeExpression.dataType), SplError.RedeclarationAsType(typeDeclaration.position, typeDeclaration.name));
        }

        public void visit(NamedTypeExpression namedTypeExpression){
            Entry entry = symbolTable.lookup(namedTypeExpression.name, SplError.UndefinedType(namedTypeExpression.position,namedTypeExpression.name));
            if (!(entry instanceof TypeEntry)) throw SplError.NotAType(namedTypeExpression.position, namedTypeExpression.name);
            namedTypeExpression.dataType = ((TypeEntry) entry).type;
        }

        public void visit (ProcedureDeclaration procedureDeclaration){
            SymbolTable localTable = new SymbolTable(symbolTable);
            TableBuilderVisitor vistor = new TableBuilderVisitor(localTable);
            List<ParameterType> parameterTypeList = new ArrayList<>();
            procedureDeclaration.parameters.forEach(parameterDeclaration ->{
                parameterDeclaration.accept(vistor);
                parameterTypeList.add(new ParameterType(parameterDeclaration.typeExpression.dataType, parameterDeclaration.isReference));
            });
            procedureDeclaration.variables.forEach(variableDeclaration -> variableDeclaration.accept(vistor));
            symbolTable.enter(new ProcedureEntry(procedureDeclaration.name, localTable, parameterTypeList), SplError.RedeclarationAsProcedure(procedureDeclaration.position, procedureDeclaration.name));
            if (showTables) {
                System.out.println("Symbol table end of Procedure " + procedureDeclaration.name);
                System.out.println(localTable.toString());
            }
        }

        public void visit (ParameterDeclaration parameterDeclaration) {
            parameterDeclaration.typeExpression.accept(this);
            if (parameterDeclaration.typeExpression.dataType instanceof ArrayType && !parameterDeclaration.isReference)
                throw SplError.MustBeAReferenceParameter(parameterDeclaration.position, parameterDeclaration.name);
            symbolTable.enter(new VariableEntry(parameterDeclaration.name, parameterDeclaration.typeExpression.dataType, parameterDeclaration.isReference), SplError.RedeclarationAsParameter(parameterDeclaration.position, parameterDeclaration.name));
        }

        public void visit(Program program) {
            program.declarations.forEach(dec -> dec.accept(this));
        }

        public void visit(VariableDeclaration variableDeclaration) {
            variableDeclaration.typeExpression.accept(this);
            symbolTable.enter(new VariableEntry(variableDeclaration.name, variableDeclaration.typeExpression.dataType, false), SplError.RedeclarationAsVariable(variableDeclaration.position, variableDeclaration.name));
        }

        public void visit(ArrayTypeExpression arrayTypeExpression) {
            arrayTypeExpression.baseType.accept(this);
            arrayTypeExpression.dataType = new ArrayType(arrayTypeExpression.baseType.dataType, arrayTypeExpression.arraySize);
        }
    }

}

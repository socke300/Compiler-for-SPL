package de.thm.mni.compilerbau.phases._04b_semant;

import de.thm.mni.compilerbau.absyn.*;
import de.thm.mni.compilerbau.absyn.visitor.DoNothingVisitor;
import de.thm.mni.compilerbau.phases._04a_tablebuild.TableBuilder;
import de.thm.mni.compilerbau.table.*;
import de.thm.mni.compilerbau.types.ArrayType;
import de.thm.mni.compilerbau.types.PrimitiveType;
import de.thm.mni.compilerbau.types.Type;
import de.thm.mni.compilerbau.utils.NotImplemented;
import de.thm.mni.compilerbau.utils.SplError;

/**
 * This class is used to check if the currently compiled SPL program is semantically valid.
 * Every statement and expression has to be checked, to ensure that every type is correct.
 * <p>
 * Calculated {@link Type}s can be stored in and read from the dataType field of the {@link Expression},
 * {@link TypeExpression} or {@link Variable} classes.
 */
public class ProcedureBodyChecker {
    public void procedureCheck(Program program, SymbolTable globalTable) {
        program.accept(new ProcedureBodyVisitor(globalTable));
    }


    protected void checkType(Type expected, Type actual, SplError error) throws SplError {
        // This method may be used to check types. It must be implemented before it can be used.
        // TODO: The implementation should compare the types and throw the given error if the types are not equal.
        throw new NotImplemented();
    }

    class ProcedureBodyVisitor extends DoNothingVisitor {
        SymbolTable symbolTable;

        ProcedureBodyVisitor(SymbolTable symbolTable) {
            this.symbolTable = symbolTable;
        }

        public void visit(Program program) {
            Entry mainEntry = symbolTable.lookup(new Identifier("main"), SplError.MainIsMissing());
            program.declarations.forEach(dec -> dec.accept(this));
            if (!(mainEntry instanceof ProcedureEntry)) throw SplError.MainIsNotAProcedure();
            ProcedureEntry procMainEntry = (ProcedureEntry) mainEntry;
            if (procMainEntry.parameterTypes.size() != 0) throw  SplError.MainMustNotHaveParameters();
        }

        public void visit(ProcedureDeclaration procedureDeclaration) {
            ProcedureEntry entry = (ProcedureEntry) symbolTable.lookup(procedureDeclaration.name, SplError.UndefinedProcedure(procedureDeclaration.position, procedureDeclaration.name));
            SymbolTable localTable = entry.localTable;
            ProcedureBodyVisitor vistor = new ProcedureBodyVisitor(localTable);
            procedureDeclaration.body.forEach(statement -> statement.accept(vistor));
        }

        public void visit(IntLiteral intLiteral) {
            intLiteral.dataType = PrimitiveType.intType;
        }

        public void visit(IfStatement ifStatement) {
            ifStatement.condition.accept(this);
            ifStatement.thenPart.accept(this);
            ifStatement.elsePart.accept(this);
            if (ifStatement.condition.dataType != PrimitiveType.boolType) throw SplError.IfConditionMustBeBoolean(ifStatement.position);
        }

        public void visit(CallStatement callStatement) {
            Entry entry = symbolTable.lookup(callStatement.procedureName, SplError.UndefinedProcedure(callStatement.position, callStatement.procedureName));
            if (!(entry instanceof ProcedureEntry)) throw SplError.CallOfNonProcedure(callStatement.position, callStatement.procedureName);
            ProcedureEntry procEntry = (ProcedureEntry) entry;
            if (callStatement.argumentList.size() > procEntry.parameterTypes.size())
                throw SplError.TooManyArguments(callStatement.position, callStatement.procedureName);
            if (callStatement.argumentList.size() < procEntry.parameterTypes.size())
                throw SplError.TooFewArguments(callStatement.position, callStatement.procedureName);
            for (int i = 0; i < callStatement.argumentList.size(); i++) {
                callStatement.argumentList.get(i).accept(this);
                if (callStatement.argumentList.get(i).dataType != procEntry.parameterTypes.get(i).type)
                    throw SplError.ArgumentTypeMismatch(callStatement.position, callStatement.procedureName, i);
                if (procEntry.parameterTypes.get(i).isReference && !(callStatement.argumentList.get(i) instanceof VariableExpression))
                    throw SplError.ArgumentMustBeAVariable(callStatement.position, callStatement.procedureName, i);
            }
        }

        public void visit(WhileStatement whileStatement) {
            whileStatement.condition.accept(this);
            whileStatement.body.accept(this);
            if (whileStatement.condition.dataType != PrimitiveType.boolType)
                throw SplError.WhileConditionMustBeBoolean(whileStatement.position);
        }

        public void visit(CompoundStatement compoundStatement) {
            compoundStatement.statements.forEach(statement -> statement.accept(this));
        }

        public void visit(AssignStatement assignStatement) {
            assignStatement.value.accept(this);
            assignStatement.target.accept(this);
            if (assignStatement.target.dataType != assignStatement.value.dataType)
                throw SplError.AssignmentHasDifferentTypes(assignStatement.position);
            if (assignStatement.target.dataType != PrimitiveType.intType)
                throw SplError.AssignmentRequiresIntegers(assignStatement.position);
        }

        public void visit(ArrayAccess arrayAccess) {
            arrayAccess.array.accept(this);
            arrayAccess.index.accept(this);
            if (!(arrayAccess.array.dataType instanceof ArrayType))
                throw SplError.IndexingNonArray(arrayAccess.position);
            if (arrayAccess.index.dataType != PrimitiveType.intType)
                throw SplError.IndexingWithNonInteger(arrayAccess.position);
            arrayAccess.dataType = ((ArrayType) arrayAccess.array.dataType).baseType;
        }

        public void visit(BinaryExpression binaryExpression) {
            binaryExpression.leftOperand.accept(this);
            binaryExpression.rightOperand.accept(this);
            if (binaryExpression.leftOperand.dataType != binaryExpression.rightOperand.dataType)
                throw SplError.OperatorDifferentTypes(binaryExpression.position);
            if (binaryExpression.operator.isArithmetic()) {
                if (binaryExpression.leftOperand.dataType != PrimitiveType.intType)
                    throw SplError.ArithmeticOperatorNonInteger(binaryExpression.position);
                binaryExpression.dataType = PrimitiveType.intType;
            } else {
                if (binaryExpression.leftOperand.dataType != PrimitiveType.intType)
                    throw SplError.ComparisonNonInteger(binaryExpression.position);
                binaryExpression.dataType = PrimitiveType.boolType;
            }
        }

        public void visit(VariableExpression variableExpression) {
            variableExpression.variable.accept(this);
            variableExpression.dataType = variableExpression.variable.dataType;
        }

        public void visit(NamedVariable namedVariable) {
            Entry entry = symbolTable.lookup(namedVariable.name, SplError.UndefinedVariable(namedVariable.position, namedVariable.name));
            if (!(entry instanceof VariableEntry))
                throw SplError.NotAVariable(namedVariable.position, namedVariable.name);
            namedVariable.dataType = ((VariableEntry) entry).type;
        }
    }
}

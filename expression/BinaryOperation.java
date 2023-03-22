package expression;

import java.math.BigDecimal;
import java.util.Objects;

abstract class BinaryOperation implements MyExpression, Expression, TripleExpression, MyExpressionBinary {

    private final MyExpression exp1;
    private final MyExpression exp2;
    private String toString;
    private String toMiniString;

    public BinaryOperation (MyExpression exp1, MyExpression exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
    }

    @Override
    public BigDecimal evaluate(BigDecimal a) {
        return getValue(exp1.evaluate(a) , exp2.evaluate(a));
    }

    @Override
    public int evaluate(int x) {
        return getValue(exp1.evaluate(x) , exp2.evaluate(x));
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return getValue(exp1.evaluate(x, y, z) , exp2.evaluate(x, y, z));
    }

    @Override
    public String toString() {
        if (toString == null) {
            String str1 = exp1.toString();
            String str2 = exp2.toString();
            toString = "(" + getString(str1, str2) + ")";
        }
        return toString;
    }

    @Override
    public String toMiniString() {
        if (toMiniString == null) {
            String str1 = exp1.toMiniString();
            if (exp1.getPriority() > getPriority()) {
                str1 = "(" + str1 + ")";
            } else if (!getLeftAssociative() && exp1.getPriority() == getPriority()) {
                str1 = "(" + str1 + ")";
            }
            String str2 = exp2.toMiniString();
            if (exp2.getPriority() > getPriority()) {
                str2 = "(" + str2 + ")";
            } else if (!getRightAssociative() && exp2.getPriority() == getPriority()) {
                str2 = "(" + str2 + ")";
            } else if (getType() == 2 && exp2.getType() == 6 || getType() > 7 && exp2.getType() > 7 && getType() < 11 && exp2.getType() < 11) {
                str2 = "(" + str2 + ")";
            }
            toMiniString = getString(str1, str2);
        }

        return toMiniString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof BinaryOperation) {
            BinaryOperation that = (BinaryOperation) o;
            if (getType() == that.getType()) {
                return Objects.equals(exp1, that.exp1) && Objects.equals(exp2, that.exp2);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exp1, exp2, getType());
    }
}
package expression;

import java.math.BigDecimal;

public class ArithmeticShifts extends BinaryOperation{

    public ArithmeticShifts(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
    }

    @Override
    public BigDecimal evaluate(BigDecimal x) {
        return null;
    }

    @Override
    public float getPriority() {
        return (float) 100;
    }

    @Override
    public int getType() {
        return 10;
    }

    @Override
    public int getValue(int a, int b) {
        return a >>> b;
    }

    @Override
    public BigDecimal getValue(BigDecimal a, BigDecimal b) {
        return null;
    }

    @Override
    public String getString(String a, String b) {
        return a + " >>> " + b;
    }

    @Override
    public boolean getLeftAssociative() {
        return true;
    }

    @Override
    public boolean getRightAssociative() {
        return true;
    }
}

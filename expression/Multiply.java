package expression;

import java.math.BigDecimal;

public class Multiply extends BinaryOperation {

    public Multiply(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
    }

    public int getValue(int a, int b) {
        return a * b;
    }

    public BigDecimal getValue(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }

    @Override
    public String getString(String a, String b){
        return a + " * " + b;
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public boolean getLeftAssociative() {
        return true;
    }

    @Override
    public boolean getRightAssociative() {
        return true;
    }

    @Override
    public float getPriority() {
        return 1;
    }
}


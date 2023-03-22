package expression;

import java.math.BigDecimal;

public class Divide extends BinaryOperation {

    public Divide(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
    }

    @Override
    public int getValue(int a, int b) {
        return a / b;
    }

    public BigDecimal getValue(BigDecimal a, BigDecimal b) {
        return a.divide(b);
    }

    @Override
    public String getString(String a, String b){
        return a + " / " + b;
    }

    @Override
    public float getPriority() {
        return 1;
    }

    @Override
    public int getType() {
        return 6;
    }

    @Override
    public boolean getLeftAssociative() {
        return true;
    }

    @Override
    public boolean getRightAssociative() {
        return false;
    }
}

package expression;

import java.math.BigDecimal;

public class Add extends BinaryOperation {


    public Add(MyExpression exp1, MyExpression exp2) {
        super(exp1, exp2);
    }

    public BigDecimal getValue(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    public int getValue(int a, int b) {
        return a + b;
    }

    @Override
    public String getString(String a, String b){
        return a + " + " + b;
    }

    @Override
    public float getPriority() {
        return 2;
    }

    @Override
    public int getType() {
        return 1;
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

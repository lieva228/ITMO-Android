package expression;

import java.math.BigDecimal;

public class Minus extends UnoOperation{

    private MyExpression exp;

    public Minus(MyExpression exp) {
        super(exp);
    }

    @Override
    public BigDecimal evaluate(BigDecimal x) {
        return null;
    }

    @Override
    public int evaluate(int x) {
        return 0;
    }

    @Override
    public float getPriority() {
        return (float) 0.1;
    }

    @Override
    public int getType() {
        return 7;
    }

    @Override
    public String getString(String a) {
        return "- " + a;
    }

    @Override
    public String getStringBracket(String a) {
        return "-(" + a + ")";
    }

    @Override
    public int getValue(int a) {
        return -a;
    }
}

package expression;

import java.math.BigDecimal;

public class T0 extends UnoOperation implements MyUnoExpression{

    public T0(MyExpression exp) {
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
        return 11;
    }

    public String getString(String a) {
        return "t0 " + a;
    }

    public String getStringBracket(String a) {
        return "t0(" + a + ")";
    }

    @Override
    public int getValue(int a) {
        return Integer.numberOfTrailingZeros(a);
    }

}

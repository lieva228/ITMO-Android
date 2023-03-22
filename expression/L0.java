package expression;

import java.math.BigDecimal;

public class L0 extends UnoOperation {

    public L0(MyExpression exp) {
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
        return 12;
    }

    @Override
    public String getString(String a) {
        return "l0 " + a;
    }

    public String getStringBracket(String a) {
        return "l0(" + a + ")";
    }

    @Override
    public int getValue(int a) {
        return Integer.numberOfLeadingZeros(a);
    }

}

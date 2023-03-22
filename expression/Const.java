package expression;

import java.math.BigDecimal;
import java.util.Objects;

public class Const implements MyExpression, Expression, TripleExpression {

    private Number constant;

    public Const(int a) {
        this.constant = a;
    }

    public Const(BigDecimal a) {
        this.constant = a;
    }

    public int evaluate(int a) {
        return constant.intValue();
    }

    @Override
    public BigDecimal evaluate(BigDecimal a) {
        return (BigDecimal) constant;
    }

    @Override
    public int evaluate(int a, int b, int c) {
        return constant.intValue();
    }

    @Override
    public String toMiniString() {
        return constant.toString();
    }

    @Override
    public String toString() {
        return constant.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Const) {
            Const that = (Const) o;
            return constant.equals(that.constant);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(constant);
    }

    @Override
    public float getPriority() {
        return 0;
    }

    @Override
    public int getType() {
        return 4;
    }
}

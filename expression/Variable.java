package expression;

import java.math.BigDecimal;
import java.util.Objects;

public class Variable implements MyExpression, Expression, TripleExpression {

    public String var;

    public Variable(String var) {
        this.var = var;
    }
    @Override
    public int evaluate(int var) {
        return var;
    }

    @Override
    public String toMiniString() {
        return var;
    }

    @Override
    public String toString() {
        return var;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Variable) {
            Variable that = (Variable) o;
            return var == that.var;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(var);
    }

    @Override
    public int evaluate(int x, int y, int z) {
        switch (var) {
            case "x" :
                return x;
            case "y" :
                return y;
            case "z" :
                return z;
            default:
                throw new IllegalStateException("Illegal variable : " + var);
        }
    }

    @Override
    public BigDecimal evaluate(BigDecimal a) {
        return a;
    }

    @Override
    public float getPriority() {
        return 0;
    }

    @Override
    public int getType() {
        return 5;
    }
}


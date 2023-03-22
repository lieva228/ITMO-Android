package expression;

public abstract class UnoOperation  implements Expression, TripleExpression, MyExpression, MyUnoExpression {

    private MyExpression exp;

    public UnoOperation(MyExpression exp) {
        this.exp = exp;
    }

    @Override
    public String toMiniString() {
        if (getType() == 7 && exp.getType() == 4 || getType() == 7 && exp.getType() == 5) {
            return getString(exp.toMiniString());
        }
        if (exp.getPriority() > getPriority()) {
            return getStringBracket(exp.toMiniString());
        }
        return getString(exp.toMiniString());
    }

    @Override
    public String toString() {
        return getStringBracket(exp.toString());
    }

    @Override
    public int evaluate(int x, int y, int z) {
        return getValue(exp.evaluate(x, y, z));
    }
}

package expression;


public interface MyUnoExpression extends Expression, MyExpression, TripleExpression {
    String getString(String a);

    String getStringBracket(String a);

    int getValue(int a);
}

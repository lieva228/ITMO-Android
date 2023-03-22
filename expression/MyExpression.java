package expression;


public interface MyExpression extends Expression, TripleExpression, BigDecimalExpression{
    float getPriority();

    int getType();

}

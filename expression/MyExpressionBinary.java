package expression;


import java.math.BigDecimal;

public interface MyExpressionBinary extends Expression, MyExpression, BigDecimalExpression {
    int getValue(int a, int b);

    BigDecimal getValue(BigDecimal a, BigDecimal b);

    String getString(String a, String b);

    boolean getLeftAssociative();

    boolean getRightAssociative();
}

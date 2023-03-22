package expression.parser;

import expression.*;


public final class ExpressionParser implements Parser {
    public TripleExpression parse(final String source) {
        return parse(new StringSource(source));
    }

    public TripleExpression parse(final CharSource source) {
        return new ExpPars(source).parseExpression();
    }

    private static class ExpPars extends BaseParser {

        private MyExpression result;

        public ExpPars(final CharSource source) {
            super(source);
        }

        private MyExpression parseOperation() {
            if (take('>')) {
                expect('>');
                if (take('>')) {
                    skipWhitespace();
                    result = new ArithmeticShifts(result, parseShifts());
                    skipWhitespace();
                } else {
                    skipWhitespace();
                    result = new ShiftRight(result, parseShifts());
                    skipWhitespace();
                }
            } else if (take('<')) {
                expect('<');
                skipWhitespace();
                result = new ShiftLeft(result, parseShifts());
                skipWhitespace();
            } else if (take('+')) {
                skipWhitespace();
                result = new Add(result, parseTerm());
                skipWhitespace();
            } else if (take('-')) {
                skipWhitespace();
                result = new Subtract(result, parseTerm());
                skipWhitespace();
            } else {
                throw error("Unexpected Symbol");
            }
            return result;
        }

        public MyExpression parseExpression() {
            result = parseTerm();
            while (!eof()) {
               skipWhitespace();
               result = parseOperation();
           }
           return result;
        }

        private MyExpression parseMinusNumber() {
            final StringBuilder sb = new StringBuilder("-");
            takeInteger(sb);

            try {
                return new Const(Integer.parseInt(sb.toString()));
            } catch (final NumberFormatException e) {
                throw error("Invalid number " + sb);
            }
        }

        private MyExpression parseNumber() {
            final StringBuilder sb = new StringBuilder();
            takeInteger(sb);

            try {
                return new Const(Integer.parseInt(sb.toString()));
            } catch (final NumberFormatException e) {
                throw error("Invalid number " + sb);
            }
        }

        private void takeDigits(final StringBuilder sb) {
            while (between('0', '9')) {
                sb.append(take());
            }
        }

        private void takeInteger(final StringBuilder sb) {
            if (take('-')) {
                sb.append('-');
            }
            if (take('0')) {
                sb.append('0');
            } else if (between('1', '9')) {
                takeDigits(sb);
            } else {
                System.out.println(take());
                throw error("Invalid number");
            }
        }

        private MyExpression parseTerm() {
            skipWhitespace();
            result = parseVCMB();
            skipWhitespace();
            while (stillTerm()) {
                if (take('*')) {
                    skipWhitespace();
                    result = new Multiply(result, parseVCMB());
                    skipWhitespace();
                } else if (take('/')) {
                    skipWhitespace();
                    result = new Divide(result, parseVCMB());
                    skipWhitespace();
                } else {
                    System.out.println(take());
                    throw error("Unexpected symbol");
                }
            }
            return result;
        }

        private MyExpression parseVCMB() {
            skipWhitespace();
            if (take('t')) {
                expect('0');
                return new T0(parseVCMB());
            } else if (take('l')) {
                expect('0');
                return new L0(parseVCMB());
            } else if (take('-')) {
                if (between('0', '9')) {
                    return parseMinusNumber();
                } else {
                    skipWhitespace();
                    return new Minus(parseVCMB());
                }
            } else if (take('(')) {
                return parseBrackets();
            } else if (eof()) {
                return result;
            } else if(between('x', 'z')) {
                return parseValue(take());
            } else {
                return parseNumber();
            }
        }

        private MyExpression parseValue(char c) {
            switch (c) {
                case 'x' :
                    return new Variable("x");
                case 'y' :
                    return new Variable("y");
                case 'z' :
                    return new Variable("z");
                default:
                    throw error("Wrong variable");
            }
        }

        private MyExpression parseBrackets() {
            result = parseTerm();
            while (!take(')')) {
                result = parseOperation();
            }
            return result;
        }

        private MyExpression parseShifts() {
            result = parseTerm();
            skipWhitespace();
            while (!eof() && !test(')') && !test('>') && !test('<')) {
                skipWhitespace();
                if (take('+')) {
                    skipWhitespace();
                    result = new Add(result, parseTerm());
                    skipWhitespace();
                } else if (take('-')) {
                    skipWhitespace();
                    result = new Subtract(result, parseTerm());
                    skipWhitespace();
                } else {
                    throw error("Unexpected Symbol");
                }
            }
            return result;
        }

        private boolean stillTerm() {
            return !eof() && !test('+') && !test('-') && !test(')') && !test('<') && !test('>');
        }

        private void skipWhitespace() {
            while (testWhiteSpace()) {
                // skip
            }
        }
    }
}
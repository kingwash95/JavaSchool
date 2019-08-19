package com.tsystems.javaschool.tasks.calculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Calculator {

    public static String evaluate(String expression) {
        try {
            List<Token> tokens = parseTokens(expression);
            if (!areValid(tokens)) {
                return null;
            }
            return fmt(calcIteratively(tokens).get(0).getNumberValue());
        } catch (NumberFormatException nfe) {
            System.out.println("Wrong number format");
            return null;
        } catch (DivisionByZeroRuntimeException dbze) {
            System.out.println("Division by zero");
            return null;
        } catch (WrongExpressionFormatException wefe) {
            System.out.println("Wrong format");
            return null;
        } catch (Exception e) {
            System.out.println("Who knows...");
            return null;
        }

    }
    static String fmt(Double value) {
        if (value == null) {
            return null;
        }
        double d = value;
        return d == (long) d ? String.format("%d", (long) d) : String.format("%s", d);
    }

    private static boolean areValid(List<Token> tokens) {

        if (tokens.get(0).isOperator() || tokens.get(tokens.size() - 1).isOperator()) {
            throw new WrongExpressionFormatException();
        }

        // check brackets
        int counter = 0;
        Token prev = null;
        for (int idx = 0; idx < tokens.size(); idx++) {
            if (prev != null) { // skip the 1st iteration
                if (prev.isOperator() && tokens.get(idx).isOperator() || prev.getType() == TokenType.NUMBER && tokens.get(idx).getType() == TokenType.NUMBER) {
                    throw new WrongExpressionFormatException();
                }
            }
            if (tokens.get(idx).getType() == TokenType.LEFT_BRACKET) {
                counter++;
            } else if (tokens.get(idx).getType() == TokenType.RIGHT_BRACKET) {
                counter--;
            }
            if (counter < 0) {
                throw new WrongExpressionFormatException();
            }
            prev = tokens.get(idx);
        }

        if (counter != 0) {
            throw new WrongExpressionFormatException();
        }

        return true;
    }

    private static List<Token> parseTokens(String expression) {
        String[] strTokens = format(expression).split(" ");
        List<Token> tokens = Arrays.stream(strTokens).filter(e -> !e.equals("")).map(e -> Token.valueOf(e)).collect(Collectors.toList());
        return tokens;
    }

    private static String format(String expression) {
        return expression.replace("(", " ( ").replace(")", " ) ")
                .replace("+", " + ").replace("-", " - ")
                .replace("*", " * ").replace("/", " / ");
    }

    // TODO: expression must be validated before
    private static List<Token> calcIteratively(List<Token> tokens) {
        System.out.println("Calculating " + tokens);

        // we must calculate inside brackets. let's do it iteratively and recursively
        while (tokens.stream().anyMatch(e -> e.getType() == TokenType.LEFT_BRACKET)) {
            // look for the most nested brackets:
            List<Token> bracketsContent = new ArrayList<>();
            boolean including = false;
            int startIdx = -1;
            int endIdx = -1;
            for (int idx = 0; idx < tokens.size(); idx++) {
                Token token = tokens.get(idx);
                //System.out.println("Processing token " + token);
                if (!including && token.getType() == TokenType.LEFT_BRACKET) {
                    including = true;
                    startIdx = idx;
                } else if (including && token.getType() == TokenType.LEFT_BRACKET) {
                    bracketsContent.clear();
                    startIdx = idx;
                } else if (including && token.getType() == TokenType.RIGHT_BRACKET) {
                    // here we found the most nested brackets, let's calculate its value:
                    including = false;
                    endIdx = idx;
                    System.out.println("Brackets content: " + bracketsContent);
                    List<Token> abbreviated = calcIteratively(bracketsContent); // call the same func but only for the brackets content
                    System.out.println("Abbreviated: " + abbreviated);

                    // remove brackets and its content from the original expression...:
                    final int removeIdx = startIdx;
                    IntStream.range(startIdx, endIdx + 1).forEach(e -> tokens.remove(removeIdx));

                    // ...and add its value instead:
                    tokens.addAll(startIdx, abbreviated);
                    System.out.println("After abbreviation: " + tokens);
                } else if (including) {
                    bracketsContent.add(token);
                }
            }
        }

        // we got an expression without brackets here:
        while (tokens.size() > 2) {
            // check for an operation with a higher priority (* or /)
            int leftOperandIdx = 0;
            for (int idx = 0; idx < tokens.size(); idx++) {
                if (tokens.get(idx).isPrio()) {
                    leftOperandIdx = idx - 1;
                    break;
                }
            }
            // process a basic operation with two operands:
            Double left = tokens.get(leftOperandIdx).getNumberValue();
            Double right = tokens.get(leftOperandIdx + 2).getNumberValue();
            Double res = null;
            switch (tokens.get(leftOperandIdx + 1).getType()) {
                case ADDITION:
                    res = left + right;
                    break;
                case SUBSTRACTION:
                    res = left - right;
                    break;
                case DIVISION:
                    if (right == 0) {
                        throw new DivisionByZeroRuntimeException();
                    }
                    res = left / right;
                    break;
                case MULTIPLICATION:
                    res = left * right;
                    break;
            }
            // remove the operands and the operator:
            tokens.remove(tokens.get(leftOperandIdx + 2));
            tokens.remove(tokens.get(leftOperandIdx + 1));
            tokens.remove(tokens.get(leftOperandIdx));
            tokens.add(leftOperandIdx, new Token(TokenType.NUMBER, res));
            System.out.println("Intermediate result: " + tokens);
        }

        return tokens;
    }

}

class Token {
    private TokenType type;
    private Double numberValue;

    public Token(TokenType type) {
        this.type = type;
        this.numberValue = null;
    }

    public Token(TokenType type, Double numberValue) {
        this.type = type;
        this.numberValue = numberValue;
    }

    public Token(TokenType type, String numberValue) {
        this.type = type;
        this.numberValue = Double.valueOf(numberValue);
    }

    public static Token valueOf(String str) {
        System.out.println("Parsing token '" + str + "'");
        switch (str) {
            case "(":
                return new Token(TokenType.LEFT_BRACKET);
            case ")":
                return new Token(TokenType.RIGHT_BRACKET);
            case "+":
                return new Token(TokenType.ADDITION);
            case "-":
                return new Token(TokenType.SUBSTRACTION);
            case "/":
                return new Token(TokenType.DIVISION);
            case "*":
                return new Token(TokenType.MULTIPLICATION);
            default:
                return new Token(TokenType.NUMBER, str);
        }
    }

    public boolean isPrio() {
        return type == TokenType.MULTIPLICATION || type == TokenType.DIVISION;
    }

    boolean isBracket() {
        return type == TokenType.LEFT_BRACKET || type == TokenType.RIGHT_BRACKET;
    }

    boolean isOperator() {
        return type == TokenType.ADDITION || type == TokenType.SUBSTRACTION || type == TokenType.DIVISION ||
                type == TokenType.MULTIPLICATION;
    }

    @Override
    public String toString() {
        if (type == TokenType.NUMBER) {
            return numberValue.toString();
        } else {
            return type.toString();
        }
    }

    public Double getNumberValue() {
        return numberValue;
    }

    public TokenType getType() {
        return type;
    }
}

enum TokenType {

    LEFT_BRACKET,
    RIGHT_BRACKET,
    NUMBER,
    SUBSTRACTION,
    ADDITION,
    MULTIPLICATION,
    DIVISION

}

class DivisionByZeroRuntimeException extends RuntimeException {
    public DivisionByZeroRuntimeException() {
        super();
    }
}

class WrongExpressionFormatException extends RuntimeException {
    public WrongExpressionFormatException() {
        super();
    }
}
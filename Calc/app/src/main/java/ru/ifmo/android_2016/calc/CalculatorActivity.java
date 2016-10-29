package ru.ifmo.android_2016.calc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import java.util.Stack;

public final class CalculatorActivity extends Activity {

    private HorizontalScrollView expressionScrollview, resultScrollview;
    private TextView expression, result;

    private boolean typingNumber = true;
    private boolean containPoint = false;
    private String expressionString = "", resultNumberStr = "0.0";
    private int lengthCurrentNumber = 0;
    private char lastSymbol = '$';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        expression = (TextView) findViewById(R.id.expression);
        result = (TextView) findViewById(R.id.result);

        expressionScrollview = (HorizontalScrollView) findViewById(R.id.expression_scrollview);
        resultScrollview = (HorizontalScrollView) findViewById(R.id.result_scrollview);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("expressionString", expressionString);
        savedInstanceState.putString("resultNumberStr", resultNumberStr);
        savedInstanceState.putBoolean("typingNumber", typingNumber);
        savedInstanceState.putBoolean("containPoint", containPoint);
        savedInstanceState.putInt("lengthCurrentNumber", lengthCurrentNumber);
        savedInstanceState.putChar("lastSymbol", lastSymbol);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        expressionString = savedInstanceState.getString("expressionString");
        resultNumberStr = savedInstanceState.getString("resultNumberStr");
        typingNumber = savedInstanceState.getBoolean("typingNumber");
        containPoint = savedInstanceState.getBoolean("containPoint");
        lengthCurrentNumber = savedInstanceState.getInt("lengthCurrentNumber");
        lastSymbol = savedInstanceState.getChar("lastSymbol");
        updateState();
    }

    public void onClick(View v) {
        char c = ((Button) v).getText().charAt(0);
        if (v.getId() == R.id.eqv && !expressionString.isEmpty()) {
            if (isOperation(lastSymbol)) {
                expressionString = expressionString.substring(0, expressionString.length() - 3);
            }
            try {
                resultNumberStr = calculateExpression(expressionString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            expressionString = "";
            lastSymbol = '$';
            lengthCurrentNumber = 0;
            containPoint = false;
            typingNumber = false;
        }
        if (v.getId() == R.id.clear && !expressionString.isEmpty()) {
            int k = 1;
            lastSymbol = expressionString.charAt(expressionString.length() - 1);
            if (lastSymbol == ' ') {
                k = 3;
                typingNumber = true;
            }
            if (typingNumber && lengthCurrentNumber == 1) {
                typingNumber = false;
            }
            expressionString = expressionString.substring(0, expressionString.length() - k);
            lengthCurrentNumber--;
            if (lastSymbol == '.') {
                containPoint = false;
                typingNumber = true;
                lastSymbol = '0';
            }
        }
        if (expressionString.isEmpty() && !resultNumberStr.isEmpty() && isOperation(c)) {
            if (!resultNumberStr.equals("NaN") && !resultNumberStr.equals("Infinity")) {
                expressionString += resultNumberStr;
                typingNumber = true;
                lastSymbol = expressionString.charAt(expressionString.length() - 1);
            }
        }
        if (typingNumber && isOperation(c)) {
            if (lastSymbol == '.') {
                return;
            }
            if (isOperation(lastSymbol)) {
                expressionString = expressionString.substring(0, expressionString.length() - 3);
            }
            typingNumber = false;
            containPoint = false;
            lastSymbol = c;
            lengthCurrentNumber = 0;
            expressionString += (" " + c + " ");
        }

        if (typingNumber && lengthCurrentNumber >= 16) {
            return;
        }
        if (v.getId() == R.id.point) {
            if (containPoint) {
                return;
            } else {
                if (lengthCurrentNumber == 0) {
                    expressionString += "0.";
                    lengthCurrentNumber += 2;
                    lastSymbol = '.';
                } else {
                    expressionString += c;
                    lengthCurrentNumber++;
                    lastSymbol = c;
                }
                containPoint = true;
            }
        }
        if (lastSymbol == '0' && (lengthCurrentNumber == 1 || lengthCurrentNumber < 0)) {
            return;
        }
        if (Character.isDigit(c)) {
            expressionString += c;
            lastSymbol = c;
            lengthCurrentNumber++;
            if (!typingNumber) {
                typingNumber = true;
            }
        }
        updateState();
    }

    private void updateState() {
        expression.setText(expressionString);
        result.setText(resultNumberStr);
        expressionScrollview.post(new Runnable() {
            @Override
            public void run() {
                expressionScrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
        resultScrollview.post(new Runnable() {
            @Override
            public void run() {
                resultScrollview.fullScroll(HorizontalScrollView.FOCUS_LEFT);
            }
        });
    }

    private static boolean isOperation(char a) {
        return ((a == '+') || (a == '−') || (a == '×') || (a == '÷'));
    }

    private static boolean checkPriority(char a, char b) {
        return ((a == '+' || a == '−') && (b == '+' || b == '−'));
    }

    private static double calculation(char operationID, double right, double left) {
        switch (operationID) {
            case '+':return (left + right);
            case '−':return (left - right);
            case '×':return (left * right);
            case '÷':return (left / right);
            default: return 0.0;
        }
    }

    public static String calculateExpression(String expression) {
        Stack<Double> digitStack = new Stack<>();
        Stack<Character> operationStack = new Stack<>();

        int i = 0;
        while (i < expression.length()) {
            StringBuilder currentNumber = new StringBuilder();
            while (i < expression.length() && !isOperation(expression.charAt(i))) {
                currentNumber.append(expression.charAt(i));
                i++;
            }
            digitStack.add(Double.valueOf(String.valueOf(currentNumber)));
            if (i == expression.length()) {
                break;
            }
            Character newOperation = expression.charAt(i);

            while (operationStack.size() > 0) {
                if (checkPriority(newOperation, operationStack.peek())) {
                    digitStack.add(calculation(operationStack.pop(), digitStack.pop(), digitStack.pop()));
                } else break;
            }
            operationStack.add(newOperation);
            i++;
        }

        while (operationStack.size() > 0) {
            digitStack.add(calculation(operationStack.pop(), digitStack.pop(), digitStack.pop()));
        }
        return digitStack.peek().toString();
    }
}

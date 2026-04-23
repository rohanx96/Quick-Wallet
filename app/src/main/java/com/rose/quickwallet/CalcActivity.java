package com.rose.quickwallet;

/**
 * Created by rose on 21/9/15.
 */

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.TextView;
        import android.widget.Button;

public class CalcActivity extends Activity {

private TextView txtResult; // Reference to EditText of result
private float result = 0;     // Result of computation
private String inStr = "0"; // Current input string
// Previous operator: '+', '-', '*', '/', '=' or ' ' (no operator)
private char lastOperator = ' ';

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calc);

    // Retrieve a reference to the EditText field for displaying the result.
    txtResult = (TextView) findViewById(R.id.txtResultId);
    txtResult.setText("0");

    // Register listener (this class) for all the buttons
    BtnListener listener = new BtnListener();
    ((Button) findViewById(R.id.btnNum0Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum1Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum2Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum3Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum4Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum5Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum6Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum7Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum8Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnNum9Id)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnAddId)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnSubId)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnMulId)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnDivId)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnClearId)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnEqualId)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnDecimal)).setOnClickListener(listener);
    ((Button) findViewById(R.id.btnDelId)).setOnClickListener(listener);
}

private class BtnListener implements OnClickListener {
    // On-click event handler for all the buttons
    @Override
    public void onClick(View view) {
        int id = view.getId();
        // Number buttons: '0' to '9' and decimal
        if (id == R.id.btnNum0Id || id == R.id.btnNum1Id || id == R.id.btnNum2Id ||
                id == R.id.btnNum3Id || id == R.id.btnNum4Id || id == R.id.btnNum5Id ||
                id == R.id.btnNum6Id || id == R.id.btnNum7Id || id == R.id.btnNum8Id ||
                id == R.id.btnNum9Id || id == R.id.btnDecimal) {
            String inDigit = ((Button) view).getText().toString();
            if (!inStr.contains(".")) {
                if (inStr.equals("0")) {
                    inStr = inDigit;
                } else {
                    inStr += inDigit;
                }
            } else {
                inStr += inDigit;
            }
            txtResult.setText(inStr);
            if (lastOperator == '=') { result = 0; lastOperator = ' '; }
        // Operator buttons
        } else if (id == R.id.btnAddId) {
            compute(); lastOperator = '+';
        } else if (id == R.id.btnSubId) {
            compute(); lastOperator = '-';
        } else if (id == R.id.btnMulId) {
            compute(); lastOperator = '*';
        } else if (id == R.id.btnDivId) {
            compute(); lastOperator = '/';
        } else if (id == R.id.btnEqualId) {
            compute(); lastOperator = '=';
        // Clear / delete
        } else if (id == R.id.btnClearId) {
            result = 0; inStr = "0"; lastOperator = ' '; txtResult.setText("0");
        } else if (id == R.id.btnDelId) {
            if (inStr.length() > 1) inStr = inStr.substring(0, inStr.length() - 1);
            else if (inStr.length() == 1) inStr = "0";
            txtResult.setText(inStr);
        }
    }

    // User pushes '+', '-', '*', '/' or '=' button.
    // Perform computation on the previous result and the current input number,
    // based on the previous operator.
    private void compute() {
        float inNum = Float.parseFloat(inStr);
        inStr = "0";
        if (lastOperator == ' ') {
            result = inNum;
        } else if (lastOperator == '+') {
            result += inNum;
        } else if (lastOperator == '-') {
            result -= inNum;
        } else if (lastOperator == '*') {
            result *= inNum;
        } else if (lastOperator == '/') {
            result /= inNum;
        } else if (lastOperator == '=') {
            // Keep the result for the next operation
        }
        txtResult.setText(String.valueOf(result));
    }
    }

    public void onDone(View v){
        if(lastOperator==' ')
            result = Float.parseFloat(inStr);
        Intent data = new Intent();
        data.putExtra("RESULT",result);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onCancel(View v){
        setResult(RESULT_CANCELED);
        finish();
    }
}

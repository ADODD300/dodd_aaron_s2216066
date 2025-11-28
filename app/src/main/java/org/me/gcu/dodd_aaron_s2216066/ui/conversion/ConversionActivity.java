package org.me.gcu.dodd_aaron_s2216066.ui.conversion;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.me.gcu.dodd_aaron_s2216066.R;

public class ConversionActivity extends AppCompatActivity {

    private TextView conversionTitleTextView;
    private TextView currencyInfoTextView;
    private RadioGroup directionRadioGroup;
    private RadioButton radioGbpToCur;
    private RadioButton radioCurToGbp;
    private EditText amountEditText;
    private Button convertButton;
    private TextView resultTextView;
    private TextView errorTextView;

    private String code;
    private String name;
    private double rate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        conversionTitleTextView = findViewById(R.id.conversionTitleTextView);
        currencyInfoTextView = findViewById(R.id.currencyInfoTextView);
        directionRadioGroup = findViewById(R.id.directionRadioGroup);
        radioGbpToCur = findViewById(R.id.radioGbpToCur);
        radioCurToGbp = findViewById(R.id.radioCurToGbp);
        amountEditText = findViewById(R.id.amountEditText);
        convertButton = findViewById(R.id.convertButton);
        resultTextView = findViewById(R.id.resultTextView);
        errorTextView = findViewById(R.id.errorTextView);

        code = getIntent().getStringExtra("code");
        name = getIntent().getStringExtra("name");
        rate = getIntent().getDoubleExtra("rate", 0.0);

        if (code == null) code = "";
        if (name == null) name = "";

        conversionTitleTextView.setText("Convert GBP / " + code);
        String info = String.format(
                "Currency: %s (%s)\nCurrent rate: 1 GBP = %.4f %s",
                name, code, rate, code);
        currencyInfoTextView.setText(info);

        radioGbpToCur.setChecked(true);

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performConversion();
            }
        });
    }

    private void performConversion() {
        errorTextView.setText("");
        resultTextView.setText("");

        String amountStr = amountEditText.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            errorTextView.setText("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            errorTextView.setText("Invalid number format.");
            return;
        }

        if (amount < 0) {
            errorTextView.setText("Amount cannot be negative.");
            return;
        }

        boolean gbpToCur = radioGbpToCur.isChecked();
        double result;

        if (gbpToCur) {
            result = amount * rate;
            String text = String.format("%.2f GBP = %.2f %s", amount, result, code);
            resultTextView.setText(text);
        } else {
            // CUR → GBP
            if (rate == 0.0) {
                errorTextView.setText("Rate is zero; cannot convert.");
                return;
            }
            result = amount / rate;
            String text = String.format("%.2f %s = %.2f GBP", amount, code, result);
            resultTextView.setText(text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

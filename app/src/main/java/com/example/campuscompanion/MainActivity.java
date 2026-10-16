package com.example.campuscompanion;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etName;
    private TextView screen1Greeting;
    private Button btnSayHello, btnClear, btnCoffee, btnQuiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. connect variables to the layout
        etName = findViewById(R.id.etName);
        screen1Greeting = findViewById(R.id.screen1Greeting);
        btnSayHello = findViewById(R.id.btnSayHello);
        btnClear = findViewById(R.id.btnClear);
        btnCoffee = findViewById(R.id.btnCoffee);
        btnQuiz = findViewById(R.id.btnQuiz);

        // 2. restore text after rotation (before the watcher is added)
        if (AppState.validname) {
            etName.setText(AppState.enteredText);
        }

        // 3. text watcher
        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (AppState.validname && s.toString().equals(AppState.enteredText)) {
                    return;   // not a real edit (e.g. rotation restore)
                }
                AppState.validname = false;
                etName.setError(null);
                UpdateVal();
            }
        });

        // 4. click listeners
        btnSayHello.setOnClickListener(v -> onSayHello());

        btnClear.setOnClickListener(v -> {
            AppState.validname = false;
            AppState.name = "";
            AppState.enteredText = "";
            etName.setText("");
            etName.setError(null);
            UpdateVal();
        });

        btnCoffee.setOnClickListener(v -> openScreen(CoffeeActivity.class));
        btnQuiz.setOnClickListener(v -> openScreen(QuizActivity.class));

        // 5. draw the screen from the state
        UpdateVal();
    }

    private void UpdateVal() {
        if (AppState.validname) {
            screen1Greeting.setText(getString(R.string.hello_format, AppState.name));
        } else {
            screen1Greeting.setText("");
        }
        btnCoffee.setEnabled(AppState.validname);
        btnQuiz.setEnabled(AppState.validname);
    }

    private void onSayHello() {
        String raw = etName.getText().toString();
        String name = raw.trim();

        if (name.length() < 2 || name.length() > 30) {
            AppState.validname = false;
            etName.setError(getString(R.string.error_name));
            UpdateVal();
            return;
        }

        AppState.name = name;
        AppState.textentered = raw;
        AppState.validname = true;
        etName.setError(null);
        UpdateVal();
    }

    private void openScreen(Class<?> target) {
        Intent i = new Intent(this, target);
        i.putExtra("name", AppState.name);
        startActivity(i);
    }
}
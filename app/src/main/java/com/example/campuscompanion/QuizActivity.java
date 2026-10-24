package com.example.campuscompanion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


public class QuizActivity extends AppCompatActivity {

    private static final String keyIndex = "index";
    private static final String keySel = "selected";
    private static final String keySc = "score";
    private static final String keyChecked = "checked";

    private static final String[] letters = {"A", "B", "C"};

    private TextView progressTextView;
    private TextView scoreTextView;
    private TextView questionTextView;
    private RadioButton[] optionButtons;
    private TextView feedbackTextView;
    private Button priButton;

    private final Question[] questionBank = new Question[] {
            new Question(R.string.question_textview,
                    new int[] {R.string.option_textview, R.string.option_edittext, R.string.option_button}, 0),
            new Question(R.string.question_click,
                    new int[] {R.string.option_text_change, R.string.option_click, R.string.option_rotation}, 1),
            new Question(R.string.question_extras,
                    new int[] {R.string.option_radio_group, R.string.option_textview, R.string.option_checkbox}, 2)
    };

    private int currentIndex = 0;
    private int sel = -1;      // -1 means no option selected
    private int score = 0;
    private boolean checked = false;


    private final ActivityResultLauncher<Intent> restartLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Cancel keeps all progress
                    if (result.getResultCode() == RESULT_OK) {
                        restart();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        restart();
                    } else {
                        finish();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt(keyIndex, 0);
            sel = savedInstanceState.getInt(keySel, -1);
            score = savedInstanceState.getInt(keySc, 0);
            checked = savedInstanceState.getBoolean(keyChecked, false);
        }

        TextView studentTextView = findViewById(R.id.student_text_view);
        progressTextView = findViewById(R.id.progress_text_view);
        scoreTextView = findViewById(R.id.score_text_view);
        questionTextView = findViewById(R.id.question_text_view);
        optionButtons = new RadioButton[] {
                findViewById(R.id.option_a),
                findViewById(R.id.option_b),
                findViewById(R.id.option_c)
        };
        feedbackTextView = findViewById(R.id.feedback_text_view);
        priButton = findViewById(R.id.primary_button);
        Button restartButton = findViewById(R.id.restart_button);
        Button backButton = findViewById(R.id.back_button);

        String name = getIntent().getStringExtra(MainActivity.EXTRA_STUDENT_NAME);
        if (name == null) {
            name = "";
            studentTextView.setText("Student: " + name);
        } else {
            studentTextView.setText("Student: " + name);
        }

        for (int i = 0; i < optionButtons.length; i++) {
            final int optionIndex = i;
            optionButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checked) {
                        sel = optionIndex;
                    }
                    updateQuestion();
                }
            });
        }

        priButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checked) {
                    checkAnswer();
                } else if (isLastQuestion()) {
                    showResult();
                } else {
                    currentIndex++;
                    sel = -1;
                    checked = false;
                }
                updateQuestion();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizActivity.this, ConfirmActivity.class);
                intent.putExtra(ConfirmActivity.extra_title, getString(R.string.restart_title));
                intent.putExtra(ConfirmActivity.extra_message, getString(R.string.restart_message));
                restartLauncher.launch(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateQuestion();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(keyIndex, currentIndex);
        savedInstanceState.putInt(keySel, sel);
        savedInstanceState.putInt(keySc, score);
        savedInstanceState.putBoolean(keyChecked, checked);
    }

    private boolean isLastQuestion() {
        return currentIndex == questionBank.length - 1;
    }

    private void checkAnswer() {
        if (checked || sel < 0) return;
        checked = true;
        if (sel == questionBank[currentIndex].getCorrectIndex()) {
            score++;
        }
    }

    private void showResult() {
        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra(ResultActivity.extraSc, score);
        intent.putExtra(ResultActivity.extraTotal, questionBank.length);
        resultLauncher.launch(intent);
    }

    private void restart() {
        currentIndex = 0;
        sel = -1;
        score = 0;
        checked = false;
        updateQuestion();
    }

    @SuppressLint("SetTextI18n")
    private void updateQuestion() {
        Question question = questionBank[currentIndex];
        int[] options = question.getOptionResIds();

        progressTextView.setText(getString(R.string.question) + " " + (currentIndex + 1) + " " + "of" + " " + questionBank.length);
        scoreTextView.setText("Score" + " " + score);
        questionTextView.setText(question.getTextResId());

        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(letters[i] + "  " + getString(options[i]));
            optionButtons[i].setChecked(i == sel);
            optionButtons[i].setEnabled(!checked);
        }

        if (!checked) {
            feedbackTextView.setText("");
        } else if (sel == question.getCorrectIndex()) {
            feedbackTextView.setText(R.string.correct_feedback);
        } else {
            int c = question.getCorrectIndex();
            feedbackTextView.setText(getString(R.string.incorrect_feedback) + " "
                    + letters[c] + " " + getString(options[c]) + ".");
        }

        if (!checked) {
            priButton.setText("Check Answer");
            priButton.setEnabled(sel >= 0);
        } else {
            if (isLastQuestion()) {
                priButton.setText("See result");
            } else {
                priButton.setText("Next question");
            }
            priButton.setEnabled(true);
        }
    }
}

package com.example.campuscompanion;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private static final String[] LETTERS = {"A", "B", "C"};

    private QuizViewModel vm;

    private TextView tvStudent, tvProgress, tvScore, tvQuestion, tvFeedback;
    private TextView tvResultScore, tvResultMessage;
    private View questionGroup, resultGroup;
    private List<RadioButton> options;
    private Button btnPrimary, btnRestart, btnBack;

    private boolean rendering = false;   // stops listeners firing during render()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        vm = new ViewModelProvider(this).get(QuizViewModel.class);

        tvStudent = findViewById(R.id.tvStudent);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        questionGroup = findViewById(R.id.questionGroup);
        tvQuestion = findViewById(R.id.tvQuestion);
        options = Arrays.asList(
                (RadioButton) findViewById(R.id.optionA),
                (RadioButton) findViewById(R.id.optionB),
                (RadioButton) findViewById(R.id.optionC)
        );
        tvFeedback = findViewById(R.id.tvFeedback);
        resultGroup = findViewById(R.id.resultGroup);
        tvResultScore = findViewById(R.id.tvResultScore);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnRestart = findViewById(R.id.btnRestart);
        btnBack = findViewById(R.id.btnBack);

        // Student name: pass it from the previous screen with
        // intent.putExtra("student_name", "...")
        String name = getIntent().getStringExtra("student_name");
        if (name == null) name = "Student";
        tvStudent.setText(getString(R.string.student_label, name));

        for (int i = 0; i < options.size(); i++) {
            final int optionIndex = i;
            options.get(i).setOnClickListener(v -> {
                if (!rendering) {
                    vm.select(optionIndex);
                    render();
                }
            });
        }

        btnPrimary.setOnClickListener(v -> {
            if (!vm.isChecked()) {
                vm.check();
            } else if (vm.isLastQuestion()) {
                vm.showResult();
            } else {
                vm.next();
            }
            render();
        });

        btnRestart.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Restart quiz?")
                        .setMessage("Your progress and score will be lost.")
                        .setPositiveButton("Restart", (dialog, which) -> {
                            vm.restart();
                            render();
                        })
                        .setNegativeButton("Cancel", null)   // preserves everything
                        .show());

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        render();
    }

    /** Draws the current state. Never changes the score. */
    private void render() {
        rendering = true;

        tvScore.setText(getString(R.string.score_label, vm.getScore()));

        if (vm.isFinished()) {
            questionGroup.setVisibility(View.GONE);
            resultGroup.setVisibility(View.VISIBLE);
            btnPrimary.setVisibility(View.GONE);
            tvProgress.setText(getString(R.string.completed));
            tvResultScore.setText(getString(R.string.final_score,
                    vm.getScore(), vm.getQuestions().size()));

            String message;
            switch (vm.getScore()) {
                case 3:  message = "Excellent! Perfect score."; break;
                case 2:  message = "Good job! Review the one you missed."; break;
                default: message = "Keep practicing. Try the quiz again."; break;
            }
            tvResultMessage.setText(message);

            rendering = false;
            return;
        }

        questionGroup.setVisibility(View.VISIBLE);
        resultGroup.setVisibility(View.GONE);
        btnPrimary.setVisibility(View.VISIBLE);

        Question q = vm.getCurrent();
        tvProgress.setText(getString(R.string.question_label,
                vm.getIndex() + 1, vm.getQuestions().size()));
        tvQuestion.setText(q.getText());

        for (int i = 0; i < options.size(); i++) {
            RadioButton rb = options.get(i);
            rb.setText(LETTERS[i] + "  " + q.getOptions().get(i));
            rb.setChecked(i == vm.getSelected());
            rb.setEnabled(!vm.isChecked());          // lock after checking
        }

        // Feedback
        if (vm.isChecked()) {
            tvFeedback.setVisibility(View.VISIBLE);
            if (vm.getSelected() == q.getCorrectIndex()) {
                tvFeedback.setText("Correct! +1 point");
            } else {
                int c = q.getCorrectIndex();
                tvFeedback.setText("Incorrect. The correct answer is "
                        + LETTERS[c] + " " + q.getOptions().get(c) + ".");
            }
        } else {
            tvFeedback.setVisibility(View.GONE);
            tvFeedback.setText("");
        }

        // Primary action
        if (!vm.isChecked()) {
            btnPrimary.setText("Check answer");
            btnPrimary.setEnabled(vm.getSelected() >= 0);
        } else if (vm.isLastQuestion()) {
            btnPrimary.setText("See result");
            btnPrimary.setEnabled(true);
        } else {
            btnPrimary.setText("Next question");
            btnPrimary.setEnabled(true);
        }

        rendering = false;
    }
}

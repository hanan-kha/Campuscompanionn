package com.example.campuscompanion;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

public class QuizViewModel extends ViewModel {

    private static final String KEY_INDEX = "index";
    private static final String KEY_SELECTED = "selected";
    private static final String KEY_SCORE = "score";
    private static final String KEY_CHECKED = "checked";
    private static final String KEY_FINISHED = "finished";

    private final SavedStateHandle state;
    private final List<Question> questions;

    public QuizViewModel(SavedStateHandle state) {
        this.state = state;
        this.questions = Arrays.asList(
                new Question("Which view displays a text label?",
                        Arrays.asList("TextView", "EditText", "Button"), 0),
                new Question("Which event handles a button tap?",
                        Arrays.asList("Text change", "Click", "Screen rotation"), 1),
                new Question("Which control allows independent selection of several extras?",
                        Arrays.asList("RadioButton group", "TextView", "CheckBox"), 2)
        );
    }

    // ---- state accessors (backed by SavedStateHandle) ----

    private int getInt(String key, int defaultValue) {
        Integer v = state.get(key);
        return v != null ? v : defaultValue;
    }

    private boolean getBoolean(String key) {
        Boolean v = state.get(key);
        return v != null && v;
    }

    public int getIndex()        { return getInt(KEY_INDEX, 0); }
    public int getSelected()     { return getInt(KEY_SELECTED, -1); }  // -1 = nothing selected
    public int getScore()        { return getInt(KEY_SCORE, 0); }
    public boolean isChecked()   { return getBoolean(KEY_CHECKED); }
    public boolean isFinished()  { return getBoolean(KEY_FINISHED); }

    public List<Question> getQuestions() { return questions; }
    public Question getCurrent()         { return questions.get(getIndex()); }
    public boolean isLastQuestion()      { return getIndex() == questions.size() - 1; }

    // ---- actions ----

    public void select(int option) {
        if (isChecked() || isFinished()) return;   // locked after checking
        state.set(KEY_SELECTED, option);           // does NOT change score
    }

    public void check() {
        if (isChecked() || isFinished() || getSelected() < 0) return;  // can't score twice
        state.set(KEY_CHECKED, true);
        if (getSelected() == getCurrent().getCorrectIndex()) {
            state.set(KEY_SCORE, getScore() + 1);
        }
    }

    public void next() {
        if (!isChecked() || isLastQuestion()) return;   // never go past the list
        state.set(KEY_INDEX, getIndex() + 1);
        state.set(KEY_SELECTED, -1);
        state.set(KEY_CHECKED, false);
    }

    public void showResult() {
        if (isChecked() && isLastQuestion()) {
            state.set(KEY_FINISHED, true);
        }
    }

    public void restart() {
        state.set(KEY_INDEX, 0);
        state.set(KEY_SELECTED, -1);
        state.set(KEY_SCORE, 0);
        state.set(KEY_CHECKED, false);
        state.set(KEY_FINISHED, false);
    }
}
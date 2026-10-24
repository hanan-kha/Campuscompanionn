package com.example.campuscompanion;

public class Question {
    private final int textResId;
    private final int[] optionResIds;
    private final int correctIndex;

    public Question(int textResId, int[] optionResIds, int correctIndex) {
        this.textResId = textResId;
        this.optionResIds = optionResIds;
        this.correctIndex = correctIndex;
    }

    public int getTextResId() {
        return textResId;
    }

    public int[] getOptionResIds() {
        return optionResIds;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }
}

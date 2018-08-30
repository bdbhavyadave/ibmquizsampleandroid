package com.example.ibmsamplequiz.modelClass;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class userQuizzes {

    @SerializedName("quizid")
    @Expose
    private String id;

    @SerializedName("correct_questions")
    @Expose
    private int correct;

    @SerializedName("incorrect_questions")
    @Expose
    private int incorrect;

    @SerializedName("score")
    @Expose
    private int score;

    public userQuizzes(String id, int correct, int incorrect, int score) {
        this.id = id;
        this.correct = correct;
        this.incorrect = incorrect;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public int getIncorrect() {
        return incorrect;
    }

    public void setIncorrect(int incorrect) {
        this.incorrect = incorrect;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

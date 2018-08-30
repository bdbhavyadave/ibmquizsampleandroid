package com.example.ibmsamplequiz.modelClass;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Doc {


    @SerializedName("_id")
    @Expose
    private String id;

    @SerializedName("_rev")
    @Expose
    private String rev;

    @SerializedName("quizid")
    @Expose
    private String quizid;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("number_of_questions")
    @Expose
    private int numberOfQuestions;

    @SerializedName("marks_per_question")
    @Expose
    private int marksPerQuestion;

    @SerializedName("total_time_allowed_in_minutes")
    @Expose
    private int totalTime;

    @SerializedName("questions")
    @Expose
    private List<QuestionsResponse> questions;

    public int getPassing() {
        return passing;
    }

    public void setPassing(int passing) {
        this.passing = passing;
    }

    @SerializedName("passing_percentage")
    @Expose
    private int passing;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public int getMarksPerQuestion() {
        return marksPerQuestion;
    }

    public void setMarksPerQuestion(int marksPerQuestion) {
        this.marksPerQuestion = marksPerQuestion;
    }

    public List<QuestionsResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List <QuestionsResponse> questions) {
        this.questions = questions;
    }



    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }



    public String getQuizid() {
        return quizid;
    }

    public void setQuizid(String quizid) {
        this.quizid = quizid;
    }

    public Doc(String id, String rev,String quizid, String title, int numberOfQuestions, int marksPerQuestion,int totalTime, List <QuestionsResponse> questions, int passing) {
        this.id = id;
        this.rev = rev;
        this.title = title;
        this.numberOfQuestions = numberOfQuestions;
        this.marksPerQuestion = marksPerQuestion;
        this.questions = questions;
        this.totalTime = totalTime;
        this.quizid = quizid;
        this.passing = passing;
    }

}

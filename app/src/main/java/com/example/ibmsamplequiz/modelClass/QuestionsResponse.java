package com.example.ibmsamplequiz.modelClass;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionsResponse {

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public List <String> getOptions() {
        return options;
    }

    public void setOptions(List <String> options) {
        this.options = options;
    }

    @SerializedName("statement")
    @Expose
    private String statement;

    @SerializedName("options")
    @Expose
    private List<String> options;

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    @SerializedName("correctoption")
    @Expose
    private String correctOption;



    public QuestionsResponse(String statement, List <String> options, String correctOption) {
        this.statement = statement;
        this.options = options;
        this.correctOption = correctOption;
    }
}

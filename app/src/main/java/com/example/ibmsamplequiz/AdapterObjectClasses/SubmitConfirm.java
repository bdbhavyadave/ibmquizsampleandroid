package com.example.ibmsamplequiz.AdapterObjectClasses;

public class SubmitConfirm {

    private String selectedOption, question,status;

    public SubmitConfirm(String selectedOption, String question) {
        this.selectedOption = selectedOption;
        this.question = question;
    }

    public SubmitConfirm(String selectedOption, String question,String status) {
        this.selectedOption = selectedOption;
        this.question = question;
        this.status = status;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}

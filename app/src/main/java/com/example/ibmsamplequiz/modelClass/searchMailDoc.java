package com.example.ibmsamplequiz.modelClass;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class searchMailDoc {

    @SerializedName("_id")
    @Expose
    private String id;

    @SerializedName("_rev")
    @Expose
    private String rev;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("quizzes")
    @Expose
    private List<userQuizzes> userQuizzes;

    public searchMailDoc(String id, String rev, String name, String email, List <userQuizzes> userQuizzes) {
        this.id = id;
        this.rev = rev;
        this.name = name;
        this.email = email;
        this.userQuizzes = userQuizzes;
    }

    public searchMailDoc(String name, String email, List <userQuizzes> userQuizzes) {
        this.name = name;
        this.email = email;
        this.userQuizzes = userQuizzes;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List <userQuizzes> getUserQuizzes() {
        return userQuizzes;
    }

    public void setUserQuizzes(List <userQuizzes> userQuizzes) {
        this.userQuizzes = userQuizzes;
    }
}

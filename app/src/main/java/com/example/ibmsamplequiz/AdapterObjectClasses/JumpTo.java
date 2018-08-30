package com.example.ibmsamplequiz.AdapterObjectClasses;

public class JumpTo {

        private int content;
        private String state;
        private boolean current;

    public JumpTo(int content, String state, boolean current) {
        this.content = content;
        this.state = state;
        this.current = current;
    }

    public int getContent() {
        return content;
    }

    public void setContent(int content) {
        this.content = content;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}


package com.example.myapplication;

import java.io.Serializable;

public class Subtask implements Serializable {
    private String title;
    private boolean done;

    public Subtask(String title, boolean done) {
        this.title = title;
        this.done = done;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}

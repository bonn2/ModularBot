package com.bonn2.modules;

public abstract class Module {

    public enum Priority {
        PRE_JDA_HIGH, PRE_JDA_LOW, POST_JDA_HIGH, POST_JDA_LOW
    }

    public String version = "v0.0";
    public Priority priority = Priority.POST_JDA_LOW;
    public String name = "Unnamed";

    public abstract void load();
}

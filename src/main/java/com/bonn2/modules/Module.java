package com.bonn2.modules;

public abstract class Module {

    public String version = "v0.0";
    public int permissions = 0;

    public abstract void load();
}

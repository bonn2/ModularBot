package com.bonn2.modules;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public abstract class Module {

    public enum Priority {
        PRE_JDA_HIGH, PRE_JDA_LOW, POST_JDA_HIGH, POST_JDA_LOW, SETTINGS, DO_NOT_LOAD
    }

    public String version = "v0.0";
    public Priority priority = Priority.POST_JDA_LOW;
    public String name = "Unnamed";

    public abstract void registerSettings();
    public abstract void load();
    public abstract CommandData[] getCommands();
}

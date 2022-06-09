package com.bonn2.modules;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class Module {

    public abstract String getName();
    public abstract String getVersion();

    public abstract void registerSettings();
    public abstract void load();
    public abstract CommandData[] getCommands();
}

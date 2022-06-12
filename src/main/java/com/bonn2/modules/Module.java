package com.bonn2.modules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.io.IOException;
import java.io.InputStream;

public abstract class Module {

    public String name;
    public String version;

    public Module() {
        try {
            InputStream metaStream = this.getClass().getResourceAsStream("meta.json");
            if (metaStream != null) {
                JsonObject meta = new Gson().fromJson(new String(metaStream.readAllBytes()), JsonObject.class);
                name = meta.get("name").getAsString();
                version = meta.get("version").getAsString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract String getName();
    public abstract String getVersion();

    public abstract void registerSettings();
    public abstract void load();
    public abstract CommandData[] getCommands();
}

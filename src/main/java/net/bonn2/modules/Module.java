package net.bonn2.modules;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public abstract class Module {

    protected String name;
    protected String version;
    protected String description;
    protected String author;
    protected JsonArray depends;

    public Module()  {
        // Get location of the module
        CodeSource src = getClass().getProtectionDomain().getCodeSource();
        if (src == null) return;

        try {
            // Get all instances of meta.json
            Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources("meta.json");
            List<URL> metas = new ArrayList<>();
            while (urlEnumeration.hasMoreElements()) metas.add(urlEnumeration.nextElement());

            metas.forEach(metaURL -> {
                // Get relevant meta.json based on parent path
                if (metaURL.getPath().contains(src.getLocation().getPath())) {
                    // Read meta.json and set values
                    try (InputStream metaStream = metaURL.openStream()) {
                        JsonObject meta = new Gson().fromJson(new String(metaStream.readAllBytes()), JsonObject.class);
                        name = meta.get("name") == null ? "NoName" : meta.get("name").getAsString();
                        version = meta.get("version") == null ? "v0.0" : meta.get("version").getAsString();
                        description = meta.get("description") == null ? "" : meta.get("description").getAsString();
                        author = meta.get("author") == null ? "" : meta.get("author").getAsString();
                        depends = meta.get("depends") == null ? new JsonArray() : meta.get("depends").getAsJsonArray();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public JsonArray getDepends() {
        return depends;
    }

    public abstract void registerLoggingChannels();

    public abstract void registerSettings();
    public abstract void load();
    public abstract CommandData[] getCommands();
}

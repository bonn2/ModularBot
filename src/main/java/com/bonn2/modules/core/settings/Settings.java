package com.bonn2.modules.core.settings;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.types.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.bonn2.Bot.*;

public class Settings extends Module {

    public Settings() {
        version = "v1.0";
        priority = Priority.SETTINGS;
        name = "Settings";
    }

    // All registered settings
    // Mapping:
    // Module -> Key -> Type
    static Map<String, Map<String, Setting.Type>> registeredSettings = new HashMap<>();
    static Map<String, Map<String, Setting>> defaultSettings = new HashMap<>();
    static Map<String, Map<String, Setting>> settings = new HashMap<>();
    static File settingsFile = new File(localPath + "/settings.json");

    @Override
    public void load() {
        logger.info("Registering listeners...");
        Bot.jda.addEventListener(new SettingsListener());

        logger.info("Creating commands...");
        commands = commands.addCommands(
                Commands.slash(
                        "settings",
                        "Manage settings"
                ).addOption(
                        OptionType.STRING,
                        "module",
                        "The module to change the settings of.",
                        false
                ).addOption(
                        OptionType.STRING,
                        "setting",
                        "The setting to change.",
                        false
                ).addOption(
                        OptionType.STRING,
                        "value",
                        "The value to set the setting to.",
                        false
                )
        );

        logger.info("Loading settings from file...");
        try {
            if (!settingsFile.createNewFile()) {
                JsonObject jsonObject = new Gson().fromJson(new String(new FileInputStream(settingsFile).readAllBytes()), JsonObject.class);
                if (jsonObject != null) {
                    for (String moduleKey : jsonObject.keySet()) {
                        Module module = Bot.getModuleIgnoreCase(moduleKey);
                        if (module != null) {
                            Map<String, Setting.Type> registeredSettings = getRegisteredSettings(moduleKey);
                            for (String settingKey : jsonObject.get(moduleKey).getAsJsonObject().keySet()) {
                                if (registeredSettings.containsKey(settingKey)) {
                                    String[] typeValue = jsonObject.get(moduleKey).getAsJsonObject().get(settingKey)
                                            .getAsString()
                                            .split(":", 2);
                                    if (Setting.Type.fromString(typeValue[0]) == registeredSettings.get(settingKey)) {
                                        set(
                                                module,
                                                settingKey,
                                                typeValue[1],
                                                false
                                        );
                                    } else {
                                        logger.warn("Type mismatch in setting %s -> %s; %s != %s".formatted(
                                                module.name,
                                                settingKey,
                                                Setting.Type.fromString(typeValue[0]),
                                                getRegisteredSettings(moduleKey).get(settingKey)
                                        ));
                                    }
                                } else {
                                    logger.warn("Found unregistered settings for module %s; %s".formatted(
                                            module.name,
                                            settingKey
                                    ));
                                }
                            }
                        } else {
                            logger.warn("Found settings for a module that does not exist: %s".formatted(moduleKey));
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load settings!");
            e.printStackTrace();
        }
    }

    public static void register(@NotNull Module module, String key, Setting.Type type, String unSet) {
        // Register setting type
        Map<String, Setting.Type> moduleSettings;
        if (registeredSettings.containsKey(module.name))
            moduleSettings = registeredSettings.get(module.name);
        else
            moduleSettings = new HashMap<>();
        moduleSettings.put(key, type);
        registeredSettings.put(module.name, moduleSettings);

        // Register setting default
        Map<String, Setting> moduleDefaults;
        if (defaultSettings.containsKey(module.name))
            moduleDefaults = defaultSettings.get(module.name);
        else
            moduleDefaults = new HashMap<>();
        moduleDefaults.put(key, Setting.of(unSet, type));
        defaultSettings.put(module.name, moduleDefaults);
    }

    public static Map<String, Setting.Type> getRegisteredSettings(String module) {
        if (registeredSettings.containsKey(module))
            return registeredSettings.get(module);
        else
            return new HashMap<>();
    }

    private static void save() {
        logger.info("Saving settings to file.");
        JsonObject jsonObject = new JsonObject();
        for (String moduleKey : settings.keySet()) {
            JsonObject moduleObject = new JsonObject();
            Map<String, Setting> moduleMap = settings.get(moduleKey);
            for (String settingKey : moduleMap.keySet()) {
                moduleObject.add(settingKey, moduleMap.get(settingKey).toJson());
            }
            jsonObject.add(moduleKey, moduleObject);
        }
        if (jsonObject.keySet().size() > 0) {
            try {
                //noinspection ResultOfMethodCallIgnored
                settingsFile.createNewFile();

                new FileOutputStream(settingsFile).write(
                        new GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(jsonObject)
                                .getBytes(StandardCharsets.UTF_8)
                );
            } catch (IOException e) {
                logger.error("Failed to save settings!");
                e.printStackTrace();
            }
        }
    }

    public static boolean set(@NotNull Module module, String key, String value) {
        return set(module, key, value, true);
    }

    public static boolean set(@NotNull Module module, String key, String value, boolean save) {
        // Check if setting is registered and get type
        Setting.Type type;
        if (hasSetting(module, key)) {
                type = registeredSettings.get(module.name).get(key);
        } else {
            logger.warn("Tried to set an unregistered setting!\nModule: %s\nKey: %s\nValue: %s".formatted(
                    module.name,
                    key,
                    value
            ));
            return false;
        }
        // Get current module settings or default
        Map<String, Setting> moduleSettings;
        if (settings.containsKey(module.name))
            moduleSettings = settings.get(module.name);
        else
            moduleSettings = new HashMap<>();

        // Set module setting
        Setting setting = Setting.of(value, type);
        if (setting == null) return false;
        moduleSettings.put(key, setting);
        settings.put(module.name, moduleSettings);

        // Save the new settings to file
        if (save) save();

        // Success
        return true;
    }

    /**
     * Get the value of a {@link Setting}, or a default value
     * @param module The module the {@link Setting} is registered to
     * @param key    The key to the {@link Setting}
     * @return       The {@link Setting} of the registered value, or null if setting is unregistered
     */
    public static @Nullable Setting get(@NotNull Module module, String key) {
        if (hasSetting(module, key)) {
                // If setting is set, return that
                // else return setting value of unset
                if (settings.containsKey(module.name)
                && settings.get(module.name).containsKey(key)) {
                    return settings.get(module.name).get(key);
                } else {
                    return defaultSettings.get(module.name).get(key);
                }
        } else {
            logger.warn("Tried to get an unregistered setting!\nModule: %s\nKey: %s".formatted(
                    module.name,
                    key
            ));
            return null;
        }
    }

    /**
     * Check if a setting is registered
     * @param module The module the setting is registered to
     * @param key    The key to the setting
     * @return       True if the setting is registered, false if it isn't
     */
    public static boolean hasSetting(@NotNull Module module, String key) {
        if (registeredSettings.containsKey(module.name))
            return registeredSettings.get(module.name).containsKey(key);
        return false;
    }

}

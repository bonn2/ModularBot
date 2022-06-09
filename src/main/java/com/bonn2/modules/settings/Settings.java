package com.bonn2.modules.settings;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.settings.types.IntSetting;
import com.bonn2.modules.settings.types.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.bonn2.Bot.*;

public class Settings extends Module {

    public Settings() {
        version = "v1.2";
        priority = Priority.SETTINGS;
        name = "Settings";
    }

    // All registered settings
    // Mapping
    // Module -> Key -> Type
    static Map<String, Map<String, Setting.Type>> registeredSettings = new HashMap<>();
    // Module -> Key -> Default
    static Map<String, Map<String, Setting>> defaultSettings = new HashMap<>();
    // Module -> Key -> Description
    static Map<String, Map<String, String>> descriptions = new HashMap<>();
    // Guild ID -> Module -> Key -> Value
    static Map<String, Map<String, Map<String, Setting>>> settings = new HashMap<>();
    static File settingsFolder = new File(localPath + File.separator + "settings");

    @Override
    public void registerSettings() {

    }

    @Override
    public void load() {
        logger.info("Registering listeners...");
        jda.addEventListener(new SettingsCommand());
        jda.addEventListener(new SettingsTabComplete());

        logger.info("Loading settings from file...");
        try {
            settingsFolder.mkdirs();
            for (String filename : Objects.requireNonNull(settingsFolder.list())) {
                if (!filename.toLowerCase().endsWith(".json")) continue;
                File settingsFile = new File(settingsFolder + File.separator + filename);
                JsonObject jsonObject = new Gson().fromJson(new String(new FileInputStream(settingsFile).readAllBytes()), JsonObject.class);
                if (jsonObject == null) continue;
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
                                            filename.replaceAll(".json", ""),
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
        } catch (IOException e) {
            logger.error("Failed to load settings!");
            e.printStackTrace();
        }
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[] {
                Commands.slash(
                        "settings",
                        "Manage settings"
                ).addOption(
                        OptionType.STRING,
                        "module",
                        "The module to change the settings of.",
                        false,
                        true
                ).addOption(
                        OptionType.STRING,
                        "setting",
                        "The setting to change.",
                        false,
                        true
                ).addOption(
                        OptionType.STRING,
                        "value",
                        "The value to set the setting to.",
                        false,
                        true
                ).addOption(
                        OptionType.STRING,
                        "values",
                        "Use multiple values. (Only works with list type settings)",
                        false
                ).addOption(
                        OptionType.BOOLEAN,
                        "default",
                        "Set an option to default.",
                        false
                ).setDefaultEnabled(false)
        };
    }

    public static void register(@NotNull Module module, String key, Setting.Type type, String unSet, String description) {
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

        // Register setting description
        Map<String, String> moduleSettingDescriptions;
        if (descriptions.containsKey(module.name))
            moduleSettingDescriptions = descriptions.get(module.name);
        else
            moduleSettingDescriptions = new HashMap<>();
        moduleSettingDescriptions.put(key, description);
        descriptions.put(module.name, moduleSettingDescriptions);
    }

    public static Map<String, Setting.Type> getRegisteredSettings(String module) {
        if (registeredSettings.containsKey(module))
            return registeredSettings.get(module);
        else
            return new HashMap<>();
    }

    public static Map<String, String> getDescriptions(String module) {
        if (descriptions.containsKey(module))
            return descriptions.get(module);
        else
            return new HashMap<>();
    }

    private static void save(String guildID) {
        logger.info("Saving settings to file.");
        JsonObject jsonObject = new JsonObject();
        Map<String, Map<String, Setting>> guildSettings = settings.get(guildID);
        for (String moduleKey : guildSettings.keySet()) {
            JsonObject moduleObject = new JsonObject();
            Map<String, Setting> moduleMap = guildSettings.get(moduleKey);
            for (String settingKey : moduleMap.keySet()) {
                moduleObject.add(settingKey, moduleMap.get(settingKey).toJson());
            }
            jsonObject.add(moduleKey, moduleObject);
        }
        if (jsonObject.keySet().size() > 0) {
            try {
                //noinspection ResultOfMethodCallIgnored
                settingsFolder.mkdirs();
                File settingsFile = new File(settingsFolder + File.separator + guildID + ".json");
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

    public static boolean set(@NotNull Module module, String guildID, String key, @NotNull Setting value) {
        return set(module, guildID, key, value.toJson().getAsString().split(":", 2)[1]);
    }

    public static boolean set(@NotNull Module module, String guildID, String key, String value) {
        return set(module, guildID, key, value, true);
    }

    public static boolean set(@NotNull Module module, String guildID, String key, String value, boolean save) {
        // Check if setting is registered and get type
        Setting.Type type;
        if (hasSetting(module, key)) {
                type = getRegisteredSettingType(module, key);
        } else {
            logger.warn("Tried to set an unregistered setting!\nModule: %s\nKey: %s\nValue: %s".formatted(
                    module.name,
                    key,
                    value
            ));
            return false;
        }

        // Get guild settings or default
        Map<String, Map<String, Setting>> guildSettings;
        if (settings.containsKey(guildID))
            guildSettings = settings.get(guildID);
        else
            guildSettings = new HashMap<>();

        // Get current module settings or default
        Map<String, Setting> moduleSettings;
        if (guildSettings.containsKey(module.name))
            moduleSettings = guildSettings.get(module.name);
        else
            moduleSettings = new HashMap<>();

        // Set module setting
        Setting setting = Setting.of(value, type);
        if (setting == null) return false;
        moduleSettings.put(key, setting);
        guildSettings.put(module.name, moduleSettings);
        settings.put(guildID, guildSettings);

        // Save the new settings to file
        if (save) save(guildID);

        // Success
        return true;
    }

    /**
     * Sets a {@link Setting} back to the default. As if it was never set.
     * @param module The {@link Module} the {@link Setting} is registered to
     * @param key    The key to reset
     * @return       True if {@link Setting} exists and was unset. (Will return true on repeat calls)
     */
    public static boolean unSet(@NotNull Module module, String guildID, String key) {
        // Check if setting is registered
        if (!hasSetting(module, key)) {
            logger.warn("Tried to unSet an unregistered setting!\nModule: %s\nKey: %s".formatted(
                    module.name,
                    key
            ));
            return false;
        }

        // Get guild settings or do nothing
        Map<String, Map<String, Setting>> guildSettings;
        if (settings.containsKey(guildID)) {
            guildSettings = settings.get(guildID);

            // Get current module settings or do nothing
            Map<String, Setting> moduleSettings;
            if (settings.containsKey(module.name)) {
                moduleSettings = guildSettings.get(module.name);
                // UnSet module setting
                moduleSettings.remove(key);
                guildSettings.put(module.name, moduleSettings);
                settings.put(guildID, guildSettings);
                // Save the new settings to file
                save(guildID);
            }
        }

        return true;
    }

    /**
     * Get the value of a {@link Setting}, or a default value
     * @param module The module the {@link Setting} is registered to
     * @param key    The key to the {@link Setting}
     * @return       The {@link Setting} of the registered value, or an {@link IntSetting} of value 0 if  unregistered
     */
    public static Setting get(@NotNull Module module, String guildID, String key) {
        if (hasSetting(module, key)) {
                // If setting is set, return that
                // else return setting value of unset
                if (settings.containsKey(guildID)
                        && settings.get(guildID).containsKey(module.name)
                        && settings.get(guildID).get(module.name).containsKey(key)) {
                    return settings.get(guildID).get(module.name).get(key);
                } else {
                    return defaultSettings.get(module.name).get(key);
                }
        } else {
            logger.warn("Tried to get an unregistered setting!\nModule: %s\nKey: %s".formatted(
                    module.name,
                    key
            ));
            return new IntSetting(0);
        }
    }

    public static int registeredSettingsCount(@NotNull Module module) {
        if (registeredSettings.containsKey(module.name))
            return registeredSettings.get(module.name).keySet().size();
        return 0;
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

    public static Setting.Type getRegisteredSettingType(Module module, String key) {
        if (hasSetting(module, key)) {
            return registeredSettings.get(module.name).get(key);
        }
        return Setting.Type.NULL;
    }

    /**
     * Returns a {@link Set<String>} of all the setting keys registered to a module
     * @param module The module to check
     * @return       The {@link Set<String>} of settings registered
     */
    public static Set<String> getSettings(@NotNull Module module) {
        if (registeredSettings.get(module.name) == null)
            return new HashSet<>(0);
        return registeredSettings.get(module.name).keySet();
    }

}

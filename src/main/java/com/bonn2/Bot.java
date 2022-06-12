package com.bonn2;

import com.bonn2.modules.Module;
import com.bonn2.modules.config.Config;
import com.bonn2.modules.settings.Settings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Bot
{
    public final static Logger logger = LoggerFactory.getLogger("Modular Bot");
    public static JDA jda = null;
    public static String localPath = null;
    public static String modulePath = null;
    public static List<Module> modules = new LinkedList<>();

    // Handles starting the bot, and initializing static variables
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        logger.info("Starting Bot...");

        // Get relevant paths
        File jarFile = new File(Bot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        localPath = jarFile.getParent() + File.separator;
        modulePath = localPath + "modules" + File.separator;
        File moduleFolder = new File(modulePath);
        if (moduleFolder.mkdir()) logger.info("Created new modules folder.");

        // Load Config
        logger.info("Loading Config...");
        Config config = new Config();
        config.load();
        modules.add(config);

        // Check token
        if (Objects.equals(Config.get("token").getAsString(), "")) {
            logger.error("Token is empty!");
            return;
        }

        // Get Module Jars
        logger.info("Getting Modules...");
        File[] externalModules = moduleFolder.listFiles();
        if (externalModules == null) externalModules = new File[0];

        // Load external modules
        for (File externalFile : externalModules) {
            if (!externalFile.getName().endsWith(".jar")) continue;
            Module module = loadModuleFromFile(externalFile.getAbsolutePath());
            if (module == null) {
                logger.info("Failed to load module: " + externalFile.getName());
                continue;
            }
            modules.add(module);
            logger.info("Found: %s %s".formatted(module.getName(), module.getVersion()));
        }

        // TODO: 6/9/2022 Decide what intents and caches are required

        jda = JDABuilder.createDefault(Config.get("token").getAsString())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_BANS,
                        GatewayIntent.GUILD_PRESENCES
                )
                .enableCache(
                        CacheFlag.ACTIVITY
                )
                .build();

        // Wait until jda is initialized
        jda.awaitReady();

        logger.info("Logged in to: " + jda.getSelfUser().getAsTag());

        logger.info("Registering Settings...");
        for (Module module : modules) {
            module.registerSettings();
            logger.info("Registered settings for %s version %s".formatted(module.getName(), module.getVersion()));
        }

        // Load settings
        logger.info("Loading Settings...");
        Settings settings = new Settings();
        settings.load();
        modules.add(settings);

//        for (Module module : modules) {
//            if (!Objects.equals(module.getName(), "Settings") && !Objects.equals(module.getName(), "Config")) {
//                logger.info("Loading %s version %s...".formatted(module.getName(), module.getVersion()));
//                module.load();
//                logger.info("Loaded %s version %s".formatted(module.getName(), module.getVersion()));
//            }
//        }

        for (Guild guild : jda.getGuilds())
            updateCommands(guild);

        logger.info("Finished Loading! (" + ((float)(System.currentTimeMillis() - startTime)) / 1000 + " sec)");
    }

    public static void updateCommands(Guild guild) {
        logger.info("Updating commands...");
        CommandListUpdateAction commandListUpdateAction = guild.updateCommands();
        int totalCommands = 0;
        for (Module module : modules) {
            CommandData[] commands = module.getCommands();
            totalCommands += commands.length;
            logger.info("Got %s command%s from %s".formatted(
                    commands.length,
                    commands.length == 1 ? "" : "s",
                    module.getName()));
            commandListUpdateAction = commandListUpdateAction.addCommands(commands);
        }
        logger.info("Queueing %s / 50 top level command%s".formatted(
                totalCommands,
                totalCommands == 1 ? "" : "s"
        ));
        commandListUpdateAction.queue();
    }

    /**
     * Gets a {@link Module} by a specified name
     * @param name The name of the {@link Module} to get
     * @return     The {@link Module} of the specified name, or null if not found
     */
    public static @Nullable Module getModuleIgnoreCase(@NotNull String name) {
        for (Module module : modules)
            if (module.getName().equalsIgnoreCase(name)) return module;
        return null;
    }

    private static @Nullable Module loadModuleFromFile(String filePath) throws Exception {

        // Get meta.json
        JsonObject meta;
        try (JarFile jarFile = new JarFile(filePath)) {
            JarEntry entry = jarFile.getJarEntry("meta.json");
            if (entry == null) {
                logger.warn(filePath + " has no meta.json!");
                return null;
            }
            meta = new Gson().fromJson(new String(jarFile.getInputStream(entry).readAllBytes()), JsonObject.class);
        }

        File file = new File(filePath);
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()})) {
            try {
                String className = meta.get("main").getAsString();
                Class<?> cc = classLoader.loadClass(className);
                if (!cc.getGenericSuperclass().equals(Module.class)) {
                    logger.warn(filePath + "'s main does not extend Module");
                    return null;
                }
                Object obj = cc.getDeclaredConstructor().newInstance();
                if (obj instanceof Module module) {
                    return module;
                }
            } catch (ClassNotFoundException e) {
                logger.warn("Main class was not found!", e);
            }
        }

        return null;
    }
}

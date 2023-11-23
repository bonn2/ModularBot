package net.bonn2;

import net.bonn2.modules.Module;
import net.bonn2.modules.config.Config;
import net.bonn2.modules.logging.Logging;
import net.bonn2.modules.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;

public class Bot
{
    public final static Logger logger = LoggerFactory.getLogger("Modular Bot");
    public static JDA jda = null;
    public static String localPath = null;
    public static String modulePath = null;
    public final static List<Module> modules = new LinkedList<>();

    // Handles starting the bot, and initializing static variables
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        logger.info("Starting Bot...");

        // Get relevant paths
        File mainFile = new File(Bot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        localPath = mainFile.getParent() + File.separator;
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
        // Load external modules
        File[] files = moduleFolder.listFiles((dir, name) -> name.endsWith(".jar"));

        ArrayList<URL> urls = new ArrayList<>();
        ArrayList<String> classes = new ArrayList<>();
        if(files != null) {
            Arrays.stream(files).forEach(file -> {
                try (JarFile jarFile = new JarFile(file)){
                    urls.add(new URL("jar:file:" + modulePath + File.separator + file.getName() + "!/"));
                    jarFile.stream().forEach(jarEntry -> {
                        if(jarEntry.getName().endsWith(".class"))
                            classes.add(jarEntry.getName());
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            try (URLClassLoader moduleLoader = new URLClassLoader(urls.toArray(new URL[0]))) {
                classes.forEach(s -> {
                    try {
                        Class<?> clazz = moduleLoader.loadClass(s.replaceAll("/",".").replace(".class",""));
                        if(clazz.getGenericSuperclass() != null && clazz.getGenericSuperclass().equals(Module.class))
                        {
                            Module module = (Module) clazz.getConstructor().newInstance();
                            modules.add(module);
                            logger.info("Found " + clazz.getCanonicalName() + " module");
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                        for (StackTraceElement element : e.getStackTrace()) {
                            logger.error("\t\t" + element.toString());
                        }
                    }
                });
            }
        }

        // TODO: 6/9/2022 Decide what intents and caches are required

        jda = JDABuilder.createDefault(Config.get("token").getAsString())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .enableCache(
                        CacheFlag.ACTIVITY
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();

        // Wait until jda is initialized
        jda.awaitReady();

        logger.info("Logged in to: " + jda.getSelfUser().getAsTag());

        logger.info("Loading Logging Module...");
        Logging logging = new Logging();
        logging.load();
        modules.add(logging);

        logger.info("Registering Log Channels...");
        for (Module module : modules) {
            module.registerLoggingChannels();
            logger.info("- Registered log channels for %s version %s".formatted(module.getName(), module.getVersion()));
        }

        logger.info("Registering Settings Module...");
        for (Module module : modules) {
            module.registerSettings();
            logger.info("- Registered settings for %s version %s".formatted(module.getName(), module.getVersion()));
        }

        // Load settings
        logger.info("Loading Settings...");
        Settings settings = new Settings();
        settings.load();
        modules.add(settings);

        updateCommands();

        for (Module module : modules) {
            if (module.getName().equals("Settings")) continue;
            if (module.getName().equals("Config")) continue;
            if (module.getName().equals("Logging")) continue;
            module.load();
        }

        logger.info("Finished Loading! (" + ((float)(System.currentTimeMillis() - startTime)) / 1000 + " sec)");

        // Log startup status
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Bot started!");
        StringBuilder loadedModules = new StringBuilder();
        for (Module module : modules)
            loadedModules
                    .append(module.getName())
                    .append(" ")
                    .append(module.getVersion())
                    .append("\n");
        embedBuilder.addField(
                "Loaded Modules",
                loadedModules.toString(),
                false
        );
        Logging.log(
                "startup",
                new MessageCreateBuilder()
                        .addEmbeds(embedBuilder.build())
                        .build()
        );
    }

    public static void updateCommands() {
        logger.info("Attempting to update commands...");

        for (Guild guild : Bot.jda.getGuilds())
            guild.updateCommands().queue();

        List<String> commandStrings = new ArrayList<>();
        CommandListUpdateAction commandListUpdateAction = jda.updateCommands();
        int totalCommands = 0;
        for (Module module : modules) {
            CommandData[] moduleCommands = module.getCommands();
            totalCommands += moduleCommands.length;
            logger.info("- Got %s command%s from %s".formatted(
                    moduleCommands.length,
                    moduleCommands.length == 1 ? "" : "s",
                    module.getName()));
            commandListUpdateAction = commandListUpdateAction.addCommands(moduleCommands);
            commandStrings.addAll(Arrays.stream(moduleCommands).map(commandData -> commandData.toData().toString()).toList());
        }
        logger.info("Queueing %s top level command%s".formatted(
                totalCommands,
                totalCommands == 1 ? "" : "s"
        ));

        logger.info("Getting current live commands...");
        List<String> liveCommandStrings = jda.retrieveCommands().complete().stream()
                .map(command -> CommandData.fromCommand(command).toData().toString()).toList();

        if (new HashSet<>(liveCommandStrings).containsAll(commandStrings) && new HashSet<>(commandStrings).containsAll(liveCommandStrings)) {
            logger.info("Live commands match local commands!");
        } else {
            logger.info("Pushing local commands!");
            commandListUpdateAction.queue();
        }
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
}

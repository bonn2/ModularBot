package com.bonn2;

import com.bonn2.modules.Module;
import com.bonn2.modules.core.config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Bot
{
    public final static Logger logger = LoggerFactory.getLogger("Modular Bot");
    public static JDA jda = null;
    public static String localPath;
    public static Guild guild;
    public static TextChannel logChannel;
    public static CommandListUpdateAction commands;
    public static List<Module> modules = new LinkedList<>();

    // Handles starting the bot, and initializing static variables
    public static void main(String[] args) throws GeneralSecurityException, URISyntaxException, InterruptedException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        long startTime = System.currentTimeMillis();

        logger.info("Starting Bot...");

        File jarFile = new File(Bot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        localPath = jarFile.getParent() + File.separator;

        logger.info("Locating modules...");
        Reflections reflections = new Reflections("com.bonn2.modules");
        Set<Class<? extends Module>> moduleClasses = reflections.getSubTypesOf(Module.class);
        for (Class<? extends Module> module : moduleClasses) {
            logger.info("Located module %s".formatted(module.getCanonicalName()));
            modules.add(module.getDeclaredConstructor().newInstance());
        }

        logger.info("Sorting modules...");
        modules.sort(new Module.SortByName());

        logger.info("Loading Pre-JDA Modules...");
        for (Module module : modules) {
            if (module.priority.equals(Module.Priority.PRE_JDA_HIGH)) {
                logger.info("Loading %s version %s".formatted(module.name, module.version));
                module.load();
            }
        }
        for (Module module : modules) {
            if (module.priority.equals(Module.Priority.PRE_JDA_LOW)) {
                logger.info("Loading %s version %s".formatted(module.name, module.version));
                module.load();
            }
        }

        jda = JDABuilder.createDefault(Config.get("token").getAsString())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_BANS
                )
                .build();

        // Wait until jda is initialized
        jda.awaitReady();

        logger.info("Logged in to: " + jda.getSelfUser().getAsTag());

        // Get Guild
        guild = jda.getGuildById(Config.get("guild").getAsString());

        // Get Log Channel
        logChannel = (TextChannel) jda.getGuildChannelById(Config.get("LOG_CHANNEL_ID").getAsString());

        // Init commands update action
        commands = guild.updateCommands();

        logger.info("Loading Post-JDA Modules...");
        for (Module module : modules) {
            if (module.priority.equals(Module.Priority.POST_JDA_HIGH)) {
                logger.info("Loading %s version %s".formatted(module.name, module.version));
                module.load();
            }
        }
        for (Module module : modules) {
            if (module.priority.equals(Module.Priority.POST_JDA_LOW)) {
                logger.info("Loading %s version %s".formatted(module.name, module.version));
                module.load();
            }
        }
        for (Module module : modules) {
            if (module.priority.equals(Module.Priority.SETTINGS)) {
                logger.info("Loading %s version %s".formatted(module.name, module.version));
                module.load();
            }
        }

        commands.queue();

        logger.info("Finished Loading! (" + ((float)(System.currentTimeMillis() - startTime)) / 1000 + " sec)");
    }

    public static Module getModuleIgnoreCase(String name) {
        for (Module module : modules)
            if (module.name.equalsIgnoreCase(name)) return module;
        return null;
    }
}

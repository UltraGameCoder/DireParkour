package net.direskies.direparkour;

import net.direskies.direparkour.command.DireParkourCMD;
import net.direskies.direparkour.registries.CourseRegistry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private final Logger log = Logger.getLogger("Minecraft");

    private CourseRegistry courseRegistry;
    private PlayerTracker playerTracker;
    private CourseBuilder courseBuilder;

    @Override
    public void onEnable() {
        this.courseRegistry = new CourseRegistry(this);
        this.playerTracker = new PlayerTracker(courseRegistry);
        this.courseBuilder = new CourseBuilder(courseRegistry);

        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(playerTracker, this);
        pm.registerEvents(courseBuilder, this);

        this.getCommand("parkour").setExecutor(new DireParkourCMD(this));

        log.info("===================================");
        log.info(this.getName()+" has been Enabled!");
        log.info("===================================");
    }

    @Override
    public void onDisable() {
        for (Player builder : courseBuilder.getBuilders()) {
            courseBuilder.cancelBuild(builder);
        }
        log.info(this.getName()+" has been Disabled!");
    }

    public CourseRegistry getCourseRegistry() {
        return courseRegistry;
    }

    public PlayerTracker getPlayerTracker() {
        return playerTracker;
    }

    public CourseBuilder getCourseBuilder() {
        return courseBuilder;
    }

    public Logger getLog() {
        return log;
    }
}
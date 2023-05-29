package net.direskies.direparkour.registries;

import net.direskies.direparkour.ParkourPlugin;
import net.direskies.direparkour.listeners.PlayerTracker;
import net.direskies.direparkour.model.Course;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CourseRegistry {

    private final ParkourPlugin plugin;
    private final Map<String, Course> courses;
    private final File registryFile;

    public CourseRegistry(ParkourPlugin plugin) {
        this.plugin = plugin;
        this.courses = new HashMap<>();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.registryFile = new File(plugin.getDataFolder(), "courses.yml");
        if (!registryFile.exists()) {
            try {
                registryFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        load();
    }

    private void load() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(registryFile);

        Logger logger = plugin.getLogger();

        //Load saved courses.
        logger.info("Loading courses...");
        Set<String> courseKeys = config.getKeys(false);
        if (courseKeys.size() == 0) {
            logger.info("No courses to load!");
            return;
        }

        for (String courseName : courseKeys) {
            ConfigurationSection courseSection = config.getConfigurationSection(courseName);
            if (courseSection == null) continue;
            String worldNamespacedKeyString = courseSection.getString("world");
            if (worldNamespacedKeyString == null) continue;
            NamespacedKey worldNamespacedKey = NamespacedKey.fromString(worldNamespacedKeyString);
            if (worldNamespacedKey == null) continue;
            World world = Bukkit.getWorld(worldNamespacedKey);
            if (world == null) continue;
            List<Map<?, ?>> serializedCheckpoints = courseSection.getMapList("checkpoints");
            BlockVector[] checkpoints = serializedCheckpoints.stream()
                    .map(m -> BlockVector.deserialize((Map<String, Object>) m))
                    .toArray(BlockVector[]::new);
            if (checkpoints.length == 0) continue;
            List<String> rewardCommands = courseSection.getStringList("rewards");


            courses.put(courseName, new Course(courseName, world, checkpoints, rewardCommands.toArray(String[]::new)));
            logger.info("Loaded course named: " + courseName);
            logger.info("Course: " + courseName + " has " + rewardCommands.size() + " rewards!");
        }
        logger.info("Loaded "+courses.size()+" courses!");
    }

    public void save() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(registryFile);

        //Clear saved courses.
        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }

        //Save all courses.
        for (Course course : courses.values()) {
            ConfigurationSection courseSection = config.createSection(course.getName());
            courseSection.set("world", course.getWorld().getKey().toString());
            courseSection.set("checkpoints", Arrays.stream(course.getCheckpoints()).map(BlockVector::serialize).collect(Collectors.toList()));
            courseSection.set("rewards", course.getRewardCommands());
        }

        try {
            config.save(registryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(@NotNull Course course) {
        courses.put(course.getName(), course);
        save();
    }

    public void unregister(@NotNull Course course) {
        courses.remove(course.getName());
        PlayerTracker playerCourseer = plugin.getPlayerTracker();
        for (Player player : playerCourseer.getPlaying(course)) {
            playerCourseer.quit(player);
        }
        save();
    }

    /**
     * Gets the parkour course by their name.
     * @param name The name of the parkour course
     * @return The course if found. Null otherwise.
     */
    public Course getCourseByName(String name) {
        for (Course course : courses.values()) {
            if (!course.getName().equalsIgnoreCase(name)) continue;
            return course;
        }
        return null;
    }

    public Course getCourseByCheckpoint(@NotNull BlockVector position) {
        for (Course course : courses.values()) {
            for (BlockVector checkpoint : course.getCheckpoints()) {
                if (!checkpoint.equals(position)) continue;
                return course;
            }
        }
        return null;
    }

    public Course getCourseByStartPosition(@NotNull BlockVector position) {
        for (Course course : courses.values()) {
            if (!course.getStartLocation().equals(position)) continue;
            return course;
        }
        return null;
    }

    public Collection<Course> getCourses() {
        return courses.values();
    }
}
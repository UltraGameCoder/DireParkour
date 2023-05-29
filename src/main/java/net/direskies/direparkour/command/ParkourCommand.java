package net.direskies.direparkour.command;

import net.direskies.direparkour.CourseBuilder;
import net.direskies.direparkour.Main;
import net.direskies.direparkour.PlayerTracker;
import net.direskies.direparkour.model.Course;
import net.direskies.direparkour.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ParkourCommand implements TabExecutor {

    private static final List<String> COMMANDS_USER = Arrays.asList("info", "quit");
    private static final List<String> COMMANDS_ADMIN = Arrays.asList("info", "quit", "list", "setup", "delete");
    private static final List<String> SETUP_COMMANDS = Arrays.asList("start", "finish", "cancel");

    private final Main plugin;

    public ParkourCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Locale.GENERAL_CMD_NOT_PLAYER.msg());
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            player.sendMessage(Locale.CMD_DIREPARKOUR_INFO.msg());
            return true;
        }

        if (args[0].equalsIgnoreCase("quit")) {
            PlayerTracker playerCourseer = plugin.getPlayerTracker();
            if (playerCourseer.isPlaying(player)) {
                playerCourseer.quit(player);
            } else {
                player.sendMessage(Locale.PARKOUR_NOT_PLAYING.msg());
            }
            return true;
        } else if (args[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("direparkour.admin")) {
                player.sendMessage(Locale.CMD_NO_PERMISSION.msg());
                return true;
            }

            // List all courses here
            Collection<Course> courses = plugin.getCourseRegistry().getCourses();
            if (courses.size() == 0) {
                player.sendMessage(Locale.CMD_DIREPARKOUR_LIST_EMTPY.msg());
            } else {
                player.sendMessage(Locale.CMD_DIREPARKOUR_LIST.msg());
                for (Course course : courses) {
                    String courseName = course.getName();
                    int checkpoints = course.getCheckpoints().length;
                    String worldName = course.getWorld().getName();
                    player.sendMessage(Locale.CMD_DIREPARKOUR_LIST_ENTRY.msg(courseName, String.valueOf(checkpoints), worldName));
                }
            }

            return true;
        } else if (args[0].equalsIgnoreCase("setup")) {
            if (!player.hasPermission("direparkour.admin")) {
                player.sendMessage(Locale.CMD_NO_PERMISSION.msg());
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_MISSING_ARGUMENT.msg());
                return true;
            }

            CourseBuilder courseBuilder = plugin.getCourseBuilder();
            if (args[1].equalsIgnoreCase("start")) {

                if (courseBuilder.isBuilding(player)) {
                    player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_IN_PROGRESS.msg());
                    return true;
                }

                courseBuilder.startBuild(player);
                player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_START.msg());

            } else if (args[1].equalsIgnoreCase("finish")) {

                if (!courseBuilder.isBuilding(player)) {
                    player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_NOT_IN_PROGRESS.msg());
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_FINISH_MISSING_COURSENAME.msg());
                    return true;
                }

                courseBuilder.finishBuild(player, args[2]);

            } else if (args[1].equalsIgnoreCase("cancel")) {
                if (!courseBuilder.isBuilding(player)) {
                    player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_NOT_IN_PROGRESS.msg());
                    return true;
                }
                courseBuilder.cancelBuild(player);
            }
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (!player.hasPermission("direparkour.admin")) {
                player.sendMessage(Locale.CMD_NO_PERMISSION.msg());
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(Locale.CMD_DIREPARKOUR_DELETE_SPECIFY_COURSE.msg());
                return true;
            }

            Course course = plugin.getCourseRegistry().getCourseByName(args[1]);
            if (course == null) {
                player.sendMessage(Locale.COURSE_NOT_EXIST.msg(args[1]));
                return true;
            }

            // Your code for deleting the course...
            plugin.getCourseRegistry().unregister(course);
            player.sendMessage(Locale.CMD_DIREPARKOUR_DELETE.msg(course.getName()));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("direparkour.admin")) {
            if (args.length == 1) {
                StringUtil.copyPartialMatches(args[0], COMMANDS_USER, completions);
            }
        } else {
            if (args.length == 1) {
                StringUtil.copyPartialMatches(args[0], COMMANDS_ADMIN, completions);
            } else if (args.length == 2 && args[0].equalsIgnoreCase("setup")) {
                StringUtil.copyPartialMatches(args[1], SETUP_COMMANDS, completions);
            } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
                StringUtil.copyPartialMatches(args[1], plugin.getCourseRegistry().getCourses().stream().map(Course::getName).collect(Collectors.toList()), completions);
            }
        }
        return completions;
    }
}

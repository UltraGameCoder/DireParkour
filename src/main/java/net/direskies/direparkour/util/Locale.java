package net.direskies.direparkour.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Locale {
    //COLOR_GOOD("#32CD32"),
    //COLOR_BAD("#9B111E"),
    //COLOR_HIGHLIGHT("#CCFF00"),

    PARKOUR_ENTER("<color:#32CD32>You've started the <color:#CCFF00><0> <color:#32CD32>parkour!"),
    PARKOUR_COMPLETED("<color:#32CD32>You've completed the <color:#CCFF00><0> <color:#32CD32>parkour! <color:#808080>[<color:#CCFF00><1><color:#808080>]"),
    PARKOUR_QUIT("<color:#9B111E>You've left the <color:#CCFF00><0> <color:#9B111E>parkour!"),
    GENERAL_ENABLE_FLIGHT("<color:#9B111E>You're not allowed to fly while doing parkour!"),
    PARKOUR_NOT_PLAYING("<color:#9B111E>You're not playing any parkour course."),
    PARKOUR_CHECKPOINT_REACHED("<color:#32CD32>You've reached the <color:#CCFF00>#<0> <color:#32CD32>checkpoint! <color:#808080>[<color:#CCFF00><1><color:#808080>]"),
    PARKOUR_CHECKPOINT_WRONG("<color:#9B111E>This is not your next checkpoint."),
    GENERAL_CMD_NOT_PLAYER("<color:#9B111E>You must be a player to execute this command."),
    COURSE_NOT_EXIST("<color:#9B111E>There's no course called <0>!"),
    CMD_DIREPARKOUR_INFO("""
            <color:#CCFF00>Available DireParkour commands:
            <color:#CCFF00>/direparkour quit <color:#808080>- Quit the current parkour course.
            <color:#CCFF00>/direparkour list <color:#808080>- Lists all courses
            <color:#CCFF00>/direparkour setup start <color:#808080>- Starts setup for a new course
            <color:#CCFF00>/direparkour setup finish <courseName> <color:#808080>- Finishes setup for a new course
            <color:#CCFF00>/direparkour setup cancel <color:#808080>- Cancels the setup for a new course
            <color:#CCFF00>/direparkour delete <courseName> <color:#808080>- Deletes a course
            """),
    CMD_DIREPARKOUR_LIST("<color:#CCFF00>Course List:"),
    CMD_DIREPARKOUR_LIST_EMTPY("<color:#808080>No courses to list!"),
    CMD_DIREPARKOUR_LIST_ENTRY("<color:#808080>- Course Name: <color:#CCFF00><0> <color:#808080>| Checkpoints: <color:#CCFF00><1> <color:#808080>| World: <color:#CCFF00><2>"),
    CMD_DIREPARKOUR_SETUP_MISSING_ARGUMENT("<color:#9B111E>Missing argument. Use /direparkour setup <start/cancel/finish>."),
    CMD_DIREPARKOUR_SETUP_START("<color:#32CD32>You've started building a new course."),
    CMD_DIREPARKOUR_SETUP_FINISH_MISSING_COURSENAME("<color:#9B111E>Missing argument. Use /direparkour setup finish <courseName>."),
    CMD_DIREPARKOUR_SETUP_FINISH_INVALID_CHECKPOINT_COUNT("<color:#9B111E>A course must have at least 2 checkpoints."),
    CMD_DIREPARKOUR_SETUP_FINISH_COURSENAME_INVALID("<color:#9B111E>Invalid course name. Please only use up to 32 alphanumeric characters."),
    CMD_DIREPARKOUR_SETUP_FINISH_COURSENAME_TAKEN("<color:#9B111E>The course name <color:#CCFF00><0> <color:#9B111E>is already used."),
    CMD_DIREPARKOUR_SETUP_NOT_IN_PROGRESS("<color:#9B111E>You're currently not building a new course."),
    CMD_DIREPARKOUR_SETUP_IN_PROGRESS("<color:#9B111E>You're already building a new course."),
    CMD_DIREPARKOUR_SETUP_CHECKPOINT_ADDED("<color:#32CD32>You're added the <color:#CCFF00>#<0> <color:#32CD32> checkpoint!"),
    CMD_DIREPARKOUR_SETUP_CHECKPOINT_ALREADY("<color:#32CD32>You're already added this checkpoint!"),
    CMD_DIREPARKOUR_SETUP_CHECKPOINT_REMOVED("<color:#32CD32>You're removed the <color:#CCFF00>#<0> <color:#32CD32> checkpoint!"),
    CMD_DIREPARKOUR_SETUP_FINISH("<color:#32CD32>You've finished building a new course with name <color:#CCFF00><0>."),
    CMD_DIREPARKOUR_SETUP_CANCEL("<color:#9B111E>You've cancelled building a new course."),
    CMD_NO_PERMISSION("<color:#9B111E>You do not have permission to execute this DireParkour command!"),
    CMD_DIREPARKOUR_DELETE_SPECIFY_COURSE("<color:#9B111E>You must specify a course to delete!"),
    CMD_DIREPARKOUR_DELETE("<color:#32CD32>You've deleted a course with name <color:#CCFF00><0>."),
    ITEM_COURSE_WAND("<gradient:#D74826:#FD7F2C>Course Tracer"),
    ITEM_COURSE_WAND_LORE("""
            <color:#808080>An wand used to select checkpoints.
            <color:#CCFF00>Left-click <color:#808080>a pressure plate to remove a checkpoint
            <color:#CCFF00>Right-click <color:#808080>a pressure to select a new checkpoint
            """);
    private static final MiniMessage serializer = MiniMessage.miniMessage();
    private final String raw;

    Locale(String raw) {
        this.raw = raw;
    }

    public @NotNull String raw() {
        return raw;
    }

    public @NotNull Component msg() {
        return serializer.deserialize(raw)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private TagResolver[] getResolvers(String... args) {
        TagResolver[] resolvers = new TagResolver[args.length];

        for (int i = 0; i < args.length; i++) {
            resolvers[i] = Placeholder.parsed(""+i, args[i]);
        }

        return resolvers;
    }

    private TagResolver[] getResolvers(ComponentLike... args) {
        TagResolver[] resolvers = new TagResolver[args.length];

        for (int i = 0; i < args.length; i++) {
            resolvers[i] = Placeholder.component(""+i, args[i]);
        }

        return resolvers;
    }

    public @NotNull Component msg(String... args) {
        return serializer.deserialize(raw, getResolvers(args))
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public @NotNull Component msg(ComponentLike... args) {
        return serializer.deserialize(raw, getResolvers(args));
    }

    public @NotNull List<Component> msgMultiline() {
        return Arrays.stream((raw).split("\\n"))
                .map(serializer::deserialize)
                .map(component -> component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .collect(Collectors.toList());
    }

    public @NotNull List<Component> msgMultiline(String... args) {
        TagResolver[] resolvers = getResolvers(args);
        return Arrays.stream(raw.split("\\n"))
                .map(line -> serializer.deserialize(line, resolvers))
                .map(component -> component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .collect(Collectors.toList());
    }

    public @NotNull List<Component> msgMultiline(ComponentLike... args) {
        TagResolver[] resolvers = getResolvers(args);
        return Arrays.stream(raw.split("\\n"))
                .map(line -> serializer.deserialize(line, resolvers))
                .map(component -> component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .collect(Collectors.toList());
    }
}

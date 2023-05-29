package net.direskies.direparkour.listeners;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import net.direskies.direparkour.model.Course;
import net.direskies.direparkour.model.CourseProgress;
import net.direskies.direparkour.registries.CourseRegistry;
import net.direskies.direparkour.util.FormatUtil;
import net.direskies.direparkour.util.Locale;
import net.direskies.direparkour.util.MaterialUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class PlayerTracker implements Listener {

    private final CourseRegistry courseRegistry;
    private final Map<Player, CourseProgress> tracking;

    public PlayerTracker(CourseRegistry courseRegistry) {
        this.courseRegistry = courseRegistry;
        this.tracking = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerStart(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        if (event.getClickedBlock() == null || !MaterialUtil.PRESSURE_PLATES.contains(event.getClickedBlock().getType())) return;
        BlockVector position = event.getClickedBlock().getLocation().toVector().toBlockVector();
        CourseProgress progress = tracking.get(event.getPlayer());
        if (progress != null) {
            int detected = progress.getCourse().getCheckpoint(position);
            if (detected >= 0) {
                event.setCancelled(true);
                if (progress.getCheckpoint() + 1 == detected) {
                    //Increases checkpoint if next checkpoint is reached.
                    progress.setCheckpoint(detected);
                    progress.setLastYaw(event.getPlayer().getLocation().getYaw());
                    if (detected == progress.getCourse().getCheckpoints().length-1) {
                        tracking.remove(event.getPlayer());

                        String[] rewardCommands = progress.getCourse().getRewardCommands();
                        if (rewardCommands.length > 0) {
                            ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
                            for (String rewardCommand : rewardCommands) {
                                rewardCommand = rewardCommand.replaceAll("<player>", event.getPlayer().getName());
                                rewardCommand = rewardCommand.replaceAll("<course>", progress.getCourse().getName());
                                Bukkit.dispatchCommand(consoleSender, rewardCommand);
                            }
                        }
                        event.getPlayer().sendMessage(Locale.PARKOUR_COMPLETED.msg(progress.getCourse().getName(), FormatUtil.formatDurationBetween(progress.getStartTime(), Instant.now())));
                    } else {
                        event.getPlayer().sendMessage(Locale.PARKOUR_CHECKPOINT_REACHED.msg("" + detected, FormatUtil.formatDurationBetween(progress.getStartTime(), Instant.now())));
                    }
                } else if (progress.getCheckpoint() != detected){
                    event.getPlayer().sendMessage(Locale.PARKOUR_CHECKPOINT_WRONG.msg());
                }
                return;
            }
        }
        Course course = courseRegistry.getCourseByStartPosition(position);
        if (course == null) return;
        event.setCancelled(true);
        //The player started a new track.
        event.getPlayer().setFlying(false);
        CourseProgress oldProgress = tracking.put(event.getPlayer(), new CourseProgress(course, event.getPlayer().getLocation().getYaw()));
        if (oldProgress != null) {
            event.getPlayer().sendMessage(Locale.PARKOUR_QUIT.msg(oldProgress.getCourse().getName()));
        }
        event.getPlayer().sendMessage(Locale.PARKOUR_ENTER.msg(course.getName()));
    }

    @EventHandler
    public void onFlyToggle(PlayerToggleFlightEvent event) {
        if (!isPlaying(event.getPlayer())) return;
        if (!event.isFlying()) return;
        quit(event.getPlayer());
        event.getPlayer().sendMessage(Locale.GENERAL_ENABLE_FLIGHT.msg());
    }

    @EventHandler
    public void onFlyToggle(PlayerElytraBoostEvent event) {
        if (!isPlaying(event.getPlayer())) return;
        quit(event.getPlayer());
        event.getPlayer().sendMessage(Locale.GENERAL_ENABLE_FLIGHT.msg());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getY() <= event.getTo().getY()) return;

        CourseProgress progress = tracking.get(event.getPlayer());
        if (progress == null) return;

        BlockVector currentCheckpoint = progress.getCourse().getCheckpoint(progress.getCheckpoint());
        if (currentCheckpoint == null) return;

        int currentCheckpointY = currentCheckpoint.getBlockY();
        double currentY = event.getTo().getY();

        if (currentCheckpointY - currentY > 1.51) {
            Location teleportLocation = new Location(event.getPlayer().getWorld(), currentCheckpoint.getX()+0.5, currentCheckpoint.getY()+0.01, currentCheckpoint.getZ()+0.5, progress.getLastYaw(), 0);
            event.getPlayer().teleport(teleportLocation);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerSwitchWorld(PlayerChangedWorldEvent event) {
        quit(event.getPlayer());
    }

    public Collection<Player> getPlaying(@NotNull Course course) {
        Set<Player> playing = new HashSet<>();
        for (Map.Entry<Player, CourseProgress> entry : tracking.entrySet()) {
            if (!course.getName().equals(entry.getValue().getCourse().getName())) continue;
            playing.add(entry.getKey());
        }
        return playing;
    }

    public @Nullable CourseProgress getProgressOf(@NotNull Player player) {
        return tracking.get(player);
    }

    public void quit(@NotNull Player player) {
        CourseProgress oldProgress = tracking.remove(player);
        if (oldProgress != null) {
            player.sendMessage(Locale.PARKOUR_QUIT.msg(oldProgress.getCourse().getName()));
        }
    }

    public boolean isPlaying(Player player) {
        return tracking.containsKey(player);
    }
}

package net.direskies.direparkour;

import net.direskies.direparkour.model.Course;
import net.direskies.direparkour.registries.CourseRegistry;
import net.direskies.direparkour.util.Locale;
import net.direskies.direparkour.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

public class CourseBuilder implements Listener {

    private final CourseRegistry courseRegistry;
    private final Map<Player, Stack<Block>> builders;

    private final ItemStack trackWand;

    public CourseBuilder(CourseRegistry courseRegistry) {
        this.courseRegistry = courseRegistry;
        this.builders = new HashMap<>();

        this.trackWand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = trackWand.getItemMeta();
        meta.displayName(Locale.ITEM_COURSE_WAND.msg());
        meta.lore(Locale.ITEM_COURSE_WAND_LORE.msgMultiline());
        meta.setUnbreakable(true);
        trackWand.setItemMeta(meta);
    }

    @EventHandler
    public void onWandInteraction(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (!MaterialUtil.PRESSURE_PLATES.contains(event.getClickedBlock().getType())) return;
        if (!trackWand.isSimilar(event.getItem())) return;
        if (event.getAction().isLeftClick()) {
            //Remove the clicked checkpoint.
            List<Block> checkpoints = builders.get(event.getPlayer());
            if (checkpoints == null) {
                event.getPlayer().sendMessage(Locale.CMD_DIREPARKOUR_SETUP_NOT_IN_PROGRESS.msg());
                return;
            }
            int checkpoint = checkpoints.indexOf(event.getClickedBlock());
            if (checkpoint == -1) return;
            if (checkpoints.remove(event.getClickedBlock())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Locale.CMD_DIREPARKOUR_SETUP_CHECKPOINT_REMOVED.msg(String.valueOf(checkpoint)));
            }
        } else if (event.getAction().isRightClick()) {
            //Add the clicked checkpoint.
            List<Block> checkpoints = builders.get(event.getPlayer());
            if (checkpoints == null) {
                event.getPlayer().sendMessage(Locale.CMD_DIREPARKOUR_SETUP_NOT_IN_PROGRESS.msg());
                return;
            }

            if (checkpoints.contains(event.getClickedBlock())) {
                event.getPlayer().sendMessage(Locale.CMD_DIREPARKOUR_SETUP_CHECKPOINT_ALREADY.msg());
                return;
            }

            Optional<Block> startBlock = checkpoints.stream().findFirst();
            if (startBlock.isPresent() && !startBlock.get().getWorld().equals(event.getClickedBlock().getWorld())) return;

            checkpoints.add(event.getClickedBlock());
            event.getPlayer().sendMessage(Locale.CMD_DIREPARKOUR_SETUP_CHECKPOINT_ADDED.msg(String.valueOf(checkpoints.size() - 1)));
        }
    }

    public void startBuild(@NotNull Player player) {
        builders.put(player, new Stack<>());
        int emptySlot = player.getInventory().firstEmpty();
        if (emptySlot != -1) {
            player.getInventory().setItem(emptySlot, player.getInventory().getItemInMainHand());
        }
        player.getInventory().setItemInMainHand(trackWand);
    }

    public Set<Player> getBuilders() {
        return builders.keySet();
    }

    public boolean isBuilding(Player player) {
        return builders.containsKey(player);
    }

    public void cancelBuild(@NotNull Player player) {
        Stack<Block> checkpoints = builders.remove(player);
        if (checkpoints != null) {
            player.getInventory().remove(trackWand);
            player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_CANCEL.msg());
        }
    }

    public boolean finishBuild(@NotNull Player player, @NotNull String name) {
        List<Block> checkpoints = builders.get(player);
        if (checkpoints == null) {
            player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_NOT_IN_PROGRESS.msg());
            return false;
        }

        if (!Pattern.matches("[A-Za-z0-9]{1,32}", name)) {
            player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_FINISH_COURSENAME_INVALID.msg());
            return false;
        }

        if (courseRegistry.getCourseByName(name) != null) {
            player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_FINISH_COURSENAME_TAKEN.msg(name));
            return false;
        }

        if (checkpoints.size() < 2) {
            player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_FINISH_INVALID_CHECKPOINT_COUNT.msg());
            return false;
        }

        builders.remove(player);

        World world = checkpoints.get(0).getWorld();
        BlockVector[] checkpointPositions = checkpoints.stream().map(block -> block.getLocation().toVector().toBlockVector()).toArray(BlockVector[]::new);
        courseRegistry.register(new Course(name, world, checkpointPositions, new String[0]));

        player.sendMessage(Locale.CMD_DIREPARKOUR_SETUP_FINISH.msg(name));

        player.getInventory().remove(trackWand);
        return true;
    }
}

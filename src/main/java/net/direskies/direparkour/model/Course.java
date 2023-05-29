package net.direskies.direparkour.model;

import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Course {

    private final String name;
    private final World world;
    private final BlockVector[] checkpoints;
    private final String[] rewardCommands;

    public Course(@NotNull String name,
                  @NotNull World world,
                  @NotNull BlockVector[] checkpoints,
                  String[] rewardCommands) {
        this.name = name;
        this.world = world;
        if (checkpoints.length == 1) throw new IllegalStateException("Course " + name + " needs at least 2 checkpoints!");
        this.checkpoints = checkpoints;
        this.rewardCommands = rewardCommands;
    }

    public int getCheckpoint(@NotNull BlockVector position) {
        for (int i = 0; i < checkpoints.length; i++) {
            if (!checkpoints[i].equals(position)) continue;
            return i;
        }
        return -1;
    }

    public @Nullable BlockVector getCheckpoint(int index) {
        if (index >= checkpoints.length) return null;
        return checkpoints[index];
    }

    public @NotNull BlockVector getStartLocation() {
        return checkpoints[0];
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return world;
    }

    public BlockVector[] getCheckpoints() {
        return checkpoints;
    }

    public String[] getRewardCommands() {
        return rewardCommands;
    }
}

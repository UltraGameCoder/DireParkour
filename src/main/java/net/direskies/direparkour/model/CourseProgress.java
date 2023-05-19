package net.direskies.direparkour.model;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class CourseProgress {

    private final Course course;
    private int checkpoint;
    private final Instant startTime;
    private float lastYaw;

    public CourseProgress(@NotNull Course course, float startYaw) {
        this.course = course;
        this.checkpoint = 0;
        this.startTime = Instant.now();
        this.lastYaw = startYaw;
    }

    public @NotNull Course getCourse() {
        return course;
    }

    public int getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(int checkpoint) {
        this.checkpoint = checkpoint;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public float getLastYaw() {
        return lastYaw;
    }

    public void setLastYaw(float lastYaw) {
        this.lastYaw = lastYaw;
    }
}

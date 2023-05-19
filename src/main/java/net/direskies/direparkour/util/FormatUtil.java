package net.direskies.direparkour.util;

import java.time.Duration;
import java.time.Instant;

public class FormatUtil {

    public static String formatDurationBetween(Instant start, Instant end) {
        Duration duration = Duration.between(start, end);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        long millis = duration.minusMinutes(minutes).minusSeconds(seconds).toMillis();
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }
}

package de.minestar.nightwatch.util;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to format and parse {@link Duration} to and from human readable formats
 */
public class DurationUtil {

    private DurationUtil() {

    }

    /**
     * Format the duration to a readable string with the format 'xh ym zs', where x, y and z the amount of hours, minutes and seconds. If one of them
     * is 0, it is not printed.
     * 
     * @param duration
     *            The duration to format
     * @return A formatted, trimmed string
     */
    public static String format(Duration duration) {
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        long seconds = duration.getSeconds();

        StringBuilder sBuilder = new StringBuilder();
        if (hours != 0)
            sBuilder.append(hours).append("h ");
        if (minutes != 0)
            sBuilder.append(minutes).append("m ");
        if (seconds != 0)
            sBuilder.append(seconds).append("s ");

        return sBuilder.toString().trim();
    }

    private static final Pattern HOUR_REGEX = Pattern.compile("\\d{1,2}h", Pattern.CASE_INSENSITIVE);
    private static final Pattern MINUTE_REGEX = Pattern.compile("\\d{1,2}m", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECOND_REGEX = Pattern.compile("\\d{1,2}s", Pattern.CASE_INSENSITIVE);

    /**
     * Parse the string, which format is 'xh ym zs' (case insensitive, the order is ignored and also the spaces) and converts it to a {@link Duration}
     * 
     * @param s
     *            The string to parse
     * @return A duration. If the string is empty or no hours nor seconds nor minutes are set, returns {@link Duration#ZERO}
     */
    public static Duration parse(String s) {

        Duration result = Duration.ZERO;
        if (s.isEmpty())
            return result;

        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        // Parse hours
        hours = parse(HOUR_REGEX, s);
        minutes = parse(MINUTE_REGEX, s);
        seconds = parse(SECOND_REGEX, s);

        if (hours != 0)
            result = result.plusHours(hours);
        if (minutes != 0)
            result = result.plusMinutes(minutes);
        if (seconds != 0)
            result = result.plusSeconds(seconds);

        return result;
    }

    private static int parse(Pattern regex, String input) {
        Matcher matcher = regex.matcher(input);
        if (matcher.find()) {
            String group = matcher.group();
            // Delete last sign, the H
            group = group.substring(0, group.length() - 1);
            return Integer.valueOf(group);
        } else
            return 0;
    }

}

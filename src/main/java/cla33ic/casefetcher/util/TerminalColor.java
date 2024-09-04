package cla33ic.casefetcher.util;

public class TerminalColor {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String LIGHT_BLUE = "\u001B[94m";
    public static final String PINK = "\u001B[95m";
    public static final String GOLD = "\u001B[93m";

    public static String colorize(String text, String color) {
        return color + text + RESET;
    }
}
package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Css {

    private Css() {}

    private static final Pattern INT = Pattern.compile("\\d+");

    public static int[] rgb(String cssColor) {
        if (cssColor == null) {
            throw new IllegalArgumentException("cssColor is null");
        }
        String s = cssColor.trim().toLowerCase();

        if (s.isEmpty() || s.contains("transparent")) {
            throw new IllegalArgumentException("Cannot parse color: '" + cssColor + "'");
        }

        List<Integer> nums = new ArrayList<>(3);
        Matcher m = INT.matcher(s);
        while (m.find() && nums.size() < 3) {
            nums.add(Integer.parseInt(m.group()));
        }

        if (nums.size() < 3) {
            throw new IllegalArgumentException("Cannot parse rgb/rgba from: '" + cssColor + "'");
        }

        return new int[]{ nums.get(0), nums.get(1), nums.get(2) };
    }

    public static boolean isGreyish(int[] rgb, int min, int max) {
        if (rgb == null || rgb.length < 3) return false;

        int r = rgb[0], g = rgb[1], b = rgb[2];
        if (r < min || r > max) return false;
        if (g < min || g > max) return false;
        if (b < min || b > max) return false;

        int diffRG = Math.abs(r - g);
        int diffRB = Math.abs(r - b);
        int diffGB = Math.abs(g - b);

        return diffRG <= 15 && diffRB <= 15 && diffGB <= 15;
    }

    public static boolean isBlueish(int[] rgb) {
        if (rgb == null || rgb.length < 3) return false;

        int r = rgb[0], g = rgb[1], b = rgb[2];

        return b >= 110 && b > r + 20 && b > g + 20;
    }


    public static String fmt(int[] rgb) {
        if (rgb == null || rgb.length < 3) return "rgb(?)";
        return "rgb(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")";
    }
}

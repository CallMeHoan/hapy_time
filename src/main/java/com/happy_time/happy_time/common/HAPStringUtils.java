package com.happy_time.happy_time.common;

import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class HAPStringUtils {
    private static final String EMPTY = "";

    /**
     * <p>Removes diacritics (~= accents) from a string. The case will not be altered.</p>
     * <p>For instance, '&agrave;' will be replaced by 'a'.</p>
     * <p>Note that ligatures will be left as is.</p>
     *
     * <pre>
     * OMIStringUtils.stripAccents(null)                = null
     * OMIStringUtils.stripAccents("")                  = ""
     * OMIStringUtils.stripAccents("control")           = "control"
     * OMIStringUtils.stripAccents("&eacute;clair")     = "eclair"
     * </pre>
     *
     * @param input String to be stripped
     * @return input text with diacritics removed
     */
    public static String stripAccents(final String input) {
        if (input == null) {
            return null;
        }
        final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");//$NON-NLS-1$
        final StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
        convertRemainingAccentCharacters(decomposed);
        // Note that this doesn't correctly remove ligatures...
        return pattern.matcher(decomposed).replaceAll(EMPTY).replaceAll("Đ", "D").replaceAll("đ", "d");
    }

    public static int compareIgnoreCaseAndAccents(final String s1, final String s2) {
        if(s1 == null || s2 == null) {
            return -1;
        }
        return StringUtils.stripAccents(s1.trim().toLowerCase()).compareTo(StringUtils.stripAccents(s2.trim().toLowerCase()));
    }

    private static void convertRemainingAccentCharacters(final StringBuilder decomposed) {
        for (int i = 0; i < decomposed.length(); i++) {
            if (decomposed.charAt(i) == '\u0141') {
                decomposed.deleteCharAt(i);
                decomposed.insert(i, 'L');
            } else if (decomposed.charAt(i) == '\u0142') {
                decomposed.deleteCharAt(i);
                decomposed.insert(i, 'l');
            }
        }
    }

    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return startsWith(str, prefix, true);
    }


    public static boolean startsWith(String str, String prefix, boolean ignoreCase) {
        if (str == null || prefix == null) {
            return (str == null && prefix == null);
        }
        if (prefix.length() > str.length()) {
            return false;
        }
        return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
    }



}

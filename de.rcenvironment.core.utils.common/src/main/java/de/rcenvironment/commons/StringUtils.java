/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.commons;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Utility class for {@link String} objects.
 * 
 * @author Doreen Seider
 */
public final class StringUtils {

    /** Separator used to separate two semantically different Strings put into an single one. */
    public static final String SEPARATOR = ":";

    /** Character used to escape the separator. */
    public static final String ESCAPE_CHARACTER = "\\";

    private StringUtils() {}

    /**
     * Escapes the separator within the given String object.
     * @param rawString The {@link String} to escape.
     * @return the escaped {@link String}.
     */
    public static String escapeSeparator(String rawString) {
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(rawString);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == SEPARATOR.toCharArray()[0]) {
                result.append(ESCAPE_CHARACTER);
                result.append(SEPARATOR);
            } else {
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }
    
    /**
     * Replaces in the given {@link String} escaped separator with the separator itself.
     * @param escapedString The {@link String} to unescape.
     * @return the unescaped {@link String}.
     */
    public static String unescapeSeparator(String escapedString) {
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(escapedString);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == ESCAPE_CHARACTER.toCharArray()[0]) {
                character = iterator.next();
                if (character == SEPARATOR.toCharArray()[0]) {
                    result.append(SEPARATOR);
                } else {
                    result.append(ESCAPE_CHARACTER);
                    result.append(character);
                }
            } else {
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }
    
    /**
     * Splits the given {@link String} around the separator.
     * @param completeString the {@link String} to split.
     * @return the splitted String as array.
     */
    public static String[] split(String completeString) {
        return split(completeString, SEPARATOR);
    }

    /**
     * Splits the given {@link String} around the separator.
     * @param completeString the {@link String} to split.
     * @param separator the {@link String} to use as separator
     * @return the splitted String as array.
     */
    public static String[] split(final String completeString, final String separator) {
        return split(completeString, separator, false);
    }

    /**
     * Splits the given {@link String} around the separator.
     * @param completeString the {@link String} to split.
     * @param separator the {@link String} to use as separator
     * @param trim true, if the parts shall be {@link String#trim()}ed
     * @return the splitted String as array.
     */
    public static String[] split(final String completeString, final String separator, final boolean trim) {
        
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(completeString);
        char character = iterator.current();
        char lastCharacter = character;
        while (character != CharacterIterator.DONE) {
            if (character == separator.toCharArray()[0] && lastCharacter != ESCAPE_CHARACTER.toCharArray()[0]) {
                result.append(separator + separator);
            } else {
                result.append(character);
            }
            lastCharacter = character;
            character = iterator.next();
        }
        
        String[] splitted = result.toString().split(separator + separator);
        if (trim) {
            for (int index = 0; index < splitted.length; ++index) {
                splitted[index] = splitted[index].trim();
            }
        }
        return splitted;
    }
    
    /**
     * Strings the given parts together to one String and escapes separator if needed.
     * @param parts the given String parts which needs to string together.
     * @return The String containing all parts separated by an separator.
     */
    public static String concat(String[] parts) {
        StringBuilder stringBuilder = new StringBuilder();
        
        for (String part : parts) {
            String escapedPart = StringUtils.escapeSeparator(part);
            stringBuilder.append(escapedPart);
            stringBuilder.append(SEPARATOR);
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    /**
     * Returns the given string instance as a non-null reference as result. If the given string
     * reference is a null reference an empty string "" will be returned.
     * 
     * @param text the text
     * @return a valid <code>String</code> instance, an empty string if the parameter was a null
     *         reference
     */
    public static String notNull(final String text) {
        if (text != null) {
            return text;
        } else {
            return "";
        }
    }

    /**
     * Returns the given string instance as a non-null reference as result. If the given string
     * reference is a null reference the default value will be returned.
     * 
     * @param text the text
     * @param defaultValue the default value to return if the text is a null reference
     * @return a valid <code>String</code> instance, an empty string if the parameter was a null
     *         reference
     */
    public static String notNull(final String text, final String defaultValue) {
        if (text != null) {
            return text;
        } else {
            return defaultValue;
        }
    }

}

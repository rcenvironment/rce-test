/*
 * Copyright (C) 2006-2011 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */
 
package de.rcenvironment.rce.components.simplewrapper.commons;


/**
 * Converts values for the configuration.
 * 
 * @author Christian Weiss
 */
public class ConfigurationValueConverter {

    private static final String MATCHER_STRING = "%s";

    protected ConfigurationValueConverter() {
        // do nothing
    }

    /**
     * Converts a byte array to a String representation.
     * 
     * @param content the byte array
     * @return a String representation of the specified byte array
     */
    public static String executableDirectoryContent(final byte[] content) {
        final String contentHexString;
        if (content == null) {
            contentHexString = null;
        } else {
            final StringBuilder contentHexStringBuilder = new StringBuilder();
            for (final byte b : content) {
                final int i128 = 128;
                final int representant = b + i128;
                final String hexString = Integer.toHexString(representant);
                if (hexString.length() < 2) {
                    contentHexStringBuilder.append("0");
                }
                contentHexStringBuilder.append(hexString);
            }
            contentHexString = contentHexStringBuilder.toString();
        }
        return contentHexString;
    }

    /**
     * Converts a String to a byte array.
     * 
     * @param content the String
     * @return a byte array with the content of the specified String
     */
    public static byte[] executableDirectoryContent(final String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        final byte[] contentByteArray = new byte[content.length() / 2];
        for (int contentIndex = 0; contentIndex < content.length(); contentIndex += 2) {
            final String hex = content.substring(contentIndex, contentIndex + 2).trim();
            final int i16 = 16;
            final int i128 = 128;
            final byte b = (byte) (Integer.parseInt(hex, i16) - i128);
            contentByteArray[contentIndex / 2] = b;
        }
        return contentByteArray;
    }

    /**
     * Converts to String -> FileMappings.
     * 
     * @param value the String value
     * @return the FileMappings value
     */
    public static FileMappings getConfiguredMappings(final String value) {
        final FileMappings result = new FileMappings();
        final String mappingString = value;
        if (mappingString != null && !mappingString.isEmpty()) {
            final String[] mappings = mappingString.split(",");
            for (final String mapping : mappings) {
                final int firstSeparator = mapping.indexOf(SimpleWrapperComponentConstants.SUB_SEPARATOR);
                final int secondSeparator = mapping.indexOf(SimpleWrapperComponentConstants.SUB_SEPARATOR, firstSeparator + 1);
                final String direction = mapping.substring(0, firstSeparator);
                final String name = mapping.substring(firstSeparator + 1, secondSeparator);
                final String path = mapping.substring(secondSeparator + 1);
                result.put(direction, name, path);
            }
        }
        return result;
    }

    /**
     * Converts to Map -> String.
     * 
     * @param value the Map value
     * @return the String value
     */
    public static String getConfiguredMappings(final FileMappings value) {
        final String fileMappingPattern = MATCHER_STRING + SimpleWrapperComponentConstants.SUB_SEPARATOR
            + MATCHER_STRING + SimpleWrapperComponentConstants.SUB_SEPARATOR + MATCHER_STRING;
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (final String direction : new String[] { "Input", "Output" }) {
            for (final String name : value.getNames(direction)) {
                if (first) {
                    first = false;
                } else {
                    builder.append(",");
                }
                final String path = value.getPath(direction, name);
                builder.append(String.format(fileMappingPattern, direction, name, path));
            }
        }
        final String result = builder.toString();
        return result;
    }

}

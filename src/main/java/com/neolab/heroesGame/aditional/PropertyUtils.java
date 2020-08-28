package com.neolab.heroesGame.aditional;

import java.util.Properties;

public final class PropertyUtils {

    private PropertyUtils() throws Exception {
        throw new Exception();
    }

    public static Integer getIntegerFromProperty(final Properties prop, final String str) {
        return Integer.parseInt(prop.getProperty(str));
    }

    public static Float getFloatFromProperty(final Properties prop, final String str) {
        return Float.parseFloat(prop.getProperty(str));
    }
}

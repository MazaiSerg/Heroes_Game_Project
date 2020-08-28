package com.neolab.heroesGame.aditional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class HeroConfigManager {

    private static final Properties HERO_CONFIG = new Properties();
    private static boolean isLoadHeroConfig = false;
    private static final String HERO_CONFIG_FILE_NAME = "src/main/resources/hero.config";
    private static final String HERO_CONFIG_FILE_NAME_2 = "resources/hero.properties";

    private HeroConfigManager() throws Exception {
        throw new Exception();
    }

    public static Properties getHeroConfig() throws IOException {
        if (!isLoadHeroConfig) {
            loadConfig();
        }
        return HERO_CONFIG;
    }

    private static void loadConfig() throws IOException {
        File test = new File(HERO_CONFIG_FILE_NAME_2);
        final InputStream is;
        if (test.exists()) {
            is = new FileInputStream(HERO_CONFIG_FILE_NAME_2);
        } else {
            is = new FileInputStream(HERO_CONFIG_FILE_NAME);
        }
        HERO_CONFIG.load(is);
        isLoadHeroConfig = true;
        is.close();
    }
}

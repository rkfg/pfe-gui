package me.rkfg.pfe.gui;

import java.net.MalformedURLException;
import java.net.URL;

import me.rkfg.pfe.SettingsStorage;

public class GUISettingsStorage extends SettingsStorage {

    private static final String UPDATE_URL = "update_url";

    public GUISettingsStorage(Class<?> jarClass) {
        super(jarClass);
    }

    public URL getUpdateURL() {
        try {
            return new URL(properties.getProperty(UPDATE_URL, "http://127.0.0.1/update.txt"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

}

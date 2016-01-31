package me.rkfg.pfe.gui;

import me.rkfg.pfe.AbstractSettingsStorage;

public class PathStorage extends AbstractSettingsStorage {

    private static final String SHARE_PATH = "share_path";
    private static final String DL_PATH = "dl_path";
    private static final String RECENT_PATHS_INI = "recent_paths.ini";

    protected PathStorage() {
        super(PathStorage.class, RECENT_PATHS_INI);
    }

    public String getDownloadPath() {
        return properties.getProperty(DL_PATH, Main.HOME);
    }

    public String getSharePath() {
        return properties.getProperty(SHARE_PATH, Main.HOME);
    }

    public void setDownloadPath(String path) {
        properties.setProperty(DL_PATH, path);
        storeProperties();
    }

    public void setSharePath(String path) {
        properties.setProperty(SHARE_PATH, path);
        storeProperties();
    }
}

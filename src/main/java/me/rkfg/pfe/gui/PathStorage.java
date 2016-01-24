package me.rkfg.pfe.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathStorage {

    private static final String SHARE_PATH = "share_path";
    private static final String DL_PATH = "dl_path";
    private static final String RECENT_PATHS_INI = "recent_paths.ini";
    Properties properties = new Properties();
    Logger log = LoggerFactory.getLogger(getClass());

    public PathStorage() {
        try {
            properties.load(new InputStreamReader(new FileInputStream(new File(RECENT_PATHS_INI)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.info("{} not found, will be created later.", RECENT_PATHS_INI);
        }
    }

    private void storeProperties() {
        try {
            properties.store(new OutputStreamWriter(new FileOutputStream(new File(RECENT_PATHS_INI)), StandardCharsets.UTF_8), "");
        } catch (IOException e) {
            log.info("{} can't be saved, path info will always be default.", RECENT_PATHS_INI);
        }
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

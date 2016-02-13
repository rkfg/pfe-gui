package me.rkfg.pfe.gui.platform;

import me.rkfg.pfe.gui.GUISettingsStorage;

public class UpdaterLinux extends Updater {

    public UpdaterLinux(GUISettingsStorage settingsStorage) {
        super(settingsStorage);
    }

    @Override
    protected String getBinaryFilename() {
        return "pfe-linux" + getArch() + ".jar";
    }

    @Override
    protected String getUpdateFilename(String binaryFilename) {
        return binaryFilename;
    }

    @Override
    protected boolean doUpdate() {
        return false;
    }
}

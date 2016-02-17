package me.rkfg.pfe.gui.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;

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
    protected boolean doUpdate() {
        Path ownPath = getUpdateFilePath(true);
        Path updatePath = getUpdateFilePath();
        try {
            Files.move(updatePath, ownPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Setting permissions for {}", ownPath);
            Files.setPosixFilePermissions(ownPath,
                    EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
                            PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ,
                            PosixFilePermission.OTHERS_EXECUTE));
        } catch (IOException e) {
            log.error("Update failed! ", e);
        }
        return false;
    }

}

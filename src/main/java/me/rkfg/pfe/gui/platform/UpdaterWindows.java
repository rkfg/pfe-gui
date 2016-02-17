package me.rkfg.pfe.gui.platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import me.rkfg.pfe.AbstractSettingsStorage;
import me.rkfg.pfe.gui.GUISettingsStorage;

public class UpdaterWindows extends Updater {

    private static final int DELAY_UPDATE_SEC = 5;

    public UpdaterWindows(GUISettingsStorage settingsStorage) {
        super(settingsStorage);
    }

    @Override
    public void update() {
        getUpdateBatFile().delete();
        super.update();
    }

    @Override
    protected String getBinaryFilename() {
        return "pfe-win" + getArch() + ".exe";
    }

    @Override
    protected boolean doUpdate() {
        try {
            File file = getUpdateBatFile();
            String updateBatPath = file.getAbsolutePath();
            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("cp866"))) {
                String ownPath = getUpdateFilePath(true).toFile().getAbsolutePath();
                String updatePath = getUpdateFilePath().toFile().getAbsolutePath();
                osw.write(String.format("ping -n %d 127.0.0.1 > nul\r\nmove \"%s\" \"%s\"\r\n%s", DELAY_UPDATE_SEC, updatePath, ownPath,
                        ownPath));
            }
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", updateBatPath, ">nul");
            log.info("Starting {}...", updateBatPath);
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private File getUpdateBatFile() {
        File file = new File(AbstractSettingsStorage.getJarDirectory(getClass()), "update.bat");
        return file;
    }
}

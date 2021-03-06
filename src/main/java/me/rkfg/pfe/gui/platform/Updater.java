package me.rkfg.pfe.gui.platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frostwire.jlibtorrent.TorrentAlertAdapter;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;

import me.rkfg.pfe.AbstractSettingsStorage;
import me.rkfg.pfe.PFECore;
import me.rkfg.pfe.gui.GUISettingsStorage;
import me.rkfg.pfe.gui.Main;

public abstract class Updater {
    protected Logger log = LoggerFactory.getLogger(getClass());
    private GUISettingsStorage settingsStorage;
    private String currentCommit;
    private String buildDate;

    public Updater(GUISettingsStorage settingsStorage) {
        this.settingsStorage = settingsStorage;
        currentCommit = null;
        try (BufferedReader brBuild = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/build.properties")))) {
            String[] line = brBuild.readLine().split("\\|");
            if (line.length != 2) {
                throw new IOException("Неверный формат версии");
            }
            currentCommit = line[0];
            buildDate = line[1];
        } catch (IOException e) {
            log.error("Не удалось прочитать версию сборки. ", e);
        }
    }

    public void update() {
        Path targetPath = getUpdateFilePath();
        if (targetPath.toFile().exists()) {
            log.info("Update file exists: {}, performing the update.", targetPath);
            if (doUpdate()) {
                System.exit(1);
            }
        }
        startChecker();
    }

    private void startChecker() {
        final Timer timer = new Timer("Update timer", true);
        timer.schedule(new TimerTask() {

            URL updateUrl = settingsStorage.getUpdateURL();

            @Override
            public void run() {
                if (updateUrl == null) {
                    timer.cancel();
                    return;
                }
                try {
                    log.info("Current commit: {} update url: {}", currentCommit, updateUrl);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(updateUrl.openStream()))) {
                        String line = br.readLine();
                        String[] split = line.split("\\s+");
                        if (split.length != 2) {
                            return;
                        }
                        String commit = split[0];
                        String hash = split[1];
                        if (commit == null || currentCommit == null || currentCommit.equals(commit)) {
                            return;
                        }
                        log.info("Should do the update! Old hash: {}, new hash: {}, torrent hash: {}", currentCommit, commit, hash);
                        PFECore pfeCore = PFECore.INSTANCE;
                        final String tmpDir = System.getProperty("java.io.tmpdir");
                        final TorrentHandle handle = pfeCore.addTorrent(hash, tmpDir);
                        pfeCore.addListener(new TorrentAlertAdapter(handle) {

                            @Override
                            public int[] types() {
                                return new int[] { AlertType.TORRENT_FINISHED.getSwig() };
                            }

                            @Override
                            public void torrentFinished(TorrentFinishedAlert alert) {
                                File updateRoot = new File(tmpDir, handle.getTorrentInfo().getFiles().getName());
                                String binaryFilename = getBinaryFilename();
                                File src = new File(updateRoot, binaryFilename);
                                log.info("Updating from: {}", src.getAbsolutePath());
                                try {
                                    Files.copy(src.toPath(), getUpdateFilePath(), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    log.error("Can't update: ", e);
                                }
                            }

                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }, 1000, TimeUnit.MINUTES.toMillis(60));
    }

    public String getBuildDate() {
        return buildDate;
    }

    public String getCommit() {
        return currentCommit;
    }

    protected String getArch() {
        String arch = System.getProperty("os.arch");
        if (arch.equals("x86") || arch.equals("i386")) {
            arch = "32";
        }
        if (arch.equals("amd64")) {
            arch = "64";
        }
        return arch;
    }

    protected Path getUpdateFilePath() {
        return getUpdateFilePath(false);
    }

    protected Path getUpdateFilePath(boolean ownFilename) {
        String filename = null;
        if (ownFilename) {
            filename = getBinaryFilename();
        } else {
            filename = getBinaryFilename() + ".new";
        }
        return new File(AbstractSettingsStorage.getJarDirectory(Updater.class)).toPath().resolve(filename);
    }

    protected abstract String getBinaryFilename();

    /**
     * Perform the update.
     * 
     * @return true if restart required, false otherwise.
     */
    protected abstract boolean doUpdate();

}

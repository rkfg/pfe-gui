package me.rkfg.pfe.gui;

import java.util.Timer;
import java.util.TimerTask;

import me.rkfg.pfe.PFECore;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;

public abstract class ClipboardMonitor {
    Clipboard clipboard;
    private static long POLLING_INTERVAL = 2000;
    private TextTransfer textTransfer = TextTransfer.getInstance();
    String previous = "";
    private Display display;
    private Timer timer;

    public ClipboardMonitor(final FileReceiver parent, final PathStorage pathStorage) {
        display = parent.getDisplay();
        clipboard = new Clipboard(parent.getDisplay());
        timer = new Timer("Clipboard monitor", true);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (!parent.isDownloadDialogOpened()) {
                            String hashFromClipboard = getHashFromClipboard(true);
                            if (hashFromClipboard != null) {
                                DownloadDialog downloadDialog = new DownloadDialog(parent.getShell());
                                DownloadInfo info = downloadDialog.open(hashFromClipboard, pathStorage.getDownloadPath(), true);
                                if (info != null) {
                                    addTorrent(info);
                                }
                            }
                        }
                    }
                });
            }
        }, POLLING_INTERVAL, POLLING_INTERVAL);
    }

    public String getHashFromClipboard(boolean checkPrevious) {
        TransferData[] types = clipboard.getAvailableTypes();
        for (TransferData transferData : types) {
            if (textTransfer.isSupportedType(transferData)) {
                String data = (String) clipboard.getContents(textTransfer);
                if (checkPrevious) {
                    if (data.equals(previous)) {
                        return null;
                    } else {
                        previous = data;
                    }
                }
                if (Main.validateHash(data) && PFECore.INSTANCE.findTorrent(data) == null) {
                    return data;
                }
                return null;
            }
        }
        return null;
    }

    protected abstract void addTorrent(DownloadInfo info);

    public void stop() {
        timer.cancel();
    }
}

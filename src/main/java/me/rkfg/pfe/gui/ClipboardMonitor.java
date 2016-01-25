package me.rkfg.pfe.gui;

import java.util.Timer;
import java.util.TimerTask;

import me.rkfg.pfe.PFECore;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class ClipboardMonitor {
    Clipboard clipboard;
    private static long POLLING_INTERVAL = 2000;
    private TextTransfer textTransfer = TextTransfer.getInstance();
    String previous = "";
    private Display display;
    private Timer timer;

    public ClipboardMonitor(final Shell parent, final PathStorage pathStorage) {
        display = parent.getDisplay();
        clipboard = new Clipboard(parent.getDisplay());
        timer = new Timer("Clipboard monitor", true);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        TransferData[] types = clipboard.getAvailableTypes();
                        for (TransferData transferData : types) {
                            if (textTransfer.isSupportedType(transferData)) {
                                String data = (String) clipboard.getContents(textTransfer);
                                if (data.equals(previous)) {
                                    return;
                                } else {
                                    previous = data;
                                }
                                if (Main.validateHash(data) && PFECore.INSTANCE.findTorrent(data) == null) {
                                    DownloadDialog downloadDialog = new DownloadDialog(parent);
                                    DownloadInfo info = downloadDialog.open(data, pathStorage.getDownloadPath());
                                    if (info != null) {
                                        addTorrent(info);
                                    }
                                }
                                return;
                            }
                        }

                    }
                });
            }
        }, POLLING_INTERVAL, POLLING_INTERVAL);
    }

    protected abstract void addTorrent(DownloadInfo info);

    public void stop() {
        timer.cancel();
    }
}

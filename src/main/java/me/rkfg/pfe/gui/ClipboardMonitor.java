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
    private static long POLLING_INTERVAL = 500;
    private TextTransfer textTransfer = TextTransfer.getInstance();
    String previous = "";
    private Display display;

    public ClipboardMonitor(final Shell parent, final PathStorage pathStorage) {
        display = parent.getDisplay();
        clipboard = new Clipboard(parent.getDisplay());
        Timer timer = new Timer("Clipboard monitor", true);
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
                                if (validate(data) && PFECore.INSTANCE.findTorrent(data) == null) {
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

            private boolean validate(String data) {
                if (data.length() != 32) {
                    return false;
                }
                for (int i = 0; i < data.length(); i++) {
                    char c = data.charAt(i);
                    if (c < '2' || c > '7' && c < 'A' || c > 'Z') {
                        return false;
                    }
                }
                return true;
            }
        }, POLLING_INTERVAL, POLLING_INTERVAL);
    }

    protected abstract void addTorrent(DownloadInfo info);
}

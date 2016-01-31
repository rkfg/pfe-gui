package me.rkfg.pfe.gui;

import java.io.File;

import me.rkfg.pfe.TorrentActivity;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.wb.swt.SWTResourceManager;

public class Progress extends Composite {
    ProgressBar pb_hashProgress;
    private Label lb_title;
    private Button b_copy;
    private String hash;
    private Clipboard clipboard;
    private String name;
    private Button b_cancel;
    private int progress;
    private long size;
    private boolean complete = false;
    private Label lb_complete;
    private FileReceiver receiver;
    private long seedPercent = 0;
    private int peers = 0;
    private Button b_open;
    private String rootPath;
    private boolean openAfterDownload;

    public Progress(Composite parent, FileReceiver receiver, int style) {
        super(parent, SWT.NONE);
        this.receiver = receiver;
        createUI();
        setHandlers();
    }

    private void setHandlers() {
        b_copy.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                clipboard.setContents(new Object[] { getHash() }, new Transfer[] { TextTransfer.getInstance() });
            }
        });
        b_cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
                messageBox.setText("Остановить?");
                String operationName = complete ? "раздачу" : "закачку";
                messageBox.setMessage("Остановить и убрать эту " + operationName + "? Файлы не пострадают.");
                int result = messageBox.open();
                if (result == SWT.OK)
                    receiver.removeTorrent(hash);
            }
        });
        b_open.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                doOpen();
            }
        });
    }

    private void createUI() {
        clipboard = new Clipboard(getDisplay());
        setLayout(new GridLayout(4, false));

        lb_complete = new Label(this, SWT.NONE);
        lb_complete.setImage(SWTResourceManager.getImage(Progress.class, "/me/rkfg/pfe/gui/icons/arrow-down-double.png"));
        lb_complete.setToolTipText("В процессе");

        b_open = new Button(this, SWT.NONE);
        b_open.setToolTipText("Открыть");
        b_open.setVisible(false);

        lb_title = new Label(this, SWT.WRAP);
        lb_title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        b_copy = new Button(this, SWT.NONE);
        b_copy.setEnabled(false);
        b_copy.setToolTipText("Скопировать код");
        b_copy.setImage(SWTResourceManager.getImage(Progress.class, "/me/rkfg/pfe/gui/icons/edit-copy.png"));
        pb_hashProgress = new ProgressBar(this, SWT.BORDER);
        pb_hashProgress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        pb_hashProgress.setMaximum(100);

        b_cancel = new Button(this, SWT.NONE);
        b_cancel.setEnabled(false);
        b_cancel.setToolTipText("Остановить");
        b_cancel.setImage(SWTResourceManager.getImage(Progress.class, "/me/rkfg/pfe/gui/icons/media-playback-stop.png"));
    }

    public void setProgress(int progress) {
        this.progress = progress;
        pb_hashProgress.setSelection(progress);
        updateTitle();
    }

    public void updateTitle() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name);
        }
        if (size > 0) {
            sb.append(", ").append(formatSize());
        }
        sb.append(" (").append(progress).append("%)");
        if (hash != null && !hash.isEmpty()) {
            b_copy.setEnabled(true);
            b_cancel.setEnabled(true);
        }
        sb.append(", ").append(" роздано ").append(seedPercent).append("%, качают: ").append(peers);
        lb_title.setText(sb.toString());
        lb_title.requestLayout();
    }

    private String formatSize() {
        String result = size + " " + "байт";
        String suffixes[] = { "байт", "КБайт", "МБайт", "ГБайт" };
        for (int i = 9; i >= 0; i -= 3) {
            double scale = Math.pow(10, i);
            if (size >= scale) {
                return Math.round(size * 100 / scale) / 100.0 + " " + suffixes[i / 3];
            }
        }
        return result;
    }

    public String getTorrentName() {
        return name;
    }

    public void setTorrentName(String name) {
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setTorrentSize(long size) {
        this.size = size;
    }

    public long geTorrentSize() {
        return size;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete() {
        complete = true;
        lb_complete.setImage(SWTResourceManager.getImage(Progress.class, "/me/rkfg/pfe/gui/icons/dialog-ok-apply.png"));
        lb_complete.setToolTipText("Завершён");
        setProgress(100);
        showOpenButton();
        if (openAfterDownload) {
            doOpen();
        }
    }

    private void showOpenButton() {
        if (getTorrentFile().isDirectory()) {
            b_open.setImage(SWTResourceManager.getImage(Progress.class, "/me/rkfg/pfe/gui/icons/folder-yellow.png"));
        } else {
            b_open.setImage(SWTResourceManager.getImage(Progress.class, "/me/rkfg/pfe/gui/icons/x-office-document.png"));
        }
        b_open.setVisible(true);
        b_open.requestLayout();
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void updateActivity(TorrentActivity torrentActivity) {
        setProgress(torrentActivity.progress);
        if (name == null && torrentActivity.name != null) {
            setTorrentName(torrentActivity.name);
        }
        long totalSize = torrentActivity.size;
        if (size == 0 && totalSize > 0) {
            setTorrentSize(totalSize);
        }
        if (!complete && torrentActivity.complete) {
            setComplete();
        }
        peers = torrentActivity.peers;
        seedPercent = torrentActivity.seedPercent;
        updateTitle();
    }

    public void setOpenAfterDownload(boolean openAfterDownload) {
        this.openAfterDownload = openAfterDownload;
    }

    private void doOpen() {
        if (rootPath == null || name == null) {
            return;
        }
        Program.launch(getTorrentFile().getAbsolutePath());
    }

    private File getTorrentFile() {
        return new File(rootPath, name);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        lb_title.setBackground(color);
        lb_complete.setBackground(color);
        pb_hashProgress.setBackground(color);
    }

}

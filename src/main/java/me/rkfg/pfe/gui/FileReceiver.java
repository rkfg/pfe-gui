package me.rkfg.pfe.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.rkfg.pfe.PFECore;
import me.rkfg.pfe.PFEListener;
import me.rkfg.pfe.TorrentActivity;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.swig.set_piece_hashes_listener;

public class FileReceiver extends Composite {

    PFECore pfeCore = PFECore.INSTANCE;
    private Label lblDropTargetHint;
    private DropTarget dropTarget;
    private ToolItem addFiles;
    private ToolItem addDirs;
    private ToolItem downloadTorrent;
    private Display display;

    ExecutorService executorService = Executors.newCachedThreadPool();
    private Map<String, Progress> progresses = new HashMap<>();
    private PathStorage pathStorage = new PathStorage();

    FileReceiver(Composite parent) {
        super(parent, SWT.NONE);
        display = getDisplay();
        setLayout(new GridLayout(1, false));

        ToolBar toolBar = new ToolBar(this, SWT.FLAT);
        toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        addFiles = new ToolItem(toolBar, SWT.NONE);
        addFiles.setWidth(32);
        addFiles.setImage(SWTResourceManager.getImage(FileReceiver.class, "/me/rkfg/pfe/gui/icons/document-new.png"));
        addFiles.setToolTipText("Добавить файлы");

        addDirs = new ToolItem(toolBar, SWT.NONE);
        addDirs.setImage(SWTResourceManager.getImage(FileReceiver.class, "/me/rkfg/pfe/gui/icons/document-open-folder.png"));
        addDirs.setToolTipText("Добавить папку");

        downloadTorrent = new ToolItem(toolBar, SWT.NONE);
        downloadTorrent.setImage(SWTResourceManager.getImage(FileReceiver.class, "/me/rkfg/pfe/gui/icons/download.png"));
        downloadTorrent.setToolTipText("Скачать");

        createDropTarget();
        setHandlers();
        pfeCore.addPFEListener(new PFEListener() {

            @Override
            public void torrentProgress(Collection<TorrentActivity> torrentActivities) {
                for (final TorrentActivity torrentActivity : torrentActivities) {
                    if (torrentActivity.hash != null) {
                        final Progress progress = progresses.get(torrentActivity.hash);
                        if (progress != null) {
                            display.asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    progress.setProgress(torrentActivity.progress);
                                    if (progress.getTorrentName() == null && torrentActivity.name != null) {
                                        progress.setTorrentName(torrentActivity.name);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
        layout();
    }

    private void setHandlers() {
        addFiles.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                fileDialog.setFilterPath(pathStorage.getSharePath());
                if (fileDialog.open() == null) {
                    return;
                }
                List<String> filenames = new ArrayList<>(fileDialog.getFileNames().length);
                String filterPath = fileDialog.getFilterPath();
                pathStorage.setDownloadPath(filterPath);
                for (String filename : fileDialog.getFileNames()) {
                    filenames.add(new File(filterPath, filename).getAbsolutePath());
                }
                hashFiles(filenames.toArray(new String[0]));
            }
        });
        addDirs.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
                directoryDialog.setMessage("Выберите папку, чтобы поделиться ею.");
                directoryDialog.setText("Выберите папку");
                directoryDialog.setFilterPath(pathStorage.getSharePath());
                String selected = directoryDialog.open();
                if (selected == null) {
                    return;
                }
                pathStorage.setSharePath(selected);
                hashFiles(selected);
            }
        });
        downloadTorrent.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DownloadInfo info = new DownloadDialog(getShell()).open(pathStorage.getDownloadPath());
                if (info != null) {
                    pathStorage.setDownloadPath(info.path);
                    pfeCore.addTorrent(info.hash, info.path);
                    createProgress(info.hash);
                }
            }
        });
    }

    public void hashFiles(final String... files) {
        final Progress progress = createProgress(null);
        executorService.submit(new Callable<TorrentHandle>() {

            @Override
            public TorrentHandle call() throws Exception {
                try {
                    final TorrentHandle handle = pfeCore.share(new set_piece_hashes_listener() {
                        @Override
                        public void progress(final int i) {
                            display.asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    progress.setProgress(i);
                                }
                            });
                        }
                    }, files);
                    handle.resume();
                    final String hash = pfeCore.getHash(handle);
                    display.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            progress.setTitle(handle.getName(), hash);
                            progress.setProgress(100);
                            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                            messageBox.setText("Готово");
                            messageBox.setMessage("Код: " + hash);
                            messageBox.open();
                        }
                    });
                    return handle;
                } catch (Throwable e) {
                    showError(e);
                    return null;
                }
            }

            private void showError(final Throwable e) {
                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        progress.dispose();
                        MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBox.setText("Ошибка");
                        messageBox.setMessage(e.getMessage());
                        messageBox.open();
                    }
                });
            }
        });
    }

    private Progress createProgress(String hash) {

        Progress progress = progresses.get(hash);
        if (progress != null) {
            return progress;
        }
        progress = new Progress(this, SWT.NONE);
        GridData gd_progress = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
        progress.setLayoutData(gd_progress);
        progress.moveAbove(lblDropTargetHint);
        progress.setHash(hash);
        progress.updateTitle();
        layout();
        if (hash != null) {
            progresses.put(hash, progress);
        }
        return progress;
    }

    private void createDropTarget() {
        dropTarget = new DropTarget(this, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
        lblDropTargetHint = new Label(this, SWT.NONE);
        lblDropTargetHint.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        lblDropTargetHint.setText("Перетащите сюда файлы или папку, чтобы поделиться ими");
        dropTarget.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
            }

            @Override
            public void dragLeave(DropTargetEvent event) {
                setBackground(null);
            }

            @Override
            public void drop(DropTargetEvent event) {
                if (!FileTransfer.getInstance().isSupportedType(event.currentDataType) || !(event.data instanceof String[])) {
                    return;
                }
                String[] files = (String[]) event.data;
                hashFiles(files);
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        executorService.shutdownNow();
    }

    public void removeTorrent(String hash) {
        if (hash == null) {
            return;
        }
        TorrentHandle handle = pfeCore.findTorrent(hash);
        Progress removed = progresses.remove(hash);
        if (removed != null) {
            removed.dispose();
        }
        pfeCore.removeTorrent(handle);
        layout();
    }
}

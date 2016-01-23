package me.rkfg.pfe.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.rkfg.pfe.PFECore;
import me.rkfg.pfe.PFEException;

import org.apache.commons.codec.DecoderException;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.swig.set_piece_hashes_listener;

public class FileReceiver extends Composite {

    PFECore pfeCore = PFECore.INSTANCE;
    private Label lblDropTargetHint;
    ProgressBar pb_hashProgress;
    private DropTarget dropTarget;
    private ToolItem addFiles;
    private ToolItem addDirs;
    private ToolItem downloadTorrent;

    FileReceiver(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(1, false));
        dropTarget = new DropTarget(this, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });

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

    }

    private void setHandlers() {
        addFiles.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                if (fileDialog.open() == null) {
                    return;
                }
                List<String> filenames = new ArrayList<>(fileDialog.getFileNames().length);
                String filterPath = fileDialog.getFilterPath();
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
                String selected = directoryDialog.open();
                if (selected == null) {
                    return;
                }
                hashFiles(selected);
            }
        });
        downloadTorrent.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DownloadInfo info = new DownloadDialog(getShell()).open();
                if (info != null) {
                    pfeCore.addTorrent(info.hash, info.path);
                }
            }
        });
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

    private void createProgressBar() {
        pb_hashProgress = new ProgressBar(this, SWT.HORIZONTAL);
        GridData gd_pb_hashProgress = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        pb_hashProgress.setLayoutData(gd_pb_hashProgress);
        pb_hashProgress.setMaximum(100);
    }

    private void createDropTarget() {
        lblDropTargetHint = new Label(this, SWT.NONE);
        lblDropTargetHint.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        lblDropTargetHint.setText("Перетащите сюда файлы или папку из проводника");
    }

    private void hashFiles(String... files) {
        try {
            createProgressBar();
            lblDropTargetHint.dispose();
            layout(true, true);
            TorrentHandle handle = pfeCore.share(new set_piece_hashes_listener() {
                @Override
                public void progress(final int i) {
                    pb_hashProgress.setSelection(i);
                    layout(true, true);
                    getDisplay().readAndDispatch();
                }
            }, files);
            restoreDropTarget();
            handle.resume();
            String link = pfeCore.getLink(handle);
            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
            messageBox.setText("Sharing is ready");
            messageBox.setMessage("Share this link: pfe:" + link);
            messageBox.open();
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (PFEException e) {
            restoreDropTarget();
            MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            messageBox.setText("Error");
            messageBox.setMessage(e.getMessage());
            messageBox.open();
        }
    }

    private void restoreDropTarget() {
        createDropTarget();
        pb_hashProgress.dispose();
        layout(true, true);
    }
}

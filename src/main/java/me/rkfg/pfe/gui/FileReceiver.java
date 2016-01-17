package me.rkfg.pfe.gui;

import me.rkfg.pfe.PFECore;

import org.apache.commons.codec.DecoderException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.frostwire.jlibtorrent.TorrentHandle;

public class FileReceiver extends Composite {

    PFECore pfeCore = PFECore.INSTANCE;

    FileReceiver(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(1, false));
        DropTarget dropTarget = new DropTarget(this, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
        dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });

        ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gridData.heightHint = 30;
        toolBar.setLayoutData(gridData);

        ToolItem addFiles = new ToolItem(toolBar, SWT.NONE);
        addFiles.setToolTipText("Добавить файлы");

        Label lblNewLabel = new Label(this, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        lblNewLabel.setText("Перетащите сюда файлы или папку из проводника");
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
                TorrentHandle handle = pfeCore.share(files);
                handle.resume();
                try {
                    String link = pfeCore.getLink(handle);
                    MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
                    messageBox.setText("Sharing is ready");
                    messageBox.setMessage("Share this link: pfe:" + link);
                    messageBox.open();
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}

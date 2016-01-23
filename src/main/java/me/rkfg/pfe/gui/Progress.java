package me.rkfg.pfe.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.wb.swt.SWTResourceManager;

public class Progress extends Composite {
    ProgressBar pb_hashProgress;
    private Label lb_title;
    private Button b_copy;
    private String hash;
    private Clipboard clipboard;

    public Progress(Composite parent, int style) {
        super(parent, SWT.NONE);
        createUI();
        setHandlers();
    }

    private void setHandlers() {
        b_copy.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                clipboard.setContents(new Object[] { hash }, new Transfer[] { TextTransfer.getInstance() });
            }
        });
    }

    private void createUI() {
        clipboard = new Clipboard(getDisplay());
        setLayout(new GridLayout(2, false));

        lb_title = new Label(this, SWT.NONE);
        lb_title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        b_copy = new Button(this, SWT.NONE);
        b_copy.setEnabled(false);
        b_copy.setToolTipText("Скопировать код");
        b_copy.setImage(SWTResourceManager.getImage(Progress.class, "/me/rkfg/pfe/gui/icons/edit-copy.png"));
        pb_hashProgress = new ProgressBar(this, SWT.BORDER);
        pb_hashProgress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        pb_hashProgress.setMaximum(100);

        Button b_cancel = new Button(this, SWT.FLAT);
        b_cancel.setToolTipText("Отмена");
        b_cancel.setImage(SWTResourceManager.getImage(Progress.class, "/me/rkfg/pfe/gui/icons/process-stop.png"));
    }

    public void setProgress(int i) {
        pb_hashProgress.setSelection(i);
    }

    public void setTitle(String name, String hash) {
        this.hash = hash;
        if (hash != null && !hash.isEmpty()) {
            b_copy.setEnabled(true);
        }
        lb_title.setText(String.format("%s [%s]", name, hash));
    }

}

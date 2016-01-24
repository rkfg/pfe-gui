package me.rkfg.pfe.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DownloadDialog extends Dialog {

    private DownloadInfo downloadInfo;
    private Text tb_hash;
    private Text tb_saveTo;
    private Button b_cancel;
    private Button b_ok;
    private Shell shell;
    private Button b_saveTo;

    public DownloadDialog(Shell parent) {
        super(parent);
    }

    public DownloadInfo open() {
        return open(null, null);
    }

    public DownloadInfo open(String path) {
        return open(null, path);
    }

    public DownloadInfo open(String hash, String path) {
        Shell shell = new Shell(getParent(), getStyle());
        shell.setSize(400, 300);
        Display display = shell.getDisplay();
        createUI(shell);
        if (path == null) {
            tb_saveTo.setText(Main.HOME);
        } else {
            tb_saveTo.setText(path);
        }
        setHandlers();
        Main.center(shell, true);
        if (hash != null && !hash.isEmpty() && hash.length() == 32) {
            tb_hash.setText(hash);
            tb_hash.setEnabled(false);
        }
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return downloadInfo;
    }

    private void createUI(Shell shell) {
        this.shell = shell;
        shell.setLayout(new GridLayout(3, false));

        Label lblNewLabel = new Label(shell, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblNewLabel.setText("Код для скачивания");

        tb_hash = new Text(shell, SWT.BORDER);
        GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_text.minimumWidth = 300;
        tb_hash.setLayoutData(gd_text);

        Label label = new Label(shell, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("Сохранить в");

        tb_saveTo = new Text(shell, SWT.BORDER);
        tb_saveTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        b_saveTo = new Button(shell, SWT.NONE);
        b_saveTo.setText("Обзор...");

        Composite c_okCancel = new Composite(shell, SWT.NONE);
        RowLayout rl_composite = new RowLayout(SWT.HORIZONTAL);
        c_okCancel.setLayout(rl_composite);
        c_okCancel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));
        b_ok = new Button(c_okCancel, SWT.NONE);
        b_ok.setText("OK");

        b_cancel = new Button(c_okCancel, SWT.NONE);
        b_cancel.setText("Отмена");
        shell.pack();
    }

    private void setHandlers() {
        b_cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
        b_ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                downloadInfo = new DownloadInfo();
                downloadInfo.hash = tb_hash.getText();
                downloadInfo.path = tb_saveTo.getText();
                shell.close();
            }
        });
        b_saveTo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.SAVE);
                directoryDialog.setText("Выберите папку");
                directoryDialog.setMessage("Выберите папку для скачивания");
                directoryDialog.setFilterPath(tb_saveTo.getText());
                String selected = directoryDialog.open();
                if (selected == null) {
                    return;
                }
                tb_saveTo.setText(selected);
            }
        });
    }
}

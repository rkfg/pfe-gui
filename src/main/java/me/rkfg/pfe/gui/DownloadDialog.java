package me.rkfg.pfe.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DownloadDialog extends Dialog {

    private DownloadInfo downloadInfo;
    private Text tb_hash;
    private Text tb_saveTo;
    private Button b_cancel;
    private Button b_download;
    private Shell shell;
    private Button b_saveTo;
    private Button b_downloadOpen;

    public DownloadDialog(Shell parent) {
        super(parent);
    }

    public DownloadInfo open() {
        return open(null, null, false);
    }

    public DownloadInfo open(String path) {
        return open(null, path, false);
    }

    public DownloadInfo open(String hash, String path, boolean lockHash) {
        Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Добавить закачку");
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
        }
        if (lockHash) {
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

        Composite c_buttons = new Composite(shell, SWT.NONE);
        RowLayout rl_c_buttons = new RowLayout(SWT.HORIZONTAL);
        c_buttons.setLayout(rl_c_buttons);
        c_buttons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1));
        b_download = new Button(c_buttons, SWT.NONE);
        b_download.setText("Скачать");

        b_downloadOpen = new Button(c_buttons, SWT.NONE);
        b_downloadOpen.setText("Скачать и открыть");

        b_cancel = new Button(c_buttons, SWT.NONE);
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
        b_download.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                confirmDialog(false);
            }
        });
        b_downloadOpen.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                confirmDialog(true);
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
        KeyListener enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 13) {
                    confirmDialog(false);
                }
            }
        };
        tb_hash.addKeyListener(enterListener);
        tb_saveTo.addKeyListener(enterListener);
    }

    private void confirmDialog(boolean openAfterDownload) {
        String hash = Main.validateHash(tb_hash.getText());
        if (hash == null) {
            MessageBox messageBox = new MessageBox(getParent());
            messageBox.setText("Ошибка");
            messageBox.setMessage("Неверно введён код. Он должен содержать ровно 32 символа, цифры от 2 до 7 и буквы от A до Z.");
            messageBox.open();
            return;
        }
        downloadInfo = new DownloadInfo();
        downloadInfo.hash = hash;
        downloadInfo.path = tb_saveTo.getText();
        downloadInfo.openAfterDownload = openAfterDownload;
        shell.close();
    }
}

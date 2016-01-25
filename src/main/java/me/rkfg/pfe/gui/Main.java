package me.rkfg.pfe.gui;

import me.rkfg.pfe.PFECore;
import me.rkfg.pfe.SettingsStorage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Main {

    public static final String HOME = System.getProperty("user.home");
    private Shell shell;
    private Display display;
    PFECore pfeCore = PFECore.INSTANCE;
    private FileReceiver fileReceiver;

    Main() {
        this.display = Display.getDefault();
        this.shell = new Shell(display);
        shell.setMinimumSize(400, 300);
        shell.setText("PFE GUI Client");
        FillLayout fillLayout = new FillLayout(SWT.ALL);
        shell.setLayout(fillLayout);
        fileReceiver = new FileReceiver(shell);
        shell.pack();
        center(shell);
        pfeCore.init(new SettingsStorage());
        shell.open();
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        shell.dispose();
        pfeCore.stop();
        fileReceiver.dispose();
        display.dispose();
    }

    public static void center(Shell shell, boolean relativeToParent) {
        int left = 0;
        int top = 0;
        Rectangle bounds = null;
        if (relativeToParent && shell != null) {
            bounds = shell.getParent().getBounds();
            left = bounds.x;
            top = bounds.y;
        } else {
            bounds = shell.getDisplay().getBounds();
        }
        Point size = shell.getSize();
        shell.setLocation(left + (bounds.width - size.x) / 2, top + (bounds.height - size.y) / 2);
    }

    public static void center(Shell shell) {
        center(shell, false);
    }

    public static boolean validateHash(String data) {
        data = data.trim();
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
}

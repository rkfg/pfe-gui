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

    private Shell shell;
    private Display display;
    PFECore pfeCore = PFECore.INSTANCE;

    Main() {
        this.display = Display.getDefault();
        this.shell = new Shell(display);
        shell.setMinimumSize(400, 300);
        shell.setText("PFE GUI Client");
        FillLayout fillLayout = new FillLayout(SWT.ALL);
        shell.setLayout(fillLayout);
        new FileReceiver(shell);
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
}

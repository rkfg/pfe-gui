package me.rkfg.pfe.gui;

import me.rkfg.pfe.PFECore;
import me.rkfg.pfe.SettingsStorage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Main extends Composite {

    private Shell shell;
    private Display display;
    PFECore pfeCore = PFECore.INSTANCE;

    Main(Shell parent, Display display) {
        super(parent, SWT.NONE);
        this.shell = parent;
        this.display = display;
        parent.setMinimumSize(400, 300);
        parent.setText("PFE GUI Client");
        FillLayout fillLayout = new FillLayout(SWT.ALL);
        parent.setLayout(fillLayout);
        setLayout(fillLayout);
        new FileReceiver(this);
        parent.pack();
        Rectangle bounds = display.getBounds();
        Point size = parent.getSize();
        parent.setLocation((bounds.width - size.x) / 2, (bounds.height - size.y) / 2);
        pfeCore.init(new SettingsStorage());
        parent.open();
    }

    public static void main(String[] args) {
        Display display = Display.getDefault();
        new Main(new Shell(display), display).run();
    }

    private void run() {
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

}

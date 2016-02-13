package me.rkfg.pfe.gui;

import me.rkfg.pfe.PFECore;
import me.rkfg.pfe.gui.platform.Updater;
import me.rkfg.pfe.gui.platform.UpdaterLinux;
import me.rkfg.pfe.gui.platform.UpdaterWindows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class Main {

    public static final String HOME = System.getProperty("user.home");
    private Shell shell;
    private Display display;
    private PFECore pfeCore = PFECore.INSTANCE;
    private PathStorage pathStorage = new PathStorage();
    private FileReceiver fileReceiver;
    private Menu trayMenu;
    private GUISettingsStorage settingsStorage;
    private Updater updater;

    private Main() {
        this.display = Display.getDefault();
        this.shell = new Shell(display);
        shell.setMinimumSize(400, 300);
        shell.setText("Файлообмен");
        FillLayout fillLayout = new FillLayout(SWT.ALL);
        shell.setLayout(fillLayout);
        fileReceiver = new FileReceiver(shell, pathStorage);
        shell.pack();
        center(shell);
        settingsStorage = new GUISettingsStorage(Main.class);
        pfeCore.init(settingsStorage);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            updater = new UpdaterWindows(settingsStorage);
        } else {
            updater = new UpdaterLinux(settingsStorage);
        }
        updater.update();
        createTrayIcon();
        shell.addListener(SWT.Close, new Listener() {

            @Override
            public void handleEvent(Event event) {
                event.doit = false;
                shell.setVisible(false);
            }
        });
    }

    private void createTrayIcon() {
        TrayItem trayIcon = new TrayItem(display.getSystemTray(), SWT.NONE);
        trayIcon.setImage(SWTResourceManager.getImage(Main.class, "/me/rkfg/pfe/gui/icons/arrow-down-double.png"));
        trayMenu = new Menu(shell);
        trayIcon.addMenuDetectListener(new MenuDetectListener() {

            @Override
            public void menuDetected(MenuDetectEvent e) {
                trayMenu.setLocation(display.getCursorLocation());
                trayMenu.setVisible(true);
            }
        });
        trayIcon.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.setVisible(!shell.isVisible());
            }
        });
        MenuItem mi_open = new MenuItem(trayMenu, SWT.NONE);
        mi_open.setText("Открыть");
        mi_open.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.open();
            }
        });
        MenuItem mi_exit = new MenuItem(trayMenu, SWT.NONE);
        mi_exit.setText("Выход");
        mi_exit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
                messageBox.setText("Выход?");
                messageBox.setMessage("Вы действительно хотите закрыть программу? "
                        + "Ваши файлы будут недоступны, пока вы снова не запустите её.");
                shell.open();
                if (messageBox.open() == SWT.CANCEL) {
                    return;
                }
                shell.dispose();
            }
        });
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

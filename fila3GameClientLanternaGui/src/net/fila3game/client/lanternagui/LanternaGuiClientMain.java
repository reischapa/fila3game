package net.fila3game.client.lanternagui;

import net.fila3game.client.backend.GameClient;

/**
 * Created by codecadet on 2/28/17.
 */
public class LanternaGuiClientMain {
    public static void main(String[] args) {
        GameClient gc = null;

        if (args.length > 0) {
            gc = new GameClient(args[0]);
        }

        if (gc == null) {
            gc = new GameClient();
        }

        LanternaGUI ln = new LanternaGUI();
        ln.setGUIEventReceiver(gc);
        gc.setGUI(ln);
        ln.init();
    }
}

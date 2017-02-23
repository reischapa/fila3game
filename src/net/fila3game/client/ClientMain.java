package net.fila3game.client;

/**
 * Created by codecadet on 2/21/17.
 */
public class ClientMain {

    public static void main(String[] args) {

        GameClient gc = new GameClient();
        LanternaDisplayController display = new LanternaDisplayController();
        gc.setDisplay(display);
        display.setInputReceiver(gc);
        display.init();
        gc.connect("localhost");

    }
}

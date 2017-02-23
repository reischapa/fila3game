package net.fila3game.client;

import com.googlecode.lanterna.input.Key;
import net.fila3game.server.gameengine.Field;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;


/**
 * Created by codecadet on 2/21/17.
 */
public class LanternaDisplayController implements Display, Controller {

    public static void main(String[] args) {
        LanternaDisplayController d = new LanternaDisplayController();
        Field f = new Field(10, 10);
        d.receiveData(new GameState(f.returnAsString()));

    }

    private InputReceiver receiver;
    private Screen screen;

    @Override
    public void receiveData(GameState state) {
        //TODO SEND DATA
        Thread t = new Thread(new KeyListener());
        t.start();

    }

    @Override
    public void setInputReceiver(InputReceiver receiver) {
        this.receiver = receiver;
    }

    private class KeyListener implements Runnable {

        @Override
        public void run() {
            Key key;
            InputReceiver.Key k;

            while (true) {
                key = screen.readInput();
                k = translateKey(key);

                if (key == null) {
                    continue;
                }

                System.out.println("key " + k + " pressed!");
                receiver.receiveInput(k);

            }
        }
    }

    private InputReceiver.Key translateKey(Key key) {
        System.out.println("got key! " + key);
        switch (key.getKind()) {
            case ArrowDown:
                return InputReceiver.Key.KEY_ARROWDOWN;
            case ArrowLeft:
                return InputReceiver.Key.KEY_ARROWLEFT;
            case ArrowUp:
                return InputReceiver.Key.KEY_ARROWUP;
            case ArrowRight:
                return InputReceiver.Key.KEY_ARROWRIGHT;
            case NormalKey:
                switch (key.getCharacter()) {
                    case ' ':
                        return InputReceiver.Key.KEY_SPACE;
                }
        }

        System.err.println("Keystroke is not mapped, returning null...");
        return null;
    }

}

package net.fila3game.client;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.ScreenWriter;
import com.googlecode.lanterna.terminal.Terminal;
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
//        d.receiveData(new GameState(f.returnAsString()));
        d.init();
    }

    private InputReceiver receiver;
    private Screen screen;

    public void init() {
        screen = TerminalFacade.createScreen();
        screen.getTerminal().getTerminalSize().setColumns(60);
        screen.getTerminal().getTerminalSize().setRows(30);

        screen.getTerminal().setCursorVisible(false);
        ScreenWriter screenWriter = new ScreenWriter(screen);
        screenWriter.setBackgroundColor(Terminal.Color.BLUE);
        screenWriter.setForegroundColor(Terminal.Color.WHITE);

        screen.startScreen();

        Thread t = new Thread(new KeyListener());
        t.start();
    }

    @Override
    public void receiveData(GameState state) {

        String[] lines = state.getFieldString().split("\n");

        for (int y = 0; y < lines.length; y++) {
            char[] chars = lines[y].toCharArray();

            for (int x = 0; x < chars.length; x++) {
                lanternaConstructCellfromChar(x * 2, y, chars[x]);
            }

        }

        this.screen.refresh();

    }

    private void lanternaConstructCellfromChar(int x, int y, char c) {
        Terminal.Color back = null;
        Terminal.Color front = null;
        char actualChar = ' ';

        switch (c) {
            case 'T':
                back = Terminal.Color.GREEN;
                front = Terminal.Color.GREEN;
                actualChar = 'T';
                break;
            case 'B':
                back = Terminal.Color.MAGENTA;
                front = Terminal.Color.MAGENTA;
                actualChar = 'B';
                break;
            case 'W':
                back = Terminal.Color.BLUE;
                front = Terminal.Color.BLUE;
                actualChar = 'W';
                break;
            case '0':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.BLACK;
                actualChar = ' ';
                break;
        }
        this.screen.putString(x, y, "" + actualChar + actualChar, back, front);
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
                return getNormalKeyCharacter(key);
        }

        System.err.println("Keystroke is not mapped, returning null...");
        return null;
    }

    private InputReceiver.Key getNormalKeyCharacter(Key key) {
        switch (key.getCharacter()) {
            case ' ':
                return InputReceiver.Key.KEY_SPACE;
        }
        return null;
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

                if (key == null) {
                    continue;
                }

                k = translateKey(key);

                System.out.println("key " + k + " pressed!");
                receiver.receiveInput(k);

            }
        }
    }

}

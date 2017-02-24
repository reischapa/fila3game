package net.fila3game.client;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.ScreenWriter;
import com.googlecode.lanterna.terminal.Terminal;
import net.fila3game.server.gameengine.Field;
import com.googlecode.lanterna.screen.Screen;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


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

    public LanternaDisplayController() {
        this.executorService = new ScheduledThreadPoolExecutor(2);
    }

    private static final String title =
                    "██╗    ██╗ ██████╗ ██████╗ ██╗     ██████╗  \n" +
                    "██║    ██║██╔═══██╗██╔══██╗██║     ██╔══██╗ \n" +
                    "██║ █╗ ██║██║   ██║██████╔╝██║     ██║  ██║ \n" +
                    "██║███╗██║██║   ██║██╔══██╗██║     ██║  ██║ \n" +
                    "╚███╔███╔╝╚██████╔╝██║  ██║███████╗██████╔╝ \n" +
                    " ╚══╝╚══╝  ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═════╝  \n" +
                    "                                            \n" +
                    "   ██████╗ ███████╗    ████████╗ █████╗ ███╗   ██╗██╗  ██╗███████╗\n" +
                    "  ██╔═══██╗██╔════╝    ╚══██╔══╝██╔══██╗████╗  ██║██║ ██╔╝██╔════╝\n" +
                    "  ██║   ██║█████╗         ██║   ███████║██╔██╗ ██║█████╔╝ ███████╗\n" +
                    "  ██║   ██║██╔══╝         ██║   ██╔══██║██║╚██╗██║██╔═██╗ ╚════██║\n" +
                    "  ╚██████╔╝██║            ██║   ██║  ██║██║ ╚████║██║  ██╗███████║\n" +
                    "   ╚═════╝ ╚═╝            ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝\n" +
                    "                                                                    ";
    private static final String tank =
                    "░░░░░░███████ ]▄▄▄▄▄▄▄▄▃\n" +
                    "▂▄▅█████████▅▄▃▂\n" +
                    "I███████████████████].\n" +
                    "◥⊙▲⊙▲⊙▲⊙▲⊙▲⊙▲⊙◤...";

    private static final String message =
                    "╔═╗╦═╗╔═╗╔═╗╔═╗  ╔═╗╔╗╔╦ ╦  ╦╔═╔═╗╦ ╦  ╔╦╗╔═╗  ╔═╗╔╦╗╔═╗╦═╗╔╦╗\n" +
                    "╠═╝╠╦╝║╣ ╚═╗╚═╗  ╠═╣║║║╚╦╝  ╠╩╗║╣ ╚╦╝   ║ ║ ║  ╚═╗ ║ ╠═╣╠╦╝ ║ \n" +
                    "╩  ╩╚═╚═╝╚═╝╚═╝  ╩ ╩╝╚╝ ╩   ╩ ╩╚═╝ ╩    ╩ ╚═╝  ╚═╝ ╩ ╩ ╩╩╚═ ╩ ";

    private static final int titlePosX = 5;
    private static final int titlePosY = 5;
    private static final int tankPosX = 70;
    private static final int tankPosY = 20;
    private static final int messagePosX = 5;
    private static final int messagePosY = 20;
    ScheduledThreadPoolExecutor executorService;

    private InputReceiver receiver;
    private Screen screen;

    public void init() {
        showFrontPage();

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
                back = Terminal.Color.BLACK;
                front = Terminal.Color.GREEN;
                actualChar = ' ';
                break;
            case 'B':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.MAGENTA;
                actualChar = 'B';
                break;
            case 'W':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.BLUE;
                actualChar = '|';
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

        return null;
    }

    private InputReceiver.Key getNormalKeyCharacter(Key key) {
        switch (key.getCharacter()) {
            case ' ':
                return InputReceiver.Key.KEY_SPACE;
        }

        System.err.println("Keystroke is not mapped, returning null...");
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

    private void showFrontPage() {
        screen = TerminalFacade.createScreen();
        screen.startScreen();
        screen.getTerminal().getTerminalSize().setColumns(100);
        screen.getTerminal().getTerminalSize().setRows(30);
        screen.setCursorPosition(99, 29);
        screen.getTerminal().setCursorVisible(false);

        createScreenElements(titlePosX, titlePosY, title, Terminal.Color.WHITE);
        createScreenElements(tankPosX, tankPosY, tank, Terminal.Color.GREEN);

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                createScreenElements(messagePosX, messagePosY, message, Terminal.Color.YELLOW);
                screen.refresh();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                createScreenElements(messagePosX, messagePosY, message, Terminal.Color.BLACK);
                screen.refresh();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void createScreenElements(int x, int y, String text, Terminal.Color color) {

        Scanner scanner = new Scanner(text);

        while (scanner.hasNextLine()) {

            screen.putString(x, y, scanner.nextLine(), color, Terminal.Color.BLACK);
            y++;
        }
    }

}

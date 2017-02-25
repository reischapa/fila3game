package net.fila3game.client;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.screen.Screen;
import net.fila3game.AudioManager;

import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by codecadet on 2/21/17.
 */
public class LanternaDisplayController implements Display, Controller {

    private enum State {
        MAIN_SCREEN, IN_GAME,
    }

    public LanternaDisplayController() {
        this.mainMenuBlinkExecutorService = new ScheduledThreadPoolExecutor(2);
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

    private ScheduledThreadPoolExecutor mainMenuBlinkExecutorService;
    private GUIEventReceiver receiver;
    private Screen screen;
    private State state = State.MAIN_SCREEN;

    public void init() {
        AudioManager.load(new String[]{"sound", /* TODO MORE SOUNDS HERE - GIULIANO */});
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
            case 'A':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.MAGENTA;
                actualChar = 'A';
                break;
            case 'V':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.MAGENTA;
                actualChar = 'V';
                break;
            case '<':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.MAGENTA;
                actualChar = '<';
                break;
            case '>':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.MAGENTA;
                actualChar = '>';
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
            case '1':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.GREEN;
                actualChar = '1';
                break;
            case '2':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.GREEN;
                actualChar = '2';
                break;
            case '3':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.GREEN;
                actualChar = '3';
                break;
            case '4':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.GREEN;
                actualChar = '4';
                break;
            case '♥':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.RED;
                actualChar = '♥';
                break;
            case '@':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.CYAN;
                actualChar = '@';
            default:
                System.out.println("Something went terribly wrong");
        }

        this.screen.putString(x, y, "" + actualChar + actualChar, back, front);
    }

    private GUIEvent.Key translateKey(Key key) {
        System.out.println("got key! " + key);
        switch (key.getKind()) {
            case ArrowDown:
                return GUIEvent.Key.KEY_ARROWDOWN;
            case ArrowLeft:
                return GUIEvent.Key.KEY_ARROWLEFT;
            case ArrowUp:
                return GUIEvent.Key.KEY_ARROWUP;
            case ArrowRight:
                return GUIEvent.Key.KEY_ARROWRIGHT;
            case NormalKey:
                return getNormalKeyCharacter(key);
            default:
                System.out.println("Something went terribly wrong");
        }

        return null;
    }

    private GUIEvent.Key getNormalKeyCharacter(Key key) {
        switch (key.getCharacter()) {
            case ' ':
                AudioManager.start("sound");
                return GUIEvent.Key.KEY_SPACE;
            case 'q':
                //TODO MORE SOUNDS - GIULIANO
                return GUIEvent.Key.KEY_Q;
            case 'r':
                return GUIEvent.Key.KEY_R;
            default:
                System.out.println("Something went terribly wrong");
        }

        System.err.println("Keystroke is not mapped, returning null...");
        return null;
    }

    @Override
    public void setInputReceiver(GUIEventReceiver receiver) {
        this.receiver = receiver;
    }

    private class KeyListener implements Runnable {

        @Override
        public void run() {
            Key key;
            GUIEvent.Key k;

            while (true) {
                key = screen.readInput();

                if (key == null) {
                    continue;
                }

                if (LanternaDisplayController.this.state == State.MAIN_SCREEN) {
                    LanternaDisplayController.this.setGameLayout();
                    LanternaDisplayController.this.mainMenuBlinkExecutorService.shutdownNow();
                    LanternaDisplayController.this.state = State.IN_GAME;
                    receiver.receiveGUIEvent(GUIEvent.connect());
                    continue;
                }

                k = translateKey(key);

                if (k == GUIEvent.Key.KEY_Q) {
                    receiver.receiveGUIEvent(GUIEvent.disconnect());
                    LanternaDisplayController.this.shutdown();
                    return;
                }

                System.out.println("key " + k + " pressed!");
                receiver.receiveGUIEvent(GUIEvent.keyboardInput(k));

            }
        }
    }

    private void setGameLayout() {
        //IF LAYOUT NEEDS CHANGES
    }

    private void shutdown() {
        this.mainMenuBlinkExecutorService.shutdownNow();
        this.screen.stopScreen();
    }

    private void showFrontPage() {
        screen = TerminalFacade.createScreen();
        screen.startScreen();
        screen.getTerminal().getTerminalSize().setColumns(100);
        screen.getTerminal().getTerminalSize().setRows(30);
        screen.setCursorPosition(99, 29);
        screen.getTerminal().setCursorVisible(false);
        //TODO BACKGROUND SOUND - GIULIANO

        createScreenElements(titlePosX, titlePosY, title, Terminal.Color.WHITE);
        createScreenElements(tankPosX, tankPosY, tank, Terminal.Color.GREEN);

        mainMenuBlinkExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                AudioManager.start("sound");

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

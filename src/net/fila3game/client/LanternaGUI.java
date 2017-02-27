package net.fila3game.client;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.screen.Screen;
import net.fila3game.AudioManager;

import javax.swing.*;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by codecadet on 2/21/17.
 */
public class LanternaGUI implements GUI, GUIEventSender {

    public static final int INPUT_SCAN_DELAY = 5;

    private enum State {
        MAIN_SCREEN, IN_GAME, GAME_OVER, CREDITS
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
            "╔═╗╦═╗╔═╗╔═╗╔═╗  ╔═╗╔═╗╔═╗╔═╗╔═╗  ╔╦╗╔═╗  ╔═╗╔╦╗╔═╗╦═╗╔╦╗\n" +
                    "╠═╝╠╦╝║╣ ╚═╗╚═╗  ╚═╗╠═╝╠═╣║  ║╣    ║ ║ ║  ╚═╗ ║ ╠═╣╠╦╝ ║\n" +
                    "╩  ╩╚═╚═╝╚═╝╚═╝  ╚═╝╩  ╩ ╩╚═╝╚═╝   ╩ ╚═╝  ╚═╝ ╩ ╩ ╩╩╚═ ╩";

    private static final String serverBusy =
            "╔═╗╔═╗╦═╗╦  ╦╔═╗╦═╗  ╦╔═╗  ╔╗ ╦ ╦╔═╗╦ ╦     ╔╦╗╦═╗╦ ╦  ╔═╗╔═╗╔═╗╦╔╗╔  ╦  ╔═╗╔╦╗╔═╗╦═╗ ┬\n" +
                    "╚═╗║╣ ╠╦╝╚╗╔╝║╣ ╠╦╝  ║╚═╗  ╠╩╗║ ║╚═╗╚╦╝      ║ ╠╦╝╚╦╝  ╠═╣║ ╦╠═╣║║║║  ║  ╠═╣ ║ ║╣ ╠╦╝ │\n" +
                    "╚═╝╚═╝╩╚═ ╚╝ ╚═╝╩╚═  ╩╚═╝  ╚═╝╚═╝╚═╝ ╩ ooo   ╩ ╩╚═ ╩   ╩ ╩╚═╝╩ ╩╩╝╚╝  ╩═╝╩ ╩ ╩ ╚═╝╩╚═ o";

    private static final String messageToRestart =
            "╔═╗╦═╗╔═╗╔═╗╔═╗  ╦═╗  ╔╦╗╔═╗  ╦═╗╔═╗╔═╗╔╦╗╔═╗╦═╗╔╦╗\n" +
                    "╠═╝╠╦╝║╣ ╚═╗╚═╗  ╠╦╝   ║ ║ ║  ╠╦╝║╣ ╚═╗ ║ ╠═╣╠╦╝ ║ \n" +
                    "╩  ╩╚═╚═╝╚═╝╚═╝  ╩╚═   ╩ ╚═╝  ╩╚═╚═╝╚═╝ ╩ ╩ ╩╩╚═ ╩ ";


    private static final String gameOver =
            "  ▄████  ▄▄▄       ███▄ ▄███▓▓█████     ▒█████   ██▒   █▓▓█████  ██▀███  \n" +
                    " ██▒ ▀█▒▒████▄    ▓██▒▀█▀ ██▒▓█   ▀    ▒██▒  ██▒▓██░   █▒▓█   ▀ ▓██ ▒ ██▒\n" +
                    "▒██░▄▄▄░▒██  ▀█▄  ▓██    ▓██░▒███      ▒██░  ██▒ ▓██  █▒░▒███   ▓██ ░▄█ ▒\n" +
                    "░▓█  ██▓░██▄▄▄▄██ ▒██    ▒██ ▒▓█  ▄    ▒██   ██░  ▒██ █░░▒▓█  ▄ ▒██▀▀█▄  \n" +
                    "░▒▓███▀▒ ▓█   ▓██▒▒██▒   ░██▒░▒████▒   ░ ████▓▒░   ▒▀█░  ░▒████▒░██▓ ▒██▒\n" +
                    " ░▒   ▒  ▒▒   ▓▒█░░ ▒░   ░  ░░░ ▒░ ░   ░ ▒░▒░▒░    ░ ▐░  ░░ ▒░ ░░ ▒▓ ░▒▓░\n" +
                    "  ░   ░   ▒   ▒▒ ░░  ░      ░ ░ ░  ░     ░ ▒ ▒░    ░ ░░   ░ ░  ░  ░▒ ░ ▒░\n" +
                    "░ ░   ░   ░   ▒   ░      ░      ░      ░ ░ ░ ▒       ░░     ░     ░░   ░ \n" +
                    "      ░       ░  ░       ░      ░  ░       ░ ░        ░     ░  ░   ░     \n" +
                    "                                                     ░                   \n";

    private static final String credits =
            " ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄   ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄▄ \n" +
                    "▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░▌ ▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌\n" +
                    "▐░█▀▀▀▀▀▀▀▀▀ ▐░█▀▀▀▀▀▀▀█░▌▐░█▀▀▀▀▀▀▀▀▀ ▐░█▀▀▀▀▀▀▀█░▌ ▀▀▀▀█░█▀▀▀▀  ▀▀▀▀█░█▀▀▀▀ ▐░█▀▀▀▀▀▀▀▀▀ \n" +
                    "▐░▌          ▐░▌       ▐░▌▐░▌          ▐░▌       ▐░▌     ▐░▌          ▐░▌     ▐░▌          \n" +
                    "▐░▌          ▐░█▄▄▄▄▄▄▄█░▌▐░█▄▄▄▄▄▄▄▄▄ ▐░▌       ▐░▌     ▐░▌          ▐░▌     ▐░█▄▄▄▄▄▄▄▄▄ \n" +
                    "▐░▌          ▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░▌       ▐░▌     ▐░▌          ▐░▌     ▐░░░░░░░░░░░▌\n" +
                    "▐░▌          ▐░█▀▀▀▀█░█▀▀ ▐░█▀▀▀▀▀▀▀▀▀ ▐░▌       ▐░▌     ▐░▌          ▐░▌      ▀▀▀▀▀▀▀▀▀█░▌\n" +
                    "▐░▌          ▐░▌     ▐░▌  ▐░▌          ▐░▌       ▐░▌     ▐░▌          ▐░▌               ▐░▌\n" +
                    "▐░█▄▄▄▄▄▄▄▄▄ ▐░▌      ▐░▌ ▐░█▄▄▄▄▄▄▄▄▄ ▐░█▄▄▄▄▄▄▄█░▌ ▄▄▄▄█░█▄▄▄▄      ▐░▌      ▄▄▄▄▄▄▄▄▄█░▌\n" +
                    "▐░░░░░░░░░░░▌▐░▌       ▐░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░▌ ▐░░░░░░░░░░░▌     ▐░▌     ▐░░░░░░░░░░░▌\n" +
                    " ▀▀▀▀▀▀▀▀▀▀▀  ▀         ▀  ▀▀▀▀▀▀▀▀▀▀▀  ▀▀▀▀▀▀▀▀▀▀   ▀▀▀▀▀▀▀▀▀▀▀       ▀       ▀▀▀▀▀▀▀▀▀▀▀ ";

    private static final String creditsMessage =
            "╔═╗╦═╗╔═╗╔═╗╦═╗╔═╗╔╦╗╔╦╗╔═╗╦═╗╔═╗\n" +
                    "╠═╝╠╦╝║ ║║ ╦╠╦╝╠═╣║║║║║║║╣ ╠╦╝╚═╗\n" +
                    "╩  ╩╚═╚═╝╚═╝╩╚═╩ ╩╩ ╩╩ ╩╚═╝╩╚═╚═╝";

    private static final String chapa =
            "╔═╗┬ ┬┌─┐┌─┐┌─┐\n" +
                    "║  ├─┤├─┤├─┘├─┤\n" +
                    "╚═╝┴ ┴┴ ┴┴  ┴ ┴";

    private static final String luizord =
            "╦  ┬ ┬┬┌─┐┌─┐┬─┐┌┬┐\n" +
                    "║  │ ││┌─┘│ │├┬┘ ││\n" +
                    "╩═╝└─┘┴└─┘└─┘┴└──┴┘";

    private static final String ruben =
            "╦═╗┬ ┬┌┐ ┌─┐┌┐┌\n" +
                    "╠╦╝│ │├┴┐├┤ │││\n" +
                    "╩╚═└─┘└─┘└─┘┘└┘";

    private static final String giuli =
            "╔═╗┬┬ ┬┬  ┬┌─┐┌┐┌┌─┐\n" +
                    "║ ╦││ ││  │├─┤││││ │\n" +
                    "╚═╝┴└─┘┴─┘┴┴ ┴┘└┘└─┘\n";

    private static final int creditsX = 5;
    private static final int creditsY = 2;
    private static final int programmersX = 5;
    private static final int programmersY = 12;
    private static final int chapaX = 5;
    private static final int chapaY = 17;
    private static final int luisX = 5;
    private static final int luisY = 23;
    private static final int rubenX = 55;
    private static final int rubenY = 17;
    private static final int giuliX = 55;
    private static final int giuliY = 23;

    private static final int titlePosX = 5;
    private static final int titlePosY = 5;
    private static final int tankPosX = 70;
    private static final int tankPosY = 20;
    private static final int messagePosX = 5;
    private static final int messagePosY = 20;
    private static final int serverBusyY = 25;

    private ScheduledThreadPoolExecutor mainMenuBlinkExecutorService;
    private GUIEventReceiver receiver;
    private Screen screen;
    private State state = State.MAIN_SCREEN;
    private Thread inputThread;

    public LanternaGUI() {
        this.mainMenuBlinkExecutorService = new ScheduledThreadPoolExecutor(100);
    }

    public void init() {
        AudioManager.load(new String[]{"sound", "startMusic", "tankFire", "tankMoving","tankWasted", "creditsTheme"});
        showFrontPage();
        this.initializeInputThread();
    }

    private void initializeInputThread() {
        if (this.inputThread != null) {
            this.inputThread.interrupt();
        }

        this.inputThread = new Thread(new KeyListener());
        this.inputThread.start();
    }

    private void stopInputThread() {
        if (this.inputThread == null) {
            return;
        }

        this.inputThread.interrupt();

    }

    @Override
    public void receiveData(GameState state) {

        switch (state.getStatus()) {
            case SERVER_NOT_REACHABLE:
                this.showFrontPage();
                this.showServerBusy();
                return;
            case SERVER_FORCED_DISCONNECT:
                this.showGameOverScreen();
                AudioManager.start("tankWasted");
                System.out.println("Game Over");
                return;

        }

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
                AudioManager.start("tankFire");

                back = Terminal.Color.BLACK;
                front = Terminal.Color.MAGENTA;
                actualChar = 'A';
                break;
            case 'V':
                AudioManager.start("tankFire");

                back = Terminal.Color.BLACK;
                front = Terminal.Color.MAGENTA;
                actualChar = 'V';
                break;
            case '<':
                AudioManager.start("tankFire");

                back = Terminal.Color.BLACK;
                front = Terminal.Color.MAGENTA;
                actualChar = '<';
                break;
            case '>':
                AudioManager.start("tankFire");

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
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                back = Terminal.Color.BLACK;
                front = Terminal.Color.GREEN;
                actualChar = c;
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
//                System.out.println("Something went terribly wrong");
        }

        this.screen.putString(x, y, "" + actualChar + actualChar, back, front);
    }

    private GUIEvent.Key translateKey(Key key) {
//        System.out.println("got key! " + key);
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
//                System.out.println("Something went terribly wrong");
        }

        return null;
    }

    private GUIEvent.Key getNormalKeyCharacter(Key key) {
        switch (key.getCharacter()) {
            case ' ':
                AudioManager.stopAll();
                return GUIEvent.Key.KEY_SPACE;
            case 'q':
                return GUIEvent.Key.KEY_Q;
            case 'r':
                AudioManager.stopAll();
                return GUIEvent.Key.KEY_R;
            case 'm':
                return GUIEvent.Key.KEY_M;
            case 'c':
                return GUIEvent.Key.KEY_C;
            default:
//                System.out.println("Something went terribly wrong");
        }

//        System.err.println("Keystroke is not mapped, returning null...");
        return null;
    }

    @Override
    public void setGUIEventReceiver(GUIEventReceiver receiver) {
        this.receiver = receiver;
    }

    private class KeyListener implements Runnable {

        @Override
        public void run() {
            Key key;
            GUIEvent.Key k;

            while (true) {

                try {
                    Thread.sleep(INPUT_SCAN_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                key = screen.readInput();

                if (key == null) {
                    continue;
                }

                k = translateKey(key);

                if (k == null) {
                    continue;
                }

                switch (k) {
                    case KEY_Q:
                        receiver.receiveGUIEvent(GUIEvent.disconnect());
                        LanternaGUI.this.shutdown();
                        return;
                    case KEY_R:
                        LanternaGUI.this.receiver.receiveGUIEvent(GUIEvent.disconnect());
                        LanternaGUI.this.showFrontPage();
                        continue;
                }


                switch (LanternaGUI.this.state) {
                    case MAIN_SCREEN:
                        AudioManager.stopAll();
                        AudioManager.start("sound");
                        LanternaGUI.this.mainMenuBlinkExecutorService.shutdownNow();

                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        AudioManager.stopAll();

                        LanternaGUI.this.state = State.IN_GAME;
                        receiver.receiveGUIEvent(GUIEvent.connect());
                        continue;
                    case GAME_OVER:
                        switch (k) {
                            case KEY_C:
                            LanternaGUI.this.showCreditsScreen();
                            continue;
                        }
                        break;
                    case IN_GAME:
                        switch (k) {
                            case KEY_ARROWDOWN:
                            case KEY_ARROWLEFT:
                            case KEY_ARROWRIGHT:
                            case KEY_ARROWUP:
                                AudioManager.start("tankMoving");
                                break;
                        }
                        break;
                    case CREDITS:
                        continue;

                }

                receiver.receiveGUIEvent(GUIEvent.keyboardInput(k));

            }
        }
    }

    private void shutdown() {
        AudioManager.stopAll();
        this.mainMenuBlinkExecutorService.shutdownNow();
        this.stopInputThread();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                screen.stopScreen();
            }
        });
    }

    private void showFrontPage() {
        if (this.screen == null) {
            this.initializeScreen();
        }

        this.screen.clear();

        AudioManager.stopAll();
        AudioManager.loop("startMusic", 2);

        this.state = State.MAIN_SCREEN;

        createScreenElements(titlePosX, titlePosY, title, Terminal.Color.WHITE);
        createScreenElements(tankPosX, tankPosY, tank, Terminal.Color.GREEN);

        if (this.mainMenuBlinkExecutorService != null) {
            this.mainMenuBlinkExecutorService.shutdownNow();
        }

        this.mainMenuBlinkExecutorService = new ScheduledThreadPoolExecutor(2);

        this.mainMenuBlinkExecutorService.scheduleAtFixedRate(new Runnable() {
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
        this.state = State.MAIN_SCREEN;
    }

    private void showGameOverScreen() {

        if (this.screen == null) {
            this.initializeScreen();
        }

        this.screen.clear();

        createScreenElements(titlePosX, titlePosY, gameOver, Terminal.Color.RED);
        createScreenElements(messagePosX, messagePosY, messageToRestart, Terminal.Color.YELLOW);
        this.screen.refresh();

        this.state = State.GAME_OVER;
    }

    private void createScreenElements(int x, int y, String text, Terminal.Color color) {

        Scanner scanner = new Scanner(text);

        while (scanner.hasNextLine()) {

            screen.putString(x, y, scanner.nextLine(), color, Terminal.Color.BLACK);
            y++;
        }
    }

    private void showCreditsScreen() {

        if (this.screen == null) {
            this.initializeScreen();
        }

        this.screen.clear();

        createScreenElements(creditsX, creditsY, credits, Terminal.Color.WHITE);
        createScreenElements(programmersX, programmersY, creditsMessage, Terminal.Color.YELLOW);
        createScreenElements(chapaX, chapaY, chapa, Terminal.Color.CYAN);
        createScreenElements(luisX, luisY, luizord, Terminal.Color.CYAN);
        createScreenElements(rubenX, rubenY, ruben, Terminal.Color.CYAN);
        createScreenElements(giuliX, giuliY, giuli, Terminal.Color.CYAN);

        this.screen.refresh();
        AudioManager.start("creditsTheme");
        this.state = State.CREDITS;
    }

    private void showServerBusy() {
        createScreenElements(messagePosX, serverBusyY, serverBusy, Terminal.Color.RED);
    }

    private void initializeScreen() {
        screen = TerminalFacade.createScreen();
        screen.startScreen();
        screen.getTerminal().getTerminalSize().setColumns(100);
        screen.getTerminal().getTerminalSize().setRows(30);
        screen.setCursorPosition(99, 29);
        screen.getTerminal().setCursorVisible(false);
    }


}

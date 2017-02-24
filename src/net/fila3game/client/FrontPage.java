package net.fila3game.client;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;

import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by codecadet on 2/24/17.
 */
public class FrontPage {

    public static void main(String[] args) {

        FrontPage frontPage = new FrontPage();
    }

    private Screen SCREEN;

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

    private static final String instructions =
                    "╔═╗╦═╗╔═╗╔═╗╔═╗  ╔═╗╔╗╔╦ ╦  ╦╔═╔═╗╦ ╦  ╔╦╗╔═╗  ╔═╗╔╦╗╔═╗╦═╗╔╦╗\n" +
                    "╠═╝╠╦╝║╣ ╚═╗╚═╗  ╠═╣║║║╚╦╝  ╠╩╗║╣ ╚╦╝   ║ ║ ║  ╚═╗ ║ ╠═╣╠╦╝ ║ \n" +
                    "╩  ╩╚═╚═╝╚═╝╚═╝  ╩ ╩╝╚╝ ╩   ╩ ╩╚═╝ ╩    ╩ ╚═╝  ╚═╝ ╩ ╩ ╩╩╚═ ╩ ";

    public FrontPage() {

        SCREEN = TerminalFacade.createScreen();
        SCREEN.startScreen();
        SCREEN.setCursorPosition(99, 29);

        createScreenElements(5, 5, title, Terminal.Color.WHITE);
        createScreenElements(70, 20, tank, Terminal.Color.GREEN);

        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                createScreenElements(5, 20, instructions, Terminal.Color.YELLOW);
                SCREEN.refresh();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                createScreenElements(5, 20, instructions, Terminal.Color.BLACK);
                SCREEN.refresh();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Key key = SCREEN.readInput();

                if (key != null) {
                    executorService.shutdownNow();
                    SCREEN.stopScreen();

                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

    }


    private void createScreenElements(int x, int y, String text, Terminal.Color color) {

        Scanner scanner = new Scanner(text);

        while (scanner.hasNextLine()) {

            SCREEN.putString(x, y, scanner.nextLine(), color, Terminal.Color.BLACK);
            y++;
        }
    }
}

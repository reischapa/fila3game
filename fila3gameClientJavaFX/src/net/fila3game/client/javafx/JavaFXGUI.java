package net.fila3game.client.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import net.fila3game.client.backend.*;

/**
 * Created by codecadet on 2/28/17.
 */
public class JavaFXGUI extends Application implements GUI, GUIEventSender {

    private enum State {
        MAIN_SCREEN, IN_GAME, GAME_OVER, CREDITS
    }


    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 600;

    private static final int N_CELLS_WIDTH = 50;
    private static final int N_CELLS_HEIGHT = 30;

    private static final String[] STYLE_STRINGS = {"cell-tank", "cell-bullet", "cell-wall", "cell-empty", "cell-number", "cell-mine"};


    public static void main(String[] args) {
        launch(args);
    }


    private Label[][] cells;
    private GridPane battlefield;
    private Scene scene;

    private GUIEventReceiver guiEventReceiver;
    private State state = State.MAIN_SCREEN;


    public void init() {
        this.cells = new Label[N_CELLS_WIDTH][N_CELLS_HEIGHT];
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setResizable(false);

        this.initializeBattlefield();
        this.setUpKeyboard();

        primaryStage.setScene(this.scene);
        primaryStage.show();

        GameClient gc = null;

        if (gc == null) {
            gc = new GameClient();
        }

        this.setGUIEventReceiver(gc);
        gc.setGUI(this);

        this.guiEventReceiver.receiveGUIEvent(GUIEvent.connect());

    }


    private void initializeBattlefield() {
        this.battlefield = new GridPane();

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        RowConstraints rc = new RowConstraints();
        rc.setVgrow(Priority.ALWAYS);

        this.battlefield.getStylesheets().add(this.getClass().getResource("/styles.css").toString());
        this.battlefield.getStyleClass().addAll("battlefield");

        for (int i : new int[N_CELLS_WIDTH]) {
            this.battlefield.getColumnConstraints().add(cc);
        }

        for (int i : new int[N_CELLS_HEIGHT]) {
            this.battlefield.getRowConstraints().add(rc);
        }

        for (int y = 0; y < N_CELLS_HEIGHT; y++ ) {
            for (int x = 0; x < N_CELLS_WIDTH; x++) {
                this.cells[x][y] = new Label();
                this.cells[x][y].getStyleClass().add("cell-empty");
                this.cells[x][y].setMaxWidth(Double.MAX_VALUE);
                this.cells[x][y].setMaxHeight(Double.MAX_VALUE);
                this.battlefield.add(this.cells[x][y], x, y);
            }
        }

        this.battlefield.setMaxSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.battlefield.setMinSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        this.scene = new Scene(this.battlefield,WINDOW_WIDTH, WINDOW_HEIGHT);
    }


    private void setUpKeyboard() {

        if (this.scene == null) {
            return;
        }

        this.scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:
                        guiEventReceiver.receiveGUIEvent(GUIEvent.keyboardInput(GUIEvent.Key.KEY_ARROWUP));
                        break;
                    case DOWN:
                        guiEventReceiver.receiveGUIEvent(GUIEvent.keyboardInput(GUIEvent.Key.KEY_ARROWDOWN));
                        break;
                    case LEFT:
                        guiEventReceiver.receiveGUIEvent(GUIEvent.keyboardInput(GUIEvent.Key.KEY_ARROWLEFT));
                        break;
                    case RIGHT:
                        guiEventReceiver.receiveGUIEvent(GUIEvent.keyboardInput(GUIEvent.Key.KEY_ARROWRIGHT));
                        break;
                    case SPACE:
                        guiEventReceiver.receiveGUIEvent(GUIEvent.keyboardInput(GUIEvent.Key.KEY_SPACE));
                        break;
                    case M:
                        guiEventReceiver.receiveGUIEvent(GUIEvent.keyboardInput(GUIEvent.Key.KEY_M));
                        break;
                }
            }
        });


    }



    @Override
    public void receiveData(GameState state) {
        switch (state.getStatus()) {
            case SERVER_NOT_REACHABLE:
                return;
            case SERVER_FORCED_DISCONNECT:
                return;
        }

        final String[] lines = state.getFieldString().split("\n");

        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (int y = 0; y < lines.length; y++) {
                    char[] chars = lines[y].toCharArray();

                    for (int x = 0; x < chars.length; x++) {
                        occupyCell(x,y,chars[x]);
                    }
                }
            }
        };

        Platform.runLater(run);

    }

    private void occupyCell(int x, int y, char c) {

        char actualChar = ' ';

        String classString = "";

        switch (c) {
            case 'T':
                classString = "cell-tank";
                break;
            case 'A':
            case 'V':
            case '<':
            case '>':
                classString = "cell-bullet";
                break;
            case 'W':
                classString = "cell-wall";
                break;
            case '0':
                classString = "cell-empty";
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
                classString = "cell-number";
                actualChar = c;
                break;
            case '@':
                classString = "cell-mine";
                actualChar = '@';
            default:
//                System.out.println("Something went terribly wrong");
        }

        if (this.cells == null) {
            return;
        }

        this.cells[x][y].getStyleClass().clear();
        this.cells[x][y].getStyleClass().add(classString);

    }



    @Override
    public void setGUIEventReceiver(GUIEventReceiver receiver) {
        this.guiEventReceiver = receiver;
    }
}
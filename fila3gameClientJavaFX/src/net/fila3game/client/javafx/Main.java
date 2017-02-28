package net.fila3game.client.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.fila3game.client.backend.GUIEventReceiver;
import net.fila3game.client.backend.GUIEventSender;

/**
 * Created by codecadet on 2/28/17.
 */
public class Main extends Application  {

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane root = new GridPane();
        root.setGridLinesVisible(true);
        root.setAlignment(Pos.CENTER);

        root.addColumn(0);

        Scene s = new Scene(root,400, 400);
        primaryStage.setScene(s);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
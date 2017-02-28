package net.fila3game.server.gameengine;

/**
 * Created by codecadet on 2/28/17.
 */
public class GameEngineResponse {

    private final String fieldString;

    public GameEngineResponse(String fieldString) {
        this.fieldString = fieldString;
    }

    public String getFieldString() {
        return fieldString;
    }
}

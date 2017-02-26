package net.fila3game.client;

/**
 * Created by codecadet on 2/21/17.
 */
public class GameState {

    public static GameState serverNotAvailable() {
        return new GameState(ConnectionStatus.SERVER_NOT_REACHABLE);
    }

    public static void main(String[] args) {
        GameState state = new GameState("1 0\r\nTTT\nTTT\n0T0\n");
    }

    public enum ConnectionStatus {
        OK, SERVER_NOT_REACHABLE, SERVER_INVALID_ADDRESS,
    }


    private String fieldString;


    private ConnectionStatus status;


    public GameState(String dataString) {
        this.fieldString = dataString;
        this.status = ConnectionStatus.OK;
    }

    public GameState( ConnectionStatus status ) {
        this.fieldString = null;
        this.status = status;
    }

    public void parseString(String dataString) {
        String[] totalSplit = dataString.split("\r\n");
        String[] data = totalSplit[0].split(" ");

        this.fieldString = totalSplit[1];

   }

    public String getFieldString() {
        return fieldString;
    }

    public ConnectionStatus getStatus() {
        return status;
    }
}

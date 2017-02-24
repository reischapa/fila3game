package net.fila3game.client;

/**
 * Created by codecadet on 2/21/17.
 */
public class GameState {

    public static void main(String[] args) {
        GameState state = new GameState("1 0\r\nTTT\nTTT\n0T0\n");
        System.out.println(state.getPlayerX());
        System.out.println(state.getPlayerY());
        System.out.println(state.getFieldString());
    }

    private String fieldString;


    private int playerY;
    private int playerX;

    public GameState(String dataString) {
        System.out.println(dataString);
        this.parseString(dataString);
    }

    public void parseString(String dataString) {
        String[] totalSplit = dataString.split("\r\n");
        String[] data = totalSplit[0].split(" ");

        this.fieldString = totalSplit[1];

        this.playerX = Integer.parseInt(data[0]);
        this.playerY = Integer.parseInt(data[1]);

   }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public String getFieldString() {
        return fieldString;
    }
}

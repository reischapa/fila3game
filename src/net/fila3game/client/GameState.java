package net.fila3game.client;

/**
 * Created by codecadet on 2/21/17.
 */
public class GameState {

    public static void main(String[] args) {
        GameState state = new GameState("1 0\r\nTTT\nTTT\n0T0\n");
    }

    private String fieldString;


    public GameState(String dataString) {
//        System.out.println(dataString);
//        this.parseString(dataString);
        this.fieldString = dataString;
    }

    public void parseString(String dataString) {
        String[] totalSplit = dataString.split("\r\n");
        String[] data = totalSplit[0].split(" ");

        this.fieldString = totalSplit[1];

   }

    public String getFieldString() {
        return fieldString;
    }
}

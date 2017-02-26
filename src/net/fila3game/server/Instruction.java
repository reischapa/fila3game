package net.fila3game.server;

/**
 * Created by codecadet on 2/21/17.
 */
public class Instruction {

    public enum Type {
        U,D,R,L,M,S
    }

    private int playerNumber;
    private Instruction.Type type;

    public Instruction(String in) {
        System.out.println(in);

        String[] results = in.split(" ");

        this.playerNumber = Integer.parseInt(results[0]);
        this.type = Type.valueOf(results[1]);
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Type getType() {
        return type;
    }
}

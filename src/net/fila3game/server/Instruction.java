package net.fila3game.server;

/**
 * Created by codecadet on 2/21/17.
 */
public class Instruction {

    public enum Type {
        U,D,R,L,S;
    }

    private int playerNumber;
    private Instruction.Type type;

    public Instruction(String in) {
        this.type = Type.R;
        this.playerNumber = 0;
        this.playerNumber = playerNumber;
        this.type = type;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Type getType() {
        return type;
    }
}

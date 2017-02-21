package net.fila3game.server;

/**
 * Created by codecadet on 2/21/17.
 */
public class Instruction {

    public enum Type {
        N,D,
    }

    private int playerNumber;
    private Instruction.Type type;

    public Instruction(int playerNumber, Type type) {
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

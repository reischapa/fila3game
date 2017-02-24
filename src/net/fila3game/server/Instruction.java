package net.fila3game.server;

/**
 * Created by codecadet on 2/21/17.
 */
public class Instruction {

    public static void main(String[] args) {
        Instruction i = new Instruction("0 D");
        System.out.println(i.getType());
    }

    public enum Type {
        U,D,R,L,S;


    }

    private int playerNumber;
    private Instruction.Type type;

    public Instruction(String in) {

        String[] results = in.split(" ");

        this.type = Type.valueOf(results[1]);
        this.playerNumber = 0;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Type getType() {
        return type;
    }
}

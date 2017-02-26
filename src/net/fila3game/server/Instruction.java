package net.fila3game.server;

/**
 * Created by codecadet on 2/21/17.
 */
public class Instruction {

    public static void main(String[] args) {
        Instruction i = new Instruction("1 D");
        System.out.println(i.getType());
        System.out.println(i.getPlayerNumber());
    }

    public enum Type {
        U,D,R,L,S,M
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

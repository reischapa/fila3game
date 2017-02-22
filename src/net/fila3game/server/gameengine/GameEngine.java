package net.fila3game.server.gameengine;

import net.fila3game.server.Instruction;

/**
 * Created by codecadet on 2/21/17.
 */
public class GameEngine {

    public enum Tiletypes {

        WALL("W"),
        TANK("T"),
        BULLET("B");

        private String symbol;

        Tiletypes(String b) {
            this.symbol = b;
        }

        public String getSymbol(){
            return symbol;
        }
    }

    private Field battlefield;

    public GameEngine(Field battlefield){

        this.battlefield = battlefield;

    }

    public void receiveInstruction(Instruction i) {

        System.out.println(i.getPlayerNumber());
        System.out.println(i.getType());
        System.out.println(battlefield.returnAsString());
    }

    //calculate and return the state
    public String calculateState() {
        throw new UnsupportedOperationException();
    }

    private boolean checkCollision(){return false;}

}

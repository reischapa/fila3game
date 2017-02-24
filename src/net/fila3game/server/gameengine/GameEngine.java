package net.fila3game.server.gameengine;

import net.fila3game.server.Instruction;
import net.fila3game.server.gameengine.gameobjects.*;
import net.fila3game.server.gameengine.gameobjects.GameObject;

import java.util.LinkedList;

/**
 * Created by codecadet on 2/21/17.
 */
public class GameEngine {

    public enum Tiletypes {

        WALL('W'),
        TANK('T'),
        BULLET('B');

        private char symbol;

        Tiletypes(char b) {
            this.symbol = b;
        }

        public char getSymbol(){
            return symbol;
        }
    }

    private Field battlefield;
    private int numberOfTanks;
    private LinkedList<Tank> tankList;
    private final Field EMPTYMASK = new Field(3,3);


    public GameEngine(Field battlefield){

        this.battlefield = battlefield;
        numberOfTanks = 0;
        tankList = new LinkedList<>();



    }

    public void receiveInstruction(Instruction i) {

        Tank tank = tankList.get(i.getPlayerNumber());

        if(i.getType().equals(Instruction.Type.R)) {
            tank.setOrientation(RepresentationFactory.Orientation.EAST);
            battlefield.addField(EMPTYMASK,tank.getX(),tank.getY());
            tank.move(tank.getX()+1,tank.getY());
            battlefield.addField(tank.getRepresentation(),tank.getX(),tank.getY());

//            if(checkCollision(tank)){
//
//                tank.move(tank.getX()-1,tank.getY());
//
//            }

        }

    }

    public boolean addTank(){

        if(numberOfTanks == 0){

            Tank tank = new Tank(0,3,2, RepresentationFactory.Orientation.EAST);

//            if(!checkCollision(tank)){
                battlefield.addField(tank.getRepresentation(),tank.getX(),tank.getY());
                tank.setPlayer(0);
                tankList.add(tank);
                numberOfTanks++;
                return true;
   //         }
        }

        return false;


    }

    //calculate and return the state
    public String calculateState() {
        return battlefield.returnAsString();
    }

    private boolean checkCollision(GameObject object){

        for(int i = object.getX(); i < object.getWidth(); i++) {

            for(int j = object.getY(); i < object.getHeight(); j++) {

                if (battlefield.get(i,j) == Tiletypes.WALL.getSymbol() || battlefield.get(i,j) == Tiletypes.BULLET.getSymbol()|| battlefield.get(i,j) == Tiletypes.TANK.getSymbol()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {

        Field field = new Field(20,20);
        GameEngine gameEngine = new GameEngine(field);
        gameEngine.addTank();
        System.out.println(field.returnAsString());
        Instruction i = new Instruction(0, Instruction.Type.R);
        gameEngine.receiveInstruction(i);
        System.out.println(field.returnAsString());

    }

}

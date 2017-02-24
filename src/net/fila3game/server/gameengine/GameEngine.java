package net.fila3game.server.gameengine;

import net.fila3game.server.Instruction;
import net.fila3game.server.gameengine.gameobjects.GameObject;
import net.fila3game.server.gameengine.gameobjects.RepresentationFactory;
import net.fila3game.server.gameengine.gameobjects.Tank;

import java.util.LinkedList;

/**
 * Created by codecadet on 2/21/17.
 */
public class GameEngine {

    private static final int MAX_NUMBER_TANKS = 2;

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

    public synchronized void receiveInstruction(Instruction i) {

        Tank tank = tankList.get(i.getPlayerNumber());

        if(i.getType().equals(Instruction.Type.R)) {
            tank.setOrientation(RepresentationFactory.Orientation.EAST);
            moveTank(tank,1,0);

//            if(checkCollision(tank)){
//
//                tank.move(tank.getX()-1,tank.getY());
//
//            }

        }else if(i.getType().equals(Instruction.Type.L)){
            tank.setOrientation(RepresentationFactory.Orientation.WEST);
            moveTank(tank,-1,0);

        }else if(i.getType().equals(Instruction.Type.U)) {
            System.out.println("entrei");
            System.out.println(tank.getY());
            tank.setOrientation(RepresentationFactory.Orientation.NORTH);
            moveTank(tank,0,-1);

        }else if(i.getType().equals(Instruction.Type.D)){
            tank.setOrientation(RepresentationFactory.Orientation.SOUTH);
            moveTank(tank,0,1);
        }
    }

    private void moveTank(Tank tank, int x, int y){
        battlefield.addField(EMPTYMASK, tank.getX(), tank.getY());
        tank.move(tank.getX()+x, tank.getY()+y);
        battlefield.addField(tank.getRepresentation(), tank.getX(), tank.getY());
    }


    public synchronized boolean addTank(){

        if( numberOfTanks < MAX_NUMBER_TANKS) {

            if (tankList.size() == 0) {

                Tank tank = new Tank(0, 3, 2, RepresentationFactory.Orientation.EAST);

//            if(!checkCollision(tank)){
                battlefield.addField(tank.getRepresentation(), tank.getX(), tank.getY());
                tank.setPlayer(0);
                tankList.add(tank);
                numberOfTanks++;
                return true;
                //         }
            } else if (tankList.size() == 1) {

                Tank tank = new Tank(1, battlefield.getWidth() - 2 - RepresentationFactory.TANK_WIDTH, battlefield.getHeight() / 2, RepresentationFactory.Orientation.WEST);

//            if(!checkCollision(tank)){
                System.out.println(tank.getX());
                System.out.println(tank.getY());
                battlefield.addField(tank.getRepresentation(), tank.getX(), tank.getY());
                tank.setPlayer(1);
                tankList.add(tank);
                numberOfTanks++;
                return true;
                //         }
            }
        }

        return false;


    }

    //calculate and return the state
    public synchronized String calculateState() {
        return battlefield.returnAsString();
    }

    private synchronized boolean checkCollision(GameObject object){

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

        Field field = new Field(25,25);
        Field mainField = new Field(25,25);
        for(int puta = 0; puta < mainField.getWidth(); puta++){
            for(int cona = 0; cona < mainField.getHeight(); cona++ ){
                if(puta == 0 || cona == 0 || puta == mainField.getWidth()-1 || cona == mainField.getHeight()-1) {
                    mainField.set(puta, cona, Tiletypes.WALL.getSymbol());
                }
            }
        }
        GameEngine gameEngine = new GameEngine(mainField);
        gameEngine.addTank();
        gameEngine.addTank();
        System.out.println(mainField.returnAsString());
        Instruction i = new Instruction(0, Instruction.Type.R);
        Instruction i2 = new Instruction(1,Instruction.Type.L);
        gameEngine.receiveInstruction(i);
        gameEngine.receiveInstruction(i2);
        System.out.println(mainField.returnAsString());
        gameEngine.receiveInstruction(new Instruction(1, Instruction.Type.U));
        gameEngine.receiveInstruction(new Instruction(1, Instruction.Type.U));
        System.out.println(mainField.returnAsString());

    }

}

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
    public static final int DEFAULT_BATTLEFIELD_COLUMNS = 30;
    public static final int DEFAULT_BATTLEFIELD_ROWS = 30;

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
    private static final Field EMPTYMASK = new Field(RepresentationFactory.TANK_WIDTH,RepresentationFactory.TANK_HEIGHT);


    public GameEngine(Field battlefield){

        this.battlefield = battlefield;
        numberOfTanks = 0;
        tankList = new LinkedList<>();

    }

    public GameEngine (){

        this.battlefield = createDefaultField();
        numberOfTanks = 0;
        tankList = new LinkedList<>();

    }

    public synchronized void receiveInstruction(Instruction i) {

        Tank tank = tankList.get(i.getPlayerNumber());

        if(i.getType().equals(Instruction.Type.R)) {
            tank.setOrientation(RepresentationFactory.Orientation.EAST);
            moveTank(tank,1,0);

        }else if(i.getType().equals(Instruction.Type.L)){
            tank.setOrientation(RepresentationFactory.Orientation.WEST);
            moveTank(tank,-1,0);

        }else if(i.getType().equals(Instruction.Type.U)) {
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

        if(checkCollision(tank)){

            System.out.println("colision");
            tank.move(tank.getX()-x,tank.getY()-y);
            return;

        }

        battlefield.addField(tank.getRepresentation(), tank.getX(), tank.getY());
    }


    public synchronized int addTank(){

        if( numberOfTanks < MAX_NUMBER_TANKS) {

            if (tankList.size() == 0) {

                Tank tank = new Tank(0, 3, 2, RepresentationFactory.Orientation.EAST);
                return createTank(tank);

            } else if (tankList.size() == 1) {

                Tank tank = new Tank(1, battlefield.getWidth() - 2 - RepresentationFactory.TANK_WIDTH, battlefield.getHeight() / 2, RepresentationFactory.Orientation.WEST);
                return createTank(tank);
            }
        }

        return -1;


    }

    private int createTank(Tank tank){

        if(!checkCollision(tank)){

            battlefield.addField(tank.getRepresentation(), tank.getX(), tank.getY());
            tankList.add(tank);
            numberOfTanks++;
            return tank.getPlayer();

        }

        return -1;
    }

    //calculate and return the state
    public synchronized String calculateState() {
        return battlefield.returnAsString();
    }

    private synchronized boolean checkCollision(GameObject object){

        for(int i = object.getX(); i < object.getX()+object.getWidth(); i++) {

            for(int j = object.getY(); j < object.getY()+3; j++) {

                if (battlefield.get(i,j) == Tiletypes.WALL.getSymbol() || battlefield.get(i,j) == Tiletypes.BULLET.getSymbol()|| battlefield.get(i,j) == Tiletypes.TANK.getSymbol()) {
                    System.out.println("why dude wtf");
                    return true;
                }
            }
        }

        return false;
    }

    private Field createDefaultField(){

        Field mainField = new Field(DEFAULT_BATTLEFIELD_COLUMNS,DEFAULT_BATTLEFIELD_ROWS);

        for(int x = 0; x < mainField.getWidth(); x++){

            for(int y = 0; y < mainField.getHeight(); y++ ){

                if(x == 0 || y == 0 || x == mainField.getWidth()-1 || y == mainField.getHeight()-1) {

                    mainField.set(x, y, Tiletypes.WALL.getSymbol());
                }
            }
        }

        return mainField;

    }

    public static void main(String[] args) {


        GameEngine gameEngine = new GameEngine();
        gameEngine.addTank();
        gameEngine.addTank();
        System.out.println(gameEngine.calculateState());
        gameEngine.receiveInstruction(new Instruction("0 D"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        gameEngine.receiveInstruction(new Instruction("0 R"));
        System.out.println(gameEngine.calculateState());


    }

}

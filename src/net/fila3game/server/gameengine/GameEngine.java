package net.fila3game.server.gameengine;

import net.fila3game.server.Instruction;
import net.fila3game.server.gameengine.gameobjects.Bullet;
import net.fila3game.server.gameengine.gameobjects.GameObject;
import net.fila3game.server.gameengine.gameobjects.RepresentationFactory;
import net.fila3game.server.gameengine.gameobjects.Tank;

import java.util.LinkedList;

/**
 * Created by codecadet on 2/21/17.
 */
public class GameEngine {

    private static final int MAX_NUMBER_TANKS = 2;
    public static final int DEFAULT_BATTLEFIELD_COLUMNS = 50;
    public static final int DEFAULT_BATTLEFIELD_ROWS = 30;

    public enum Tiletypes {

        WALL('W'),
        TANK('T'),
        BULLET_R('>'),
        BULLET_L('<'),
        BULLET_U('A'),
        BULLET_D('V');

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
    private LinkedList<Bullet> bullets;
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
        bullets = new LinkedList<>();

    }

    public synchronized void receiveInstruction(Instruction i) {

        if(!tankList.isEmpty()) {
            Tank tank = tankList.get(i.getPlayerNumber());

            if (i.getType().equals(Instruction.Type.R)) {
                tank.setOrientation(RepresentationFactory.Orientation.EAST);
                moveTank(tank, 1, 0);

            } else if (i.getType().equals(Instruction.Type.L)) {
                tank.setOrientation(RepresentationFactory.Orientation.WEST);
                moveTank(tank, -1, 0);

            } else if (i.getType().equals(Instruction.Type.U)) {
                tank.setOrientation(RepresentationFactory.Orientation.NORTH);
                moveTank(tank, 0, -1);

            } else if (i.getType().equals(Instruction.Type.D)) {
                tank.setOrientation(RepresentationFactory.Orientation.SOUTH);
                moveTank(tank, 0, 1);

            }else if (i.getType().equals(Instruction.Type.S)) {


                Bullet bullet = createBullet(tank);
                moveBullet(bullet,1,0);

            }
        }
    }

    private Bullet createBullet(Tank tank){

        Bullet bullet = null;

        if(tank.getOrientation().equals(RepresentationFactory.Orientation.WEST)){

            bullet = new Bullet(tank.getPlayer(),tank.getX()+4,tank.getY()+1, RepresentationFactory.Orientation.WEST);

        }else if(tank.getOrientation().equals(RepresentationFactory.Orientation.EAST)){

            bullet = new Bullet(tank.getPlayer(),tank.getX()+4,tank.getY()+1, RepresentationFactory.Orientation.EAST);


        }

        bullets.add(bullet);

        return bullet;
    }

    private void moveBullet(Bullet bullet, int x, int y){

        battlefield.addField(EMPTYMASK, bullet.getX(), bullet.getY());
        bullet.move(bullet.getX()+x,bullet.getY()+y);

//        for(Tank t : tankList) {
//            if (checkTankCollision(t)) {
//                System.out.println("cock");
//                battlefield.addField(EMPTYMASK,t.getX(), t.getY());
//                battlefield.addField(EMPTYMASK,bullet.getX(),bullet.getY());
//                tankList.remove(t);
//                bullets.remove(bullet);
//                numberOfTanks--;
//            }
//        }
        if(checkBulletCollision(bullet)){
            System.out.println("bitch");
            battlefield.addField(EMPTYMASK,bullet.getX(), bullet.getY());
            tankList.remove(bullet);
            return;
        }

        battlefield.addField(bullet.getRepresentation(), bullet.getX(), bullet.getY());

    }

    private void moveTank(Tank tank, int x, int y){

        battlefield.addField(EMPTYMASK, tank.getX(), tank.getY());
        tank.move(tank.getX()+x, tank.getY()+y);

        if(checkTankCollision(tank)){

            tank.move(tank.getX()-x,tank.getY()-y);

        }

        if(checkBulletCollision(tank)){

            battlefield.addField(EMPTYMASK,tank.getX(), tank.getY());
            tankList.remove(tank);
            numberOfTanks--;
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

        if(!checkTankCollision(tank)){

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

    private synchronized boolean checkTankCollision(GameObject object){

        for(int i = object.getX(); i < object.getX()+object.getWidth(); i++) {

            for(int j = object.getY(); j < object.getY()+object.getHeight(); j++) {

                if (battlefield.get(i,j) == Tiletypes.WALL.getSymbol() || battlefield.get(i,j) == Tiletypes.TANK.getSymbol()) {
                    System.out.println("Collision");
                    return true;
                }
            }
        }

        return false;
    }

    private synchronized boolean checkBulletCollision(GameObject object){

        for(int i = object.getX(); i < object.getX()+object.getWidth(); i++) {

            for(int j = object.getY(); j < object.getY()+object.getHeight(); j++) {

                if (battlefield.get(i,j) == Tiletypes.BULLET_D.getSymbol() || battlefield.get(i,j) == Tiletypes.BULLET_U.getSymbol() ||
                        battlefield.get(i,j) == Tiletypes.BULLET_L.getSymbol() || battlefield.get(i,j) == Tiletypes.BULLET_R.getSymbol()) {
                    System.out.println("Bullet Collision");
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
        gameEngine.battlefield.addField(new Bullet(1,5,5, RepresentationFactory.Orientation.EAST).getRepresentation(),5,5);
        gameEngine.battlefield.addField(new Bullet(1,10,10, RepresentationFactory.Orientation.WEST).getRepresentation(),10,10);
        System.out.println(gameEngine.calculateState());
        gameEngine.receiveInstruction(new Instruction("0 S"));

        System.out.println(gameEngine.calculateState());


    }

}

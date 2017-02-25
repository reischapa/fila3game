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

    private static final int MAX_NUMBER_TANKS = 4;
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
    private final Field EMPTYMASK = new Field(RepresentationFactory.TANK_WIDTH,RepresentationFactory.TANK_HEIGHT);
    private final Field EMPTYBULLET = new Field(1,1);


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
            Tank tank = tankList.get(i.getPlayerNumber()-1);
            if(tank.isAlive()) {

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

                } else if (i.getType().equals(Instruction.Type.S)) {

                    if(!bulletExists(tank.getPlayer())) {
                        Bullet bullet = createBullet(tank);
                        bullets.add(bullet);
                    }

                }
            }
        }
    }

    private boolean bulletExists(int tankID) {
        if(!bullets.isEmpty()) {
            for (Bullet bullet : bullets) {
                if (bullet.getPlayer() == tankID) {
                    return true;
                }
            }
        }
        return false;
    }

    private Bullet createBullet(Tank tank){

        Bullet bullet = null;

        if(tank.getOrientation().equals(RepresentationFactory.Orientation.WEST)){

            bullet = new Bullet(tank.getPlayer(),tank.getX()-1,tank.getY()+1, RepresentationFactory.Orientation.WEST);

        }else if(tank.getOrientation().equals(RepresentationFactory.Orientation.EAST)){

            bullet = new Bullet(tank.getPlayer(),tank.getX()+3,tank.getY()+1, RepresentationFactory.Orientation.EAST);

        }else if(tank.getOrientation().equals(RepresentationFactory.Orientation.SOUTH)){

            bullet = new Bullet(tank.getPlayer(),tank.getX()+1,tank.getY()+3, RepresentationFactory.Orientation.SOUTH);

        }else {

            bullet = new Bullet(tank.getPlayer(),tank.getX()+1,tank.getY()-1, RepresentationFactory.Orientation.NORTH);

        }

        if(!checkWallColistion(bullet) && !checkBulletCollision(bullet)){

            battlefield.addField(bullet.getRepresentation(),bullet.getX(),bullet.getY());
        }

        return bullet;
    }

    private synchronized void moveBullet(Bullet bullet){

        battlefield.addField(new Field(1,1), bullet.getX(), bullet.getY());
        int x = 0;
        int y = 0;

        switch (bullet.getOrientation()){
            case NORTH:
                y = -1;
                break;
            case SOUTH:
                y = 1;
                break;
            case EAST:
                x = 1;
                break;
            case WEST:
                x = -1;
                break;
            default:
                x = 0;
                y = 0;
                break;
        }

        bullet.move(bullet.getX()+x,bullet.getY()+y);

        for(Tank t : tankList) {

            battlefield.addField(EMPTYMASK,t.getX(),t.getY());

            if (checkBulletCollision(t)) {

                System.out.println("colidiu");
                battlefield.addField(EMPTYMASK,t.getX(), t.getY());
                battlefield.addField(EMPTYBULLET,bullet.getX(),bullet.getY());
                tankList.remove(t);
                t.die();
                bullet.die();
                bullets.remove(bullet);
                numberOfTanks--;
                return;
            }

            battlefield.addField(t.getRepresentation(),t.getX(),t.getY());
        }

        for(Bullet otherbullet : bullets){
            if(otherbullet.getPlayer() != bullet.getPlayer()){
                if(checkBulletCollision(otherbullet)){
                    System.out.println("bullet on bullet collision");
                    battlefield.addField(EMPTYBULLET,bullet.getX(), bullet.getY());
                    battlefield.addField(EMPTYBULLET,otherbullet.getX(), otherbullet.getY());
                    bullet.die();
                    otherbullet.die();
                    bullets.remove(bullet);
                    bullets.remove(otherbullet);
                    return;
                }
            }
        }

        if(checkWallColistion(bullet)){

            System.out.println("bitch");
            Field wallField = new Field(1,1);
            wallField.set(0,0,'W');
            battlefield.addField(wallField,bullet.getX(), bullet.getY());
            bullet.die();
            bullets.remove(bullet);
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
            System.out.println("wtfboy");

            battlefield.addField(EMPTYMASK,tank.getX(), tank.getY());
            tankList.remove(tank);
            numberOfTanks--;
            return;
        }

        battlefield.addField(tank.getRepresentation(), tank.getX(), tank.getY());
    }

    //TODO Tanques teem que morrer e o server teem que saber;


    public synchronized int addTank(){

        if( numberOfTanks < MAX_NUMBER_TANKS) {

            if (tankList.size() == 0) {

                Tank tank = new Tank(1, 3, battlefield.getHeight()/2, RepresentationFactory.Orientation.EAST);
                return createTank(tank);

            } else if (tankList.size() == 1) {

                Tank tank = new Tank(2, battlefield.getWidth() - 2 - RepresentationFactory.TANK_WIDTH, battlefield.getHeight() / 2, RepresentationFactory.Orientation.WEST);
                return createTank(tank);
            } else if(tankList.size() == 2){

                Tank tank = new Tank(3, battlefield.getWidth()/2 , 2 , RepresentationFactory.Orientation.SOUTH);
                return createTank(tank);

            }else if(tankList.size() == 3){

                Tank tank = new Tank(4, battlefield.getWidth()/2, battlefield.getHeight() - 2 , RepresentationFactory.Orientation.NORTH);
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

        if(!bullets.isEmpty()) {
            for (Bullet bullet : bullets) {
                if(bullet.isAlive()){
                    moveBullet(bullet);
                }
            }
        }
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

    private synchronized boolean checkWallColistion(Bullet bullet){

        for(int i = bullet.getX(); i < bullet.getX()+bullet.getWidth(); i++) {

            for (int j = bullet.getY(); j < bullet.getY() + bullet.getHeight(); j++) {

                if (battlefield.get(i, j) == Tiletypes.WALL.getSymbol()) {
                    System.out.println("Wall Collision");
                    return true;
                }
            }
        }
        return false;
    }

    private synchronized boolean checkBulletCollision(GameObject object){

        for(int i = object.getX(); i < object.getX()+object.getWidth(); i++) {

            for(int j = object.getY(); j < object.getY()+object.getHeight(); j++) {

                System.out.println(i);
                System.out.println(j);

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

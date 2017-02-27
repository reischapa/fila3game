package net.fila3game.server.gameengine;

import net.fila3game.AudioManager;
import net.fila3game.server.Instruction;
import net.fila3game.server.gameengine.gameobjects.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Luizord on 2/21/17.
 */
public class GameEngine {

    private static final int MAX_NUMBER_TANKS = 9;
    public static final int MAX_MINE_NUMBER = 3;
    public static final int DEFAULT_BATTLEFIELD_COLUMNS = 50;
    public static final int DEFAULT_BATTLEFIELD_ROWS = 30;

    public enum Tiletypes {

        WALL('W'),
        TANK('T'),
        BULLET_R('>'),
        BULLET_L('<'),
        BULLET_U('A'),
        BULLET_D('V'),
        HEART('♥'),
        MINE('@');

        private char symbol;

        Tiletypes(char b) {
            this.symbol = b;
        }

        public char getSymbol() {
            return symbol;
        }
    }

    private Field battlefield;
    private int numberOfTanks;
    private List<Tank> tankList;
    private List<Bullet> bullets;
    private List<Mine> mineList;
    private final Field EMPTYMASK = new Field(RepresentationFactory.TANK_WIDTH, RepresentationFactory.TANK_HEIGHT);
    private final Field EMPTYBULLET = new Field(1, 1);


    public GameEngine(Field battlefield) {

        this.battlefield = battlefield;
        this.numberOfTanks = 0;
        this.tankList = new LinkedList<>();
        this.bullets = new LinkedList<>();
        this.mineList = new LinkedList<>();

    }

    public GameEngine() {

        this.battlefield = createDefaultField();
        this.numberOfTanks = 0;
        this.tankList = Collections.synchronizedList( new LinkedList<Tank>());
        this.bullets = Collections.synchronizedList( new LinkedList<Bullet>());
        this.mineList = Collections.synchronizedList( new LinkedList<Mine>());

    }

    public synchronized void receiveInstruction(Instruction i) {

        if (this.tankList.isEmpty()) {
            return;
        }

        Tank tank = null;

        for (int x = 0; x < this.tankList.size(); x++) {

            if (i.getPlayerNumber() == this.tankList.get(x).getPlayer()) {

                tank = this.tankList.get(x);

            }
        }

        if (tank == null) {
            return;
        }

        if (tank.isAlive()) {

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

                if (!bulletExists(tank.getPlayer())) {

                    Bullet bullet = createBullet(tank);

                    if (bullet.isAlive()) {

                        this.bullets.add(bullet);

                    }
                }

            } else if (i.getType().equals(Instruction.Type.M)) {

                int mineNumber = 0;

                if (!this.mineList.isEmpty()) {

                    for (Mine m : this.mineList) {

                        if (m.getPlayer() == i.getPlayerNumber()) {

                            mineNumber++;

                        }

                    }

                }

                if (mineNumber < this.MAX_MINE_NUMBER) {

                    dropMine(tank);

                }
            }
        }
    }

    private boolean bulletExists(int tankID) {

        if (!this.bullets.isEmpty()) {

            for (Bullet bullet : this.bullets) {

                if (bullet.getPlayer() == tankID) {

                    return true;

                }
            }
        }

        return false;
    }

    private Bullet createBullet(Tank tank) {

        Bullet bullet = null;

        if (tank.getOrientation().equals(RepresentationFactory.Orientation.WEST)) {

            bullet = new Bullet(tank.getPlayer(), tank.getX() - 1, tank.getY() + 1, RepresentationFactory.Orientation.WEST);

        } else if (tank.getOrientation().equals(RepresentationFactory.Orientation.EAST)) {

            bullet = new Bullet(tank.getPlayer(), tank.getX() + 3, tank.getY() + 1, RepresentationFactory.Orientation.EAST);

        } else if (tank.getOrientation().equals(RepresentationFactory.Orientation.SOUTH)) {

            bullet = new Bullet(tank.getPlayer(), tank.getX() + 1, tank.getY() + 3, RepresentationFactory.Orientation.SOUTH);

        } else {

            bullet = new Bullet(tank.getPlayer(), tank.getX() + 1, tank.getY() - 1, RepresentationFactory.Orientation.NORTH);

        }

        if (!checkWallCollision(bullet) && !checkBulletCollision(bullet)) {

            this.battlefield.addField(bullet.getRepresentation(), bullet.getX(), bullet.getY());

        } else {

            bullet.die();

        }

        return bullet;
    }

    private synchronized void moveBullet(Bullet bullet) {

        this.battlefield.addField(new Field(1, 1), bullet.getX(), bullet.getY());
        int x = 0;
        int y = 0;

        switch (bullet.getOrientation()) {
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

        if (bullet.getX() + x >= 0 && bullet.getY() + x < this.battlefield.getWidth()) {

            bullet.move(bullet.getX() + x, bullet.getY() + y);

        } else {

            Field wallField = new Field(1, 1);
            wallField.set(0, 0, 'W');
            this.battlefield.addField(wallField, bullet.getX(), bullet.getY());
            bullet.die();
            this.bullets.remove(bullet);
            return;

        }

        if (checkBulletTankCollision(bullet)) {

            this.battlefield.addField(bullet.getRepresentation(), bullet.getX(), bullet.getY());

            for (Tank t : this.tankList) {

                if (checkBulletCollision(t)) {

                    if (bullet.getPlayer() != t.getPlayer()) {

                        this.battlefield.addField(this.EMPTYMASK, t.getX(), t.getY());
                        t.die();
                        this.tankList.remove(t);
                        this.numberOfTanks--;

                    }
                }
            }

            this.battlefield.addField(this.EMPTYBULLET, bullet.getX(), bullet.getY());
            bullet.die();
            this.bullets.remove(bullet);
            return;

        }

        for (Bullet otherbullet : this.bullets) {

            if (otherbullet.getPlayer() != bullet.getPlayer()) {

                if (checkBulletCollision(bullet)) {

                    this.battlefield.addField(bullet.getRepresentation(), bullet.getX(), bullet.getY());
                    this.battlefield.addField(this.EMPTYBULLET, bullet.getX(), bullet.getY());
                    this.battlefield.addField(this.EMPTYBULLET, otherbullet.getX(), otherbullet.getY());
                    bullet.die();
                    otherbullet.die();
                    this.bullets.remove(bullet);
                    this.bullets.remove(otherbullet);
                    return;

                }
            }
        }

        if (checkWallCollision(bullet)) {

            Field wallField = new Field(1, 1);
            wallField.set(0, 0, 'W');
            this.battlefield.addField(wallField, bullet.getX(), bullet.getY());
            bullet.die();
            this.bullets.remove(bullet);
            return;

        }

        if (checkMineCollision(bullet)) {

            this.battlefield.addField(this.EMPTYBULLET, bullet.getX(), bullet.getY());
            bullet.die();
            this.bullets.remove(bullet);
            return;
        }

        this.battlefield.addField(bullet.getRepresentation(), bullet.getX(), bullet.getY());

    }

    private void moveTank(Tank tank, int x, int y) {

        this.battlefield.addField(this.EMPTYMASK, tank.getX(), tank.getY());
        tank.move(tank.getX() + x, tank.getY() + y);

        if (checkTankCollision(tank)) {

            tank.move(tank.getX() - x, tank.getY() - y);

        }

        if (checkMineCollision(tank)) {

            tank.die();
            this.tankList.remove(tank);
            this.battlefield.addField(this.EMPTYMASK, tank.getX(), tank.getY());
            this.numberOfTanks--;
            return;

        }

        this.battlefield.addField(tank.getRepresentation(), tank.getX(), tank.getY());
    }

    public synchronized int addTank() {

        if (this.numberOfTanks >= MAX_NUMBER_TANKS) {
            return -1;
        }

        Tank t = null;

        int newTankX;
        int newTankY;
        int newPlayerNumber = 1;

        for (int j = 0; j < this.tankList.size(); j++) {

            while (this.tankList.get(j).getPlayer() == newPlayerNumber) {

                newPlayerNumber++;

            }
        }

        System.out.println("player: " + newPlayerNumber);

        do {

            newTankX = (int) Math.ceil(Math.random() * this.battlefield.getWidth() - 1 )  ;
            newTankY = (int) Math.ceil(Math.random() * this.battlefield.getHeight() - 1 ) ;

            if (newTankX + RepresentationFactory.TANK_WIDTH > this.battlefield.getWidth()-1) {
                newTankX -= RepresentationFactory.TANK_WIDTH;
            }

            if (newTankY + RepresentationFactory.TANK_HEIGHT > this.battlefield.getHeight()-1) {
                newTankY -= RepresentationFactory.TANK_HEIGHT;
            }

            if (newTankX > this.battlefield.getWidth() / 4 && newTankX < (this.battlefield.getWidth() * 3) / 4) {

                if (newTankY > this.battlefield.getHeight() / 2) {

                    t = new Tank(newPlayerNumber, newTankX, newTankY, RepresentationFactory.Orientation.NORTH);

                } else {

                    t = new Tank(newPlayerNumber, newTankX, newTankY, RepresentationFactory.Orientation.SOUTH);

                }
            } else if (newTankX > (this.battlefield.getWidth() * 3) / 4) {

                t = new Tank(newPlayerNumber, newTankX, newTankY, RepresentationFactory.Orientation.WEST);

            } else {

                t = new Tank(newPlayerNumber, newTankX, newTankY, RepresentationFactory.Orientation.EAST);

            }

        } while (this.createTank(t) < 0);

        if (t == null) {
            return -1;
        }

        return t.getPlayer();
    }


    public synchronized void removeTankOfPlayerNumber(int playerNumber) {

        if (playerNumber < 1) {
            return;
        }

        if (this.tankList.size() == 0) {
            return;
        }

        for (int i = 0; i < this.tankList.size(); i++) {

            Tank t = this.tankList.get(i);

            if (t.getPlayer() == playerNumber) {

                this.tankList.remove(t);
                this.battlefield.addField(this.EMPTYMASK, t.getX(), t.getY());
                this.numberOfTanks--;

            }
        }

        System.out.println("Tank removed");
    }

    public synchronized boolean isPlayerDead(int playerNumber) {

        for (Tank t : this.tankList) {

            if (t.getPlayer() == playerNumber) {

                return false;

            }
        }

        return true;
    }

    private int createTank(Tank tank) {

        if (!checkTankCollision(tank)) {

            this.battlefield.addField(tank.getRepresentation(), tank.getX(), tank.getY());
            this.tankList.add(tank);
            this.numberOfTanks++;
            return tank.getPlayer();

        }

        return -1;
    }

    //calculate and return the gameState
    public synchronized String calculateState() {

        if (!this.bullets.isEmpty()) {

            for (Bullet bullet : this.bullets) {

                if (bullet.isAlive()) {

                    moveBullet(bullet);

                }
            }
        }

        if (!this.mineList.isEmpty()) {

            for (Mine mine : mineList) {

                if (battlefield.get(mine.getX(), mine.getY()) != Tiletypes.MINE.getSymbol()) {

                    mineList.remove(mine);

                }
            }
        }

        return this.battlefield.returnAsString();
    }

    private synchronized boolean checkTankCollision(GameObject object) {

        for (int i = object.getX(); i < object.getX() + object.getWidth(); i++) {

            for (int j = object.getY(); j < object.getY() + object.getHeight(); j++) {

                if (this.battlefield.get(i, j) == Tiletypes.WALL.getSymbol() || this.battlefield.get(i, j) == Tiletypes.TANK.getSymbol()) {

                    System.out.println("Tank Collision");
                    return true;

                }
            }
        }

        return false;
    }

    private synchronized boolean checkWallCollision(Bullet bullet) {

        for (int i = bullet.getX(); i < bullet.getX() + bullet.getWidth(); i++) {

            for (int j = bullet.getY(); j < bullet.getY() + bullet.getHeight(); j++) {

                if (this.battlefield.get(i, j) == Tiletypes.WALL.getSymbol()) {

                    System.out.println("Bullet-Wall Collision");
                    return true;

                }
            }
        }

        return false;
    }

    private synchronized boolean checkBulletTankCollision(GameObject object) {

        for (int i = object.getX(); i < object.getX() + object.getWidth(); i++) {

            for (int j = object.getY(); j < object.getY() + object.getHeight(); j++) {

                if (this.battlefield.get(i, j) == Tiletypes.TANK.getSymbol()) {

                    System.out.println("Tank-Bullet Collision");
                    return true;

                }
            }
        }

        return false;
    }


    private synchronized boolean checkBulletCollision(GameObject object) {

        for (int i = object.getX(); i < object.getX() + object.getWidth(); i++) {

            for (int j = object.getY(); j < object.getY() + object.getHeight(); j++) {

                if (this.battlefield.get(i, j) == Tiletypes.BULLET_D.getSymbol() || this.battlefield.get(i, j) == Tiletypes.BULLET_U.getSymbol() ||
                        this.battlefield.get(i, j) == Tiletypes.BULLET_L.getSymbol() || this.battlefield.get(i, j) == Tiletypes.BULLET_R.getSymbol()) {

                    System.out.println("Bullet Collision");
                    return true;

                }
            }
        }

        return false;
    }

    private Field createDefaultField() {

        Field mainField = new Field(DEFAULT_BATTLEFIELD_COLUMNS, DEFAULT_BATTLEFIELD_ROWS);

        for (int x = 0; x < mainField.getWidth(); x++) {

            for (int y = 0; y < mainField.getHeight(); y++) {

                if (x == 0 || y == 0 || x == mainField.getWidth() - 1 || y == mainField.getHeight() - 1) {

                    mainField.set(x, y, Tiletypes.WALL.getSymbol());
                }
            }
        }

        return mainField;

    }

    public void drawHeart() {

        int randomX = (int) Math.round(Math.random() * this.battlefield.getWidth() - 4) + 4;
        int randomY = (int) Math.round(Math.random() * this.battlefield.getHeight() - 4) + 4;

        Heart heart = new Heart(randomX, randomY);

        if (!checkTankCollision(heart) && !checkBulletCollision(heart) && !checkHeartCollision(heart)) {

            this.battlefield.addField(heart.getRepresentation(), heart.getX(), heart.getY());

        }
    }

    private synchronized boolean checkHeartCollision(GameObject object) {

        for (int i = object.getX(); i < object.getX() + object.getWidth(); i++) {

            for (int j = object.getY(); j < object.getY() + object.getHeight(); j++) {

                if (this.battlefield.get(i, j) == Tiletypes.HEART.getSymbol()) {

                    System.out.println("Heart Collision");
                    return true;

                }
            }
        }

        return false;

    }

    private synchronized void dropMine(Tank tank) {

        Mine mine = null;

        if (tank.getOrientation().equals(RepresentationFactory.Orientation.EAST)) {

            mine = new Mine(tank.getX() - 1, tank.getY() + 1, tank.getPlayer());

        } else if (tank.getOrientation().equals(RepresentationFactory.Orientation.WEST)) {

            mine = new Mine(tank.getX() + 3, tank.getY() + 1, tank.getPlayer());

        } else if (tank.getOrientation().equals(RepresentationFactory.Orientation.NORTH)) {

            mine = new Mine(tank.getX() + 1, tank.getY() + 3, tank.getPlayer());

        } else {

            mine = new Mine(tank.getX() + 1, tank.getY() - 1, tank.getPlayer());

        }

        if (!checkTankCollision(mine)) {

            this.battlefield.addField(mine.getRepresentation(), mine.getX(), mine.getY());

        } else {

            mine.die();
            return;

        }
        mineList.add(mine);
    }

    private synchronized boolean checkMineCollision(GameObject object) {

        for (int i = object.getX(); i < object.getX() + object.getWidth(); i++) {

            for (int j = object.getY(); j < object.getY() + object.getHeight(); j++) {

                if (this.battlefield.get(i, j) == Tiletypes.MINE.getSymbol()) {

                    this.battlefield.addField(EMPTYBULLET, i, j);
                    System.out.println("Mine Collision");
                    return true;

                }
            }
        }

        return false;

    }

}

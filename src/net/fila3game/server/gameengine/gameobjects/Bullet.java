package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/21/17.
 */
public class Bullet implements GameObject {

    public static final int BULLET_HEIGHT = 1;
    public static final int BULLET_WIDTH = 1;

    private int player;
    private int x;
    private int y;
    private RepresentationFactory.Orientation orientation;

    public Bullet(int player, int x, int y) {
        this.player = player;
        this.x = x;
        this.y = y;
    }

    @Override
    public int getPlayer() {
        return player;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public Field getRepresentation() {
        return RepresentationFactory.returnRepresentation(this.orientation, RepresentationFactory.GameObjectType.BULLET);
    }

    @Override
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setOrientation(RepresentationFactory.Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public int getHeight() {
        return BULLET_HEIGHT;
    }

    @Override
    public int getWidth() {
        return BULLET_WIDTH;
    }
}

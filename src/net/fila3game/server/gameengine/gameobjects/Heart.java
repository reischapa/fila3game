package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/25/17.
 */
public class Heart implements GameObject {

    public static final int HEART_WIDTH = 2;
    public static final int HEART_HEIGHT = 2;

    private int player;
    private int x;
    private int y;
    private RepresentationFactory.Orientation defaultOrientation;
    private boolean alive;

    public Heart(int x, int y) {
        this.x = x;
        this.y = y;
        player = 0;
        defaultOrientation = RepresentationFactory.Orientation.NORTH;
        alive = true;
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
        return RepresentationFactory.returnRepresentation(defaultOrientation, RepresentationFactory.GameObjectType.HEART, String.valueOf(player));
    }

    @Override
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setOrientation(RepresentationFactory.Orientation orientation) {
        defaultOrientation = orientation;
    }

    @Override
    public RepresentationFactory.Orientation getOrientation() {
        return defaultOrientation;
    }

    @Override
    public int getHeight() {
        return HEART_HEIGHT;
    }

    @Override
    public int getWidth() {
        return HEART_WIDTH;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void die() {
        if (alive) {
            alive = false;
        }
    }
}

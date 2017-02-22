package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/21/17.
 */
public class Tank implements GameObject {

    private int player;

    public Tank(int x, int y) {

    }

    @Override
    public int player() {
        return 0;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public Field getRepresentation() {
        return null;
    }

    @Override
    public void move(int x, int y) {

    }

    @Override
    public void setOrientation(RepresentationFactory.Orientation orientation) {

    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    public void setPlayer(int player) {
        this.player = player;
    }
}

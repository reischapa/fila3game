package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/21/17.
 */
public class Bullet implements GameObject{
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
}

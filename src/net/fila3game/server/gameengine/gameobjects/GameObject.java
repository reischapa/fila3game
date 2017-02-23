package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/21/17.
 */
public interface GameObject {

    int getPlayer();

    int getX();

    int getY();

    Field getRepresentation();

    void move(int x, int y);

    void setOrientation(RepresentationFactory.Orientation orientation);

    int getHeight();

    int getWidth();

}

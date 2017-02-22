package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/21/17.
 */
public class RepresentationFactory {

    public enum Orientation {
        NORTH,
    }

    public enum GameObjectType {
        TANK, BULLET,
    }


    public static Field returnRepresentation(RepresentationFactory.Orientation orientation, RepresentationFactory.GameObjectType objectType) {
       //switch logic
        throw new UnsupportedOperationException();
    }

}

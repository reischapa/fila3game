package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/21/17.
 */
public class RepresentationFactory {

    public static final int TANK_WIDTH = 3;
    public static final int TANK_HEIGHT = 3;

    public enum Orientation {

        NORTH, SOUTH, EAST, WEST
    }

    public enum GameObjectType {

        TANK, BULLET
    }


    public static Field returnRepresentation(Orientation orientation, GameObjectType objectType, String playerID) {

        Field fieldRepresentation = null;

        switch (objectType) {
            case TANK:

                switch (orientation) {
                    case NORTH:
                        fieldRepresentation = new Field(Tank.TANK_WIDTH, Tank.TANK_HEIGHT);
                        fieldRepresentation.constructFromString("0T0\nT"+playerID+"T\nTTT");
                        break;

                    case SOUTH:
                        fieldRepresentation = new Field(Tank.TANK_WIDTH, Tank.TANK_HEIGHT);
                        fieldRepresentation.constructFromString("TTT\nT"+playerID+"T\n0T0");
                        break;

                    case EAST:
                        fieldRepresentation = new Field(Tank.TANK_WIDTH, Tank.TANK_HEIGHT);
                        fieldRepresentation.constructFromString("TT0\nT"+playerID+"T\nTT0");
                        break;

                    case WEST:
                        fieldRepresentation = new Field(Tank.TANK_WIDTH, Tank.TANK_HEIGHT);
                        fieldRepresentation.constructFromString("0TT\nT"+playerID+"T\n0TT");
                        break;

                    default:
                        System.out.println("Something went terribly wrong");
                }
                break;

            case BULLET:

                switch (orientation) {
                    case NORTH:
                        fieldRepresentation = new Field(Bullet.BULLET_WIDTH, Bullet.BULLET_HEIGHT);
                        fieldRepresentation.constructFromString("A");
                        break;

                    case SOUTH:
                        fieldRepresentation = new Field(Bullet.BULLET_WIDTH, Bullet.BULLET_HEIGHT);
                        fieldRepresentation.constructFromString("V");
                        break;

                    case EAST:
                        fieldRepresentation = new Field(Bullet.BULLET_WIDTH, Bullet.BULLET_HEIGHT);
                        fieldRepresentation.constructFromString(">");
                        break;

                    case WEST:
                        fieldRepresentation = new Field(Bullet.BULLET_WIDTH, Bullet.BULLET_HEIGHT);
                        fieldRepresentation.constructFromString("<");
                        break;

                    default:
                        System.out.println("Something went terribly wrong");
                }
                break;
        }
        return fieldRepresentation;
    }

}

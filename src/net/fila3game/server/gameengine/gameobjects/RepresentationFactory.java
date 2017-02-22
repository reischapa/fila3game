package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/21/17.
 */
public class RepresentationFactory {

    public enum Orientation {

        NORTH, SOUTH, EAST, WEST
    }

    public enum GameObjectType {

        TANK, BULLET
    }


    public static Field returnRepresentation(Orientation orientation, GameObjectType objectType) {

        Field fieldRepresentation = null;

        switch (objectType) {
            case TANK:

                switch (orientation) {
                    case NORTH:
                        fieldRepresentation = new Field(3, 3);
                        fieldRepresentation.constructFromString("0T0\nTTT\nTTT");
                        break;

                    case SOUTH:
                        fieldRepresentation = new Field(3, 3);
                        fieldRepresentation.constructFromString("TTT\nTTT\n0T0");
                        break;

                    case EAST:
                        fieldRepresentation = new Field(3, 3);
                        fieldRepresentation.constructFromString("TT0\nTTT\nTT0");
                        break;

                    case WEST:
                        fieldRepresentation = new Field(3, 3);
                        fieldRepresentation.constructFromString("0TT\nTTT\n0TT");
                        break;

                    default:
                        System.out.println("Something went terribly wrong");
                }
                break;

            case BULLET:

                switch (orientation) {
                    case NORTH:
                        fieldRepresentation = new Field(1, 1);
                        fieldRepresentation.constructFromString("A");
                        break;

                    case SOUTH:
                        fieldRepresentation = new Field(1, 1);
                        fieldRepresentation.constructFromString("V");
                        break;

                    case EAST:
                        fieldRepresentation = new Field(1, 1);
                        fieldRepresentation.constructFromString(">");
                        break;

                    case WEST:
                        fieldRepresentation = new Field(1, 1);
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

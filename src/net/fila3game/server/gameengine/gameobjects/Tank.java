package net.fila3game.server.gameengine.gameobjects;

import net.fila3game.server.gameengine.Field;
import net.fila3game.server.gameengine.GameEngine;

/**
 * Created by codecadet on 2/21/17.
 */
public class Tank implements GameObject {

    public static final int TANK_HEIGHT = 3;
    public static final int TANK_WIDTH = 3;

    private Field tankField;
    private int player;
    private int x;
    private int y;
    private RepresentationFactory.Orientation orientation;

    public Tank(int player, int x, int y) {
        this.player = player;
        this.x = x;
        this.y = y;

        tankField = new Field(TANK_WIDTH, TANK_HEIGHT);
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
        return RepresentationFactory.returnRepresentation(this.orientation, RepresentationFactory.GameObjectType.TANK);
    }

    @Override
    public void move(int x, int y) {
        tankField.set(x,y, GameEngine.Tiletypes.TANK.getSymbol());
    }

    @Override
    public void setOrientation(RepresentationFactory.Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public int getHeight() {
        return TANK_HEIGHT;
    }

    @Override
    public int getWidth() {
        return TANK_WIDTH;
    }

    public void setPlayer(int player) {
        this.player = player;
    }
}

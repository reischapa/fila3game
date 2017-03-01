package net.fila3game.commons;

/**
 * Created by codecadet on 3/1/17.
 */
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

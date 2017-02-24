package net.fila3game.server.gameengine;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by codecadet on 2/1/17.
 */
public class Field {

    private int width;
    private int height;

    public static void main(String[] args) {
        Field f = new Field(3, 3);
        f.constructFromString("TTT\nTTT\n0T0\n");


        for (int y = 0; y < f.yMax ; y++) {
            for (int x = 0; x < f.xMax; x++) {
                System.out.println(f.get(x,y));
            }

        }
    }

    public static final char FIELD_LINE_SEPARATOR = '\n';
    public static final char EMPTY_CELL_CHARACTER = '0';

    private ConcurrentMap<Coord, Character> statusMap;
    private final int xMin, xMax, yMin, yMax;

    public Field(int width, int height) {
        this.xMin = 0;
        this.xMax = xMin + width;
        this.yMin = 0;
        this.yMax = yMin + height;
        this.statusMap = new ConcurrentHashMap<>();
        this.init();
    }

    public char get(int x, int y) {

        if (!this.isPointValid(x, y)) {
            throw new IndexOutOfBoundsException();
        }

        return this.statusMap.get(new Coord(x, y));
    }

    public void set(int x, int y, Character c) {

        if (!this.isPointValid(x, y)) {
            throw new IndexOutOfBoundsException();
        }

        this.statusMap.put(new Coord(x, y), c);
    }

    public boolean isPointValid(int x, int y) {
        return (x >= xMin && y >= yMin && x < xMax && y < yMax);
    }

    public void init() {
        for (int j = this.yMin; j < this.yMax; j++ ) {
            for (int i = this.xMin; i < this.xMax; i++) {
                Coord c = new Coord(i, j);
                this.statusMap.remove(c);
                this.statusMap.put(c, EMPTY_CELL_CHARACTER);
            }
        }
    }

    public String returnAsString() {
        String s = "";
        for (int i = yMin; i < yMax; i++ ) {
            for (int j = xMin; j < xMax; j++) {
                s += this.statusMap.get(new Coord(j, i));
            }
            s += FIELD_LINE_SEPARATOR;
        }
        return s;
    }

    public void constructFromString(String input) {

        if (this.statusMap == null) {
            this.statusMap = new ConcurrentHashMap<>();
        }

        String[] rows = input.split("" + FIELD_LINE_SEPARATOR );



        for (int y = this.yMin; y < this.yMax; y++ ) {
            char[] chars = rows[y].toCharArray();
            for (int x = this.xMin; x < this.xMax; x++) {
                this.statusMap.put(new Coord(x, y), chars[x]);
            }
        }
    }

    public void addField(Field field, int posX, int posY) {
        for (int y = field.yMin; y < field.yMax ; y++) {
            for (int x = field.xMin; x < field.xMax ; x++) {
                this.set(posX + x, posY + y, field.get(x, y));
            }
        }
    }

    public int getWidth() {
        return xMax;
    }

    public int getHeight() {
        return yMax;
    }


    private static class Coord {

        private int x;
        private int y;

        private Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Coord coord = (Coord) o;

            if (x != coord.x) return false;
            return y == coord.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        @Override
        public String toString() {
            return "(" + this.x + "," + this.y + ")";
        }
    }

}

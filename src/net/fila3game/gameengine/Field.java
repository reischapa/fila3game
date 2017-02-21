package net.fila3game.gameengine;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by codecadet on 2/1/17.
 */
public class Field {
    public static final char FIELD_LINE_SEPARATOR = '\n';
    public static final char EMPTY_CELL_CHARACTER = '0';

    private ConcurrentMap<Coord, Character> statusMap;
    private final int xMin, xMax, yMin, yMax;

    public Field(int xMin, int yMin, int width, int height) {
        this.xMin = xMin;
        this.xMax = xMin + width;
        this.yMin = yMin;
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

        String[] rows = input.split("" + FIELD_LINE_SEPARATOR + "\\+");

        List<String> wordList = Arrays.asList(rows);

        Iterator<String> iter = wordList.iterator();

        for (int j = this.yMin; j < this.yMax; j++ ) {
            String word = "";
            if (iter.hasNext()) {
                word = iter.next();
            } else {
                word = this.createPaddingString(xMax);
            }
            for (int i = this.xMin; i < this.xMax; i++) {
                char c;
                if (i >= word.length()) {
                    c = EMPTY_CELL_CHARACTER;
                } else {
                    c = word.charAt(i);
                }
                this.statusMap.put(new Coord(i, j), c);
            }
        }

    }

    private String createPaddingString(int length) {
        char[] c = new char[length];

        for (int i = 0; i < c.length; i++) {
            c[i] = EMPTY_CELL_CHARACTER;
        }
        return String.valueOf(c);
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

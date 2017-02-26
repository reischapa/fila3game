package net.fila3game.client;

/**
 * Created by codecadet on 2/25/17.
 */
public class GUIEvent {

    private Type type;
    private Key key;

    public static GUIEvent keyboardInput(GUIEvent.Key key) {
        GUIEvent e = new GUIEvent(Type.CLIENT_KEYBOARD_INPUT);
        e.setKey(key);
        return e;
    }

    public static GUIEvent connect() {
        GUIEvent e = new GUIEvent(Type.CLIENT_CONNECT_SERVER);
        return e;
    }

    public static GUIEvent disconnect() {
        GUIEvent e = new GUIEvent(Type.CLIENT_DISCONNECT_SERVER);
        return e;
    }

    public enum Key {
        KEY_SPACE, KEY_ARROWUP, KEY_ARROWDOWN, KEY_ARROWLEFT, KEY_ARROWRIGHT, KEY_Q, KEY_R, KEY_M;
    }

    public enum Type {
        CLIENT_KEYBOARD_INPUT, CLIENT_CONNECT_SERVER, CLIENT_DISCONNECT_SERVER,
    }

    public GUIEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}

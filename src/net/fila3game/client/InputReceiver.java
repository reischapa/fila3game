package net.fila3game.client;

/**
 * Created by codecadet on 2/21/17.
 */
public interface InputReceiver {

    public enum Key {
        KEY_SPACE, KEY_A,
    }

    void receiveInput(InputReceiver.Key key);

}

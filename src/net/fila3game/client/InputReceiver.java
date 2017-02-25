package net.fila3game.client;

/**
 * Created by codecadet on 2/21/17.
 */
public interface InputReceiver {

    enum Key {
        KEY_SPACE, KEY_ARROWUP, KEY_ARROWDOWN, KEY_ARROWLEFT, KEY_ARROWRIGHT, KEY_Q;
    }

    void receiveInput(InputReceiver.Key key);

}

package net.fila3game.client;

/**
 * Created by codecadet on 2/21/17.
 */
public class LanternaDisplayController implements Display, Controller {


    InputReceiver receiver;

    @Override
    public void receiveData(String data) {

    }

    @Override
    public void setInputReceiver(InputReceiver receiver) {

    }

    public void setReceiver(InputReceiver receiver) {
        this.receiver = receiver;
    }
}

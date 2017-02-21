package net.fila3game.client;

import net.fila3game.server.gameengine.Field;

/**
 * Created by codecadet on 2/21/17.
 */
public class LanternaDisplayController implements Display, Controller {

    public static void main(String[] args) {
        LanternaDisplayController d = new LanternaDisplayController();
        Field f = new Field(10, 10);
        d.receiveData(new GameState(f.returnAsString()));

    }


    InputReceiver receiver;

    @Override
    public void receiveData(GameState state) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setInputReceiver(InputReceiver receiver) {

    }

    public void setReceiver(InputReceiver receiver) {
        this.receiver = receiver;
    }
}

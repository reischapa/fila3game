package net.fila3game.client;

import net.fila3game.server.gameengine.Field;
import net.fila3game.server.GameServer;
//import net.jchapa.chapautils.RandomGen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chapa on 2/19/2017.
 */
public class GameClient implements InputReceiver {

    public static void main(String[] args) {
        GameClient gc = new GameClient();
        gc.connect();
    }

    private final Field field;
    private DatagramSocket datagramSocket;
    private ExecutorService executorService;


    private Display display;

    public GameClient() {
        this.field = new Field(0, 0, GameServer.FIELD_WIDTH, GameServer.FIELD_HEIGHT);
        this.executorService = Executors.newFixedThreadPool(10);

    }

    public void connect() {

        try {
            Socket socket = new Socket("localhost", GameServer.TCP_CONNECTION_PORT);
            ServerConnectionWorker worker = new ServerConnectionWorker(socket);
            worker.run();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void receiveInput(Key key) {

    }


    private class ServerConnectionWorker implements Runnable {
        private Socket socket;
        private DatagramSocket datagramSocket;
        private BufferedReader reader;
        private ScheduledThreadPoolExecutor executorService;

        public ServerConnectionWorker(Socket socket) throws IOException {
            this.socket = socket;
            this.datagramSocket = new DatagramSocket(GameServer.OUTGOING_UDP_CONNECTION_PORT);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.executorService = new ScheduledThreadPoolExecutor(4);
        }

        @Override
        public void run() {

            Runnable tcpRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ServerConnectionWorker.this.reader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            Runnable dataReceiver = new Runnable() {
                @Override
                public void run() {

                    try {
                        byte[] buffer = new byte[20000];
                        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

                        ServerConnectionWorker.this.datagramSocket.receive(packet);
                        String string = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                        synchronized (GameClient.this.field) {
                            GameClient.this.field.constructFromString(string);
                        }

                        System.out.println(string);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };



            Runnable dataSender = new Runnable() {
                @Override
                public void run() {
                    String sent = null;
//                    int x = RandomGen.getBoundedRandomInt(0, GameServer.FIELD_WIDTH - 1);
//                    int y = RandomGen.getBoundedRandomInt(0, GameServer.FIELD_HEIGHT - 1);

//                    sent = x + " " + y + " G";

                    try {
                        byte[] bytes = sent.getBytes("UTF-8");
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("localhost"), GameServer.INCOMING_UDP_CONNECTION_PORT);

                        ServerConnectionWorker.this.datagramSocket.send(packet);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                }
            };

            this.executorService.scheduleAtFixedRate(tcpRunnable,0,GameServer.KEEPALIVE_INTERVAL, TimeUnit.MILLISECONDS);
            this.executorService.scheduleAtFixedRate(dataReceiver,0,GameServer.SEND_INTERVAL/4, TimeUnit.MILLISECONDS);
            this.executorService.scheduleAtFixedRate(dataSender,0,GameServer.SEND_INTERVAL, TimeUnit.MILLISECONDS);


        }
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

}

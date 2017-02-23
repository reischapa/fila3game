package net.fila3game.client;

//import net.jchapa.chapautils.RandomGen;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chapa on 2/19/2017.
 */
public class GameClient implements InputReceiver {

    public static final int SERVER_TCP_CONNECTION_PORT = 8080;
    public static final int RECEIVING_UDP_CONNECTION_PORT = 55356;
    public static final int SENDING_UDP_CONNECTION_PORT = 55355;
    public static final String STRING_ENCODING = "UTF-8";
    public static final int CLIENT_LISTEN_INTERVAL_MILLIS = 10;
    public static final int NUMBER_MULTIPLES_SENT = 5;

    public static void main(String[] args) {
        GameClient gc = new GameClient();
        gc.connect("localhost");
    }

    private Display display;


    private InetAddress serverAddress;
    private Socket socket;

    private BufferedReader reader;
    private BufferedWriter writer;

    private DatagramSocket incomingDatagramSocket;
    private DatagramSocket outgoingDatagramSocket;

    private ScheduledThreadPoolExecutor scheduledExecutorService;
    private ExecutorService normalExecutorService;

    private int playerID = 0;

    public GameClient() {
    }

    public void connect(String address) {


        try {
            this.serverAddress = InetAddress.getByName(address);

            this.socket = new Socket(this.serverAddress, SERVER_TCP_CONNECTION_PORT);

            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //TODO client side handshake with server?
            System.out.println(this.reader.readLine());

            System.out.println("dlksjfdlskdjf");

            this.scheduledExecutorService = new ScheduledThreadPoolExecutor(4);
            this.normalExecutorService = Executors.newFixedThreadPool(4);


            this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
            this.outgoingDatagramSocket = new DatagramSocket();

            this.scheduledExecutorService.scheduleAtFixedRate(new ServerReceiverWorker(this.incomingDatagramSocket, this.display),0, CLIENT_LISTEN_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);


        } catch (UnknownHostException e) {
            //TODO no host
            e.printStackTrace();
        } catch (IOException e) {
            //TODO failed connection error handling
            e.printStackTrace();
        }

    }

    @Override
    public void receiveInput(InputReceiver.Key key) {
        this.normalExecutorService.execute(new ServerSenderWorker(this.outgoingDatagramSocket,this.serverAddress,this.multiplyCommands("0 S")));
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    private String[] multiplyCommands(String in) {
        Long time = System.currentTimeMillis();
        in = in.concat(" " + Long.toString(time));
        String[] result = new String[NUMBER_MULTIPLES_SENT];
        for (int i = 0; i < NUMBER_MULTIPLES_SENT; i++) {
            result[i] = in;
        }
        System.out.println(result[0]);
        return result;
    }

    private class ServerReceiverWorker implements Runnable {

        private DatagramSocket datagramSocket;
        private Display display;

        public ServerReceiverWorker(DatagramSocket datagramSocket, Display display) {
            this.datagramSocket = datagramSocket;
            this.display = display;
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[20000];
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

                this.datagramSocket.receive(packet);
                String string = new String(packet.getData(), 0, packet.getLength(), STRING_ENCODING);

                System.out.println(string);

                this.display.receiveData(new GameState(string));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ServerSenderWorker implements Runnable {

        private DatagramSocket datagramSocket;
        private InetAddress inetAddress;
        private String[] data;

        public ServerSenderWorker(DatagramSocket datagramSocket, InetAddress inetAddress, String[] data)  {
            this.inetAddress = inetAddress;
            this.data = data;
            this.datagramSocket = datagramSocket;

        }

        @Override
        public void run() {

            try {

                for (String s : this.data) {
                    byte[] bytes = s.getBytes("UTF-8");
                    System.out.println(s);
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.inetAddress, GameClient.SENDING_UDP_CONNECTION_PORT);

                    this.datagramSocket.send(packet);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}

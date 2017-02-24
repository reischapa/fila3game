package net.fila3game.client;


import net.fila3game.server.Instruction;

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
        LanternaDisplayController ln = new LanternaDisplayController();
        ln.setInputReceiver(gc);
        gc.setDisplay(ln);
        ln.init();
        gc.connect("192.168.0.132");
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

    private int playerNumber = 0;

    private boolean isConnected = false;

    public GameClient() {
    }

    public void connect(String address) {


        try {
            this.serverAddress = InetAddress.getByName(address);

            this.socket = new Socket(this.serverAddress, SERVER_TCP_CONNECTION_PORT);

            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //TODO client side handshake with server?

            System.out.print(this.tcpReceive());

            System.out.println("Connected to server");

            this.scheduledExecutorService = new ScheduledThreadPoolExecutor(4);
            this.normalExecutorService = Executors.newFixedThreadPool(4);

            this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
            this.outgoingDatagramSocket = new DatagramSocket();

            this.scheduledExecutorService.scheduleAtFixedRate(new ServerReceiverWorker(),0, CLIENT_LISTEN_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);

            this.isConnected = true;


        } catch (UnknownHostException e) {
            //TODO no host
            e.printStackTrace();
        } catch (IOException e) {
            //TODO failed connection error handling
            e.printStackTrace();
        }

    }

    private void handshake() {

    }

    private void tcpSend(String message) throws IOException {
        this.writer.write(message + "\n");
        this.writer.flush();
    }

    private String tcpReceive() throws  IOException {
        return this.reader.readLine();
    }

    @Override
    public void receiveInput(InputReceiver.Key key) {
        if (!this.isConnected) {
            return;
        }

        String instruction = this.appendMovementInstruction(this.playerNumber + "", key);

        this.normalExecutorService.execute(new ServerSenderWorker(this.multiplyCommands(instruction)));

    }

    public String appendMovementInstruction(String base, InputReceiver.Key key) {
        switch (key) {
            case KEY_ARROWDOWN:
                return base + " " + Instruction.Type.D.toString();
            case KEY_ARROWRIGHT:
                return base + " " + Instruction.Type.R.toString();
            case KEY_ARROWUP:
                return base + " " + Instruction.Type.U.toString();
            case KEY_ARROWLEFT:
                return base + " " + Instruction.Type.L.toString();
            default:
                throw new UnsupportedOperationException();
        }
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

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[20000];
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
//                System.out.println("Receiving message:");
                GameClient.this.incomingDatagramSocket.receive(packet);
                String string = new String(packet.getData(), 0, packet.getLength(), STRING_ENCODING);

//                System.out.println(string);

                GameState state = new GameState(string);

//                System.out.println("Transmitting state to display");

                GameClient.this.display.receiveData(state);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ServerSenderWorker implements Runnable {

        private String[] data;

        public ServerSenderWorker( String[] data)  {
            this.data = data;

        }

        @Override
        public void run() {

            try {

                for (String s : this.data) {
                    byte[] bytes = s.getBytes("UTF-8");
                    System.out.println(s);
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, GameClient.this.serverAddress, GameClient.SENDING_UDP_CONNECTION_PORT);

                    GameClient.this.outgoingDatagramSocket.send(packet);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}

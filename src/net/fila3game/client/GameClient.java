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
public class GameClient implements GUIEventReceiver {

    private enum State {
        WAITING, CONNECTED,
    }


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

    private State state = State.WAITING;

    public GameClient() {
    }

    public void connect(String address) {

        if (this.state == State.CONNECTED) {
            return;
        }


        try {
            this.serverAddress = InetAddress.getByName(address);

            this.socket = new Socket(this.serverAddress, SERVER_TCP_CONNECTION_PORT);

            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //TODO client side handshake with server?

            this.playerNumber = Integer.parseInt(this.tcpReceive());

            System.out.println("Connected to server");

            this.scheduledExecutorService = new ScheduledThreadPoolExecutor(4);
            this.normalExecutorService = Executors.newFixedThreadPool(4);

            this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
            this.outgoingDatagramSocket = new DatagramSocket();

            this.scheduledExecutorService.scheduleAtFixedRate(new ServerReceiverWorker(),0, CLIENT_LISTEN_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);


        } catch (UnknownHostException e) {
            //TODO no host
            e.printStackTrace();
        } catch (IOException e) {
            //TODO failed connection error handling
            e.printStackTrace();
        }

    }

    private void disconnect() {

        try {
            this.reader.close();
            this.writer.close();
            this.socket.close();

            this.normalExecutorService.shutdownNow();
            this.scheduledExecutorService.shutdownNow();
            this.incomingDatagramSocket.close();
            this.outgoingDatagramSocket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Disconnected");

    }

    private void receiveInitialConfiguration() throws IOException                                                                                                                                                                                       {

    }

    private void tcpSend(String message) throws IOException {
        this.writer.write(message + "\n");
        this.writer.flush();
    }

    private String tcpReceive() throws  IOException {
        return this.reader.readLine();
    }

    @Override
    public void receiveGUIEvent(GUIEvent event) {

//        if (event == null) {
//            return;
//        }

        System.out.println(event.getType());

        switch (event.getType()) {

            case CLIENT_CONNECT_SERVER:
                this.connect("localhost");
                this.state = State.CONNECTED;
                break;
            case CLIENT_DISCONNECT_SERVER:
                this.disconnect();
                this.state = State.WAITING;
                break;
            case CLIENT_KEYBOARD_INPUT:
                ServerSenderWorker s = new ServerSenderWorker(this.multiplyCommands(this.constructInstructionString(event.getKey())));
                this.normalExecutorService.execute(s);
                break;
        }



    }


    private String constructInstructionString(GUIEvent.Key key) {
        String result = this.playerNumber + "";

        result = this.appendMovementInstruction(result, key);
        result = this.appendShootInstruction(result, key);
        return result;
    }


    public String appendMovementInstruction(String base, GUIEvent.Key key) {


        switch (key) {
            case KEY_ARROWDOWN:
                base = base + " " + Instruction.Type.D.toString();
                break;
            case KEY_ARROWRIGHT:
                base = base + " " + Instruction.Type.R.toString();
                break;
            case KEY_ARROWUP:
                base = base + " " + Instruction.Type.U.toString();
                break;
            case KEY_ARROWLEFT:
                base = base + " " + Instruction.Type.L.toString();
                break;
            default:
        }

        return base;
    }

    public String appendShootInstruction(String base, GUIEvent.Key key) {
        switch (key) {
            case KEY_SPACE:
                return base + " " + Instruction.Type.S;
            default:
                return base;

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

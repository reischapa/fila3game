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

    public static final int HEARTBEAT_INTERVAL = 1000;

    public static void main(String[] args) {
        GameClient gc = new GameClient();
        LanternaDisplayController ln = new LanternaDisplayController();
        ln.setInputReceiver(gc);
        gc.setDisplay(ln);
        ln.init();
    }

    private Display display;


    private InetAddress serverAddress;
    private Socket socket;

    private BufferedReader reader;
    private BufferedWriter writer;

    private DatagramSocket incomingDatagramSocket;
    private DatagramSocket outgoingDatagramSocket;

    private ExecutorService workerExecutorService;
    private ScheduledThreadPoolExecutor heartbeatExecutor;

    private String playerIdentifier;

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

            this.initializeTCPStreams();

            //TODO client side handshake with server?

            this.receiveInitialConfiguration();

            System.out.println("Connected to server");

            this.initializeUDPSockets();

            this.scheduleWorkers();
            this.startHeartbeatSender();

        } catch (UnknownHostException e) {
            System.out.println("HOST NOT AVAILABLE");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initializeTCPStreams() throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private void initializeUDPSockets() throws IOException {
        this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
        this.outgoingDatagramSocket = new DatagramSocket();
    }

    private void scheduleWorkers() {
        if (this.workerExecutorService == null) {
            this.workerExecutorService = Executors.newFixedThreadPool(4);
        }

        this.workerExecutorService.execute(new ServerReceiverWorker());
    }


    private void startHeartbeatSender() {
        if (this.heartbeatExecutor == null) {
            this.heartbeatExecutor = new ScheduledThreadPoolExecutor(4);
        }

        this.heartbeatExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    GameClient.this.tcpSend(GameClient.this.getHeartbeatMessage());
                    GameClient.this.tcpReceive();
                } catch (IOException e) {
                    e.printStackTrace();
                    GameClient.this.disconnect();
                }
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);

    }

    private String getHeartbeatMessage() {
        return this.playerIdentifier;
    }

    private void stopHeartbeatSender() {
        if (this.heartbeatExecutor == null) {
            return;
        }

        this.heartbeatExecutor.shutdownNow();

    }

    private void disconnect() {

        try {
            this.stopHeartbeatSender();
            this.workerExecutorService.shutdownNow();
            this.reader.close();
            this.writer.close();
            this.socket.close();

            this.incomingDatagramSocket.close();
            this.outgoingDatagramSocket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Disconnected");

    }

    private void receiveInitialConfiguration() throws IOException                                                                                                                                                                                       {
        this.playerIdentifier = this.tcpReceive();
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
                this.heartbeatExecutor.execute(s);
                break;
        }



    }


    private String constructInstructionString(GUIEvent.Key key) {
        String result = this.playerIdentifier + "";

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
            if (GameClient.this.incomingDatagramSocket.isClosed()) {
                return;
            }

            try {
                byte[] buffer = new byte[20000];
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);


                while (true) {
//                System.out.println("Receiving message:");
                    GameClient.this.incomingDatagramSocket.receive(packet);
                    String string = new String(packet.getData(), 0, packet.getLength(), STRING_ENCODING);

                    GameState state = new GameState(string);

//                System.out.println("Transmitting state to display");

                    GameClient.this.display.receiveData(state);
                }

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

            if (GameClient.this.outgoingDatagramSocket.isClosed()) {
                return;
            }

            try {

                for (String s : this.data) {
                    byte[] bytes = s.getBytes(STRING_ENCODING);
                    System.out.println(s);
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, GameClient.this.serverAddress, GameClient.SENDING_UDP_CONNECTION_PORT);

                    GameClient.this.outgoingDatagramSocket.send(packet);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void setDisplay(Display display) {
        this.display = display;
    }


}

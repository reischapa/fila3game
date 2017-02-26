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

    private enum ConnectionState {
        DISCONNECTED, CONNECTED,
    }


    public static final int SERVER_TCP_CONNECTION_PORT = 8080;
    public static final int RECEIVING_UDP_CONNECTION_PORT = 55356;
    public static final int SENDING_UDP_CONNECTION_PORT = 55355;
    public static final String STRING_ENCODING = "UTF-8";
    public static final int CLIENT_LISTEN_INTERVAL_MILLIS = 10;
    public static final int NUMBER_MULTIPLES_SENT = 5;

    public static final int HEARTBEAT_INTERVAL = 1000;
    public static final String SERVER_IP_ADDRESS = "localhost";
    public static final int CONNECTION_TIMEOUT_MILLIS = 2000;

    public static void main(String[] args) {
        GameClient gc = new GameClient();
        LanternaDisplayController ln = new LanternaDisplayController();
        ln.setInputReceiver(gc);
        gc.setDisplay(ln);
        ln.init();
//        gc.connect("192.168.0.132");

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
    private int playerNumber;

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public GameClient() {
    }

    public void connect(String address) {

        if (this.connectionState == ConnectionState.CONNECTED) {
            return;
        }

        try {
            this.serverAddress = InetAddress.getByName(address);

            this.socket = new Socket();

            this.socket.connect( new InetSocketAddress(this.serverAddress,SERVER_TCP_CONNECTION_PORT), CONNECTION_TIMEOUT_MILLIS);

            this.initializeTCPStreams();

            this.receiveInitialConfiguration();

            System.out.println("Connected to server");

            this.initializeUDPSockets();

            this.scheduleWorkers();

            this.startHeartbeatSender();

        } catch (UnknownHostException e) {
            System.out.println("HOST NOT AVAILABLE");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            System.out.println("ESTE ROUTER Ë UMA MERDA");
            this.disconnect();
            this.display.receiveData(GameState.serverNotAvailable());
            e.printStackTrace();
            return;
        }

        this.connectionState = ConnectionState.CONNECTED;


    }

    private void initializeTCPStreams() throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private void receiveInitialConfiguration() throws IOException                                                                                                                                                                                       {
        this.playerIdentifier = this.tcpReceive();
        this.playerNumber = Integer.parseInt(this.tcpReceive());
    }

    private void initializeUDPSockets() throws IOException {
        this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
        this.outgoingDatagramSocket = new DatagramSocket();
    }

    private void scheduleWorkers() {
        if (this.workerExecutorService != null) {
            this.workerExecutorService.shutdownNow();
        }

        this.workerExecutorService = Executors.newFixedThreadPool(4);

        this.workerExecutorService.execute(new ServerReceiverWorker());
    }


    private void startHeartbeatSender() {
        if (this.heartbeatExecutor != null) {
            this.heartbeatExecutor.shutdownNow();
        }

        this.heartbeatExecutor = new ScheduledThreadPoolExecutor(4);

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

        this.stopHeartbeatSender();
        this.stopExecutorService();
        this.closeTCPSocket();
        this.closeUDPSockets();
        this.connectionState = ConnectionState.DISCONNECTED;

        System.out.println("Disconnected");

    }

    private void stopExecutorService() {
        if (this.workerExecutorService == null) {
            return;
        }

        this.workerExecutorService.shutdownNow();
    }

    private void closeTCPSocket() {
        try {

            if (this.socket != null) {
                this.socket.close();
            }

        } catch (IOException e) {
        }
    }

    private void closeUDPSockets() {
        if (this.incomingDatagramSocket != null) {
            this.incomingDatagramSocket.close();
        }

        if (this.outgoingDatagramSocket != null) {
            this.outgoingDatagramSocket.close();
        }
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

        if (event == null) {
            return;
        }

        System.out.println(event.getType());

        switch (event.getType()) {

            case CLIENT_CONNECT_SERVER:
                this.connect(SERVER_IP_ADDRESS);
                break;
            case CLIENT_DISCONNECT_SERVER:
                this.disconnect();
                break;
            case CLIENT_KEYBOARD_INPUT:
                ServerSenderWorker s = new ServerSenderWorker(this.multiplyCommands(this.constructInstructionString(event.getKey())));
                this.heartbeatExecutor.execute(s);
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

        if (key == null) {
            return base;
        }

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

        if (key == null) {
            return base;
        }

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

//                System.out.println("Transmitting connectionState to display");

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
//                    System.out.println(s);
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

package net.fila3game.server;

import net.fila3game.server.gameengine.GameEngine;
//import net.jchapa.chapautils.FileIOManager;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * Created by chapa on 2/18/2017.
 */
public class GameServer {
    public static final int SEND_INTERVAL = 25;
    public static final int GAME_ENGINE_SENDING_INTERVAL = 50;
    public static final int KEEPALIVE_INTERVAL = 100;
    public static final String KEEPALIVE_MESSAGE = "a";
    public static final int TCP_CONNECTION_PORT = 8080;
    public static final int RECEIVING_UDP_CONNECTION_PORT = 55355;
    public static final int SENDING_UDP_CONNECTION_PORT = 55356;
    public static final String STRING_ENCODING = "UTF-8";
    public static final String COMMAND_TOKEN_SEPARATOR = " ";


    //debug toggles
    private static boolean D_REQUIRE_ENGINE = false;


    public static void main(String[] args) {
        GameServer gm = null;
        try {
            gm = new GameServer();
            gm.start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

    }

    private ServerSocket serverSocket;
    private DatagramSocket incomingDatagramSocket;
    private DatagramSocket outgoingDatagramSocket;
    private GameEngine engine;
    private ConcurrentMap<String, ClientStatusWorker> currentClients;
    private final ConcurrentMap<String, Instruction> currentInstructions;
    private ExecutorService normalExecutorService;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;


    public GameServer() throws IOException {
        this.serverSocket = new ServerSocket(TCP_CONNECTION_PORT);
        this.currentClients = new ConcurrentHashMap<>();
        this.currentInstructions = new ConcurrentHashMap<>();
        this.normalExecutorService = Executors.newFixedThreadPool(100);
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);
        this.engine = new GameEngine();
    }

    private void start() throws IOException {

        if (D_REQUIRE_ENGINE && this.engine == null) {
            throw new IllegalStateException("no engine");
        }

        this.initializeGeneralUDPSockets();
        this.scheduleGeneralUDPWorkers();


        while (true) {
            this.acceptClient();
        }

    }


    private void initializeGeneralUDPSockets() throws IOException{
        this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
        this.outgoingDatagramSocket = new DatagramSocket();
    }


    private void scheduleGeneralUDPWorkers() {
        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new ClientBroadcastWorker(), 0, SEND_INTERVAL, TimeUnit.MILLISECONDS);
        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new InstructionTableCleaner(), 0, GAME_ENGINE_SENDING_INTERVAL, TimeUnit.MILLISECONDS);
        this.normalExecutorService.execute(new ClientReceiverWorker());
    }



    private void acceptClient() throws IOException {
        Socket socket = this.serverSocket.accept();
//            System.out.println("Server accepted connection");
        this.normalExecutorService.execute(new ClientStatusWorker(socket));
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    private class ClientStatusWorker implements Runnable {

        private Socket socket;
        private InetAddress clientIPAddress;

        private BufferedReader reader;
        private BufferedWriter writer;

        private ExecutorService heartbeatExecutor;

        private String identifier;

        public ClientStatusWorker(Socket socket) throws UnknownHostException {
            this.socket = socket;
            this.clientIPAddress = InetAddress.getByName(socket.getInetAddress().toString().substring(1));
        }



        @Override
        public void run() {

                try {

                    int playerNumber = this.addGameEnginePlayerReference();

                    if (playerNumber < 1) {
//                        Thread.sleep(1000);
//                        this.run();
//                        return;
                        throw new IOException();
                    }

                    System.out.println(playerNumber);

                    this.constructClientIdentifier(playerNumber);
                    this.sendInitialConfiguration();
                    System.out.println("test");

                    this.registerSelfToActiveClientList();
                    this.startHeartbeatReceiver();

                } catch (IOException e) {
                    e.printStackTrace();
                    this.safelyShutdownClientConnection();
//                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        }

        private void sendInitialConfiguration() throws IOException{
            this.tcpSend(this.identifier + "");
            this.tcpSend(this.getEnginePlayerNumberFromId() + "");
            System.out.println("configuration sent");
        }

        private int addGameEnginePlayerReference() {
            return GameServer.this.engine.addTank();
        }

        private void removeGameEnginePlayerReference() {
            GameServer.this.engine.removeTankOfPlayerNumber(this.getEnginePlayerNumberFromId());
        }

        private int getEnginePlayerNumberFromId() {
            try {
                return Integer.parseInt(this.identifier.split(" ")[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out.println("WRONG PLAYER IN COMMAND FORMAT");
                System.exit(0);
            }
            return -4;
        }


        private void constructClientIdentifier(int playerNumber) {
            this.identifier = playerNumber + " " + this.clientIPAddress.toString().concat(" " + Long.toString(System.currentTimeMillis()));
            System.out.println(this.identifier);
        }

        private void startHeartbeatReceiver() {
            if (this.heartbeatExecutor == null) {
                this.heartbeatExecutor = Executors.newFixedThreadPool(1);
            }

            this.heartbeatExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String heartBeat = ClientStatusWorker.this.tcpReceive();
                            ClientStatusWorker.this.tcpSend(ClientStatusWorker.this.getHeartbeatMessage());
                        } catch (IOException e) {
                            ClientStatusWorker.this.safelyShutdownClientConnection();
                            break;
                        }
                    }
                }
            });

        }


        private String getHeartbeatMessage() {
            return KEEPALIVE_MESSAGE;
        }

        private void stopHeartbeatReceiver() {
            if (this.heartbeatExecutor == null) {
                return;
            }

            this.heartbeatExecutor.shutdownNow();
        }

        private void tcpSend(String msg) throws IOException {
            if (this.writer == null) {
                this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), STRING_ENCODING));
            }
            this.writer.write(msg + "\n");
            this.writer.flush();
        }

        public String tcpReceive() throws IOException {
            if (this.reader == null) {
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), STRING_ENCODING));
            }
            return this.reader.readLine();
        }

        private void safelyShutdownClientConnection() {
            this.removeGameEnginePlayerReference();
            this.deRegisterSelfFromActiveClientList();
            this.stopHeartbeatReceiver();
            this.safelyShutdownTCP();
            System.out.println("Client " + this.identifier + " has disconnected.");
        }


        public void safelyShutdownTCP() {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private boolean registerSelfToActiveClientList() {
            return GameServer.this.currentClients.put(this.identifier, this) != null;
        }

        private void deRegisterSelfFromActiveClientList() {
            GameServer.this.currentClients.remove(this.identifier);
        }


        public InetAddress getClientIPAddress() {
            return clientIPAddress;
        }


    }

    private class ClientReceiverWorker implements Runnable {

        @Override
        public void run() {

            if (GameServer.this.incomingDatagramSocket.isClosed()) {
                return;
            }

            try {

                while (true) {
                    byte[] buf = new byte[20000];

                    DatagramPacket dp = new DatagramPacket(buf, buf.length);

                    GameServer.this.incomingDatagramSocket.receive(dp);

                    String command = new String(buf, 0, dp.getLength(), STRING_ENCODING).trim();

                    ClientReceiverWorker.this.parseCommand(command);
                }


            } catch (IOException e) {
                // not handled here
            }
        }

        private void parseCommand(String command) {
            String[] tokens = command.split(COMMAND_TOKEN_SEPARATOR);

            if (!this.isValidCommand(tokens)) {
                return;
            }

            String identifier = tokens[0] + " " + tokens[tokens.length - 1];

            if (GameServer.this.currentInstructions.containsKey(identifier)) {
                return;
            }

            GameServer.this.currentInstructions.put(identifier, new Instruction(command) );

//            System.out.println(identifier);

        }

        private boolean isValidCommand(String[] commandTokens) {
            if (commandTokens.length < 3) {
                return false;
            }

            //TODO with regex

            try {
                Integer.parseInt(commandTokens[0]);
                Long.parseLong(commandTokens[commandTokens.length - 1]);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                return false;
            }

            return true;
        }

    }


    private class ClientBroadcastWorker implements Runnable {


        @Override
        public void run() {

            if (GameServer.this.outgoingDatagramSocket.isClosed()) {
                return;
            }

            Iterator<String> iterator = GameServer.this.currentClients.keySet().iterator();

            while (iterator.hasNext()) {
                ClientStatusWorker worker = GameServer.this.currentClients.get(iterator.next());

                if (worker == null) {
                    continue;
                }

//                System.out.println("Server sending message:");

                String message =  GameServer.this.engine.calculateState();
//                String message = "hello";

                byte[] b = message.getBytes();
                DatagramPacket p = new DatagramPacket(b, 0, b.length, worker.getClientIPAddress(), SENDING_UDP_CONNECTION_PORT);
                try {
                    GameServer.this.outgoingDatagramSocket.send(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }



    }




    private class InstructionTableCleaner implements Runnable {

        @Override
        public void run() {


            synchronized (GameServer.this.currentInstructions) {
                StringBuilder sb = new StringBuilder();
                Iterator<String> iter = GameServer.this.currentInstructions.keySet().iterator();
                while (iter.hasNext()) {
                    String elem = iter.next();
                    sb.append(elem);

                    Instruction i = GameServer.this.currentInstructions.get(elem);

//                    System.out.println(i.getPlayerNumber());

                    GameServer.this.engine.receiveInstruction(i);
                }

//                if (sb.length() > 0) {
//                    System.out.println(sb.toString());
//                }


                GameServer.this.currentInstructions.clear();
            }

        }
    }


}

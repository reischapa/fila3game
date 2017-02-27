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
    public static final int SERVER_BROADCAST_INTERVAL = 34;
    public static final int SERVER_GAME_ENGINE_QUERY_INTERVAL = 25;
    public static final int SERVER_TCP_CONNECTION_PORT = 8080;
    public static final int SERVER_RECEIVING_UDP_PORT = 55355;
    public static final int SERVER_SENDING_UDP_PORT = 55356;

    public static final int SERVER_N_CLIENT_EXECUTOR_SERVICE_THREADS = 10;
    public static final int SERVER_N_SCHEDULED_EXECUTOR_SERVICE_THREADS = 10;

    public static final String STRING_ENCODING = "UTF-8";
    public static final String COMMAND_TOKEN_SEPARATOR = " ";




    public static void main(String[] args) {
        GameServer gm = new GameServer();
        gm.setEngine(new GameEngine());
        gm.start();
    }

    private ServerSocket serverSocket;
    private DatagramSocket incomingDatagramSocket;
    private DatagramSocket outgoingDatagramSocket;
    private GameEngine engine;
    private ConcurrentMap<String, ClientStatusWorker> currentClients;
    private final ConcurrentMap<String, Instruction> currentInstructions;
    private ExecutorService clientExecutorService;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;


    public GameServer() {
        this.currentClients = new ConcurrentHashMap<>();
        this.currentInstructions = new ConcurrentHashMap<>();
    }

    private void start() {

        if (this.engine == null) {
            throw new IllegalStateException("no engine");
        }

        try {

            this.serverSocket = new ServerSocket(SERVER_TCP_CONNECTION_PORT);

            this.initializeGeneralUDPSockets();
            this.initializeExecutorServices();

            this.scheduleGeneralUDPWorkers();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        while (true) {
            try {
                this.acceptClient();
            } catch (IOException e) {
                System.out.println("client failed to connect");
                e.printStackTrace();
            }
        }

    }


    private void initializeGeneralUDPSockets() throws IOException{
        this.incomingDatagramSocket = new DatagramSocket(SERVER_RECEIVING_UDP_PORT);
        this.outgoingDatagramSocket = new DatagramSocket();
    }


    private void initializeExecutorServices() {

        if (this.scheduledThreadPoolExecutor != null) {
            this.scheduledThreadPoolExecutor.shutdownNow();
        }

        if (this.clientExecutorService != null) {
            this.clientExecutorService.shutdownNow();
        }

        this.clientExecutorService = Executors.newFixedThreadPool(SERVER_N_CLIENT_EXECUTOR_SERVICE_THREADS);
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(SERVER_N_SCHEDULED_EXECUTOR_SERVICE_THREADS);
    }

    private void scheduleGeneralUDPWorkers() {

        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new ClientBroadcastWorker(), 0, SERVER_BROADCAST_INTERVAL, TimeUnit.MILLISECONDS);
        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new InstructionTableCleaner(), 0, SERVER_GAME_ENGINE_QUERY_INTERVAL, TimeUnit.MILLISECONDS);
        this.clientExecutorService.execute(new ClientReceiverWorker());
    }



    private void acceptClient() throws IOException{
        Socket socket = this.serverSocket.accept();
        this.clientExecutorService.execute(new ClientStatusWorker(socket));
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    private class ClientStatusWorker implements Runnable {

        private Socket socket;
        private InetAddress clientIPAddress;

        private BufferedReader reader;
        private BufferedWriter writer;

        private ExecutorService statusExecutor;

        private String identifier;
        private int assignedPlayerNumber;

        public ClientStatusWorker(Socket socket) throws UnknownHostException {
            this.socket = socket;
            this.clientIPAddress = InetAddress.getByName(socket.getInetAddress().toString().substring(1));
        }



        @Override
        public void run() {

                try {

                    int playerNumber = this.addGameEnginePlayerReference();

                    if (playerNumber < 1) {
                        //TODO wait queue
                        throw new IOException();
                    }

                    System.out.println(playerNumber);

                    this.constructClientIdentifier();
                    this.assignedPlayerNumber = playerNumber;
                    this.sendInitialConfiguration();

                    this.registerSelfToActiveClientList();
                    this.startStatusTransmitter();

                } catch (IOException e) {
                    e.printStackTrace();
                    this.safelyShutdownClientConnection();
//                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        }

        private void sendInitialConfiguration() throws IOException{
            this.tcpSend(this.identifier + "");
            this.tcpSend(this.assignedPlayerNumber + "");
            System.out.println("configuration sent");
        }

        private int addGameEnginePlayerReference() {
            return GameServer.this.engine.addTank();
        }

        private void removeGameEnginePlayerReference() {
            GameServer.this.engine.removeTankOfPlayerNumber(this.assignedPlayerNumber);
        }

        private void constructClientIdentifier() {
            this.identifier = this.clientIPAddress.toString().concat(" " + Long.toString(System.currentTimeMillis()));
            System.out.println(this.identifier);
        }

        private void startStatusTransmitter() {
            if (this.statusExecutor != null) {
                this.statusExecutor.shutdownNow();
            }

            this.statusExecutor = Executors.newFixedThreadPool(1);

            this.statusExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            ClientStatusWorker.this.tcpReceive();
                            ClientStatusWorker.this.tcpSend(ClientStatusWorker.this.getStatusMessage());
                        } catch (IOException e) {
                            ClientStatusWorker.this.safelyShutdownClientConnection();
                            break;
                        }
                    }
                }
            });

        }


        private String getStatusMessage() {

            if (GameServer.this.engine.isPlayerDead(this.assignedPlayerNumber)) {
                return -1 + "";
            }

            return Integer.toString(this.assignedPlayerNumber);
        }

        private void stopStatusTransmitter() {
            if (this.statusExecutor == null) {
                return;
            }

            this.statusExecutor.shutdownNow();
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
            this.stopStatusTransmitter();
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
                DatagramPacket p = new DatagramPacket(b, 0, b.length, worker.getClientIPAddress(), SERVER_SENDING_UDP_PORT);
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

                Iterator<String> iter = GameServer.this.currentInstructions.keySet().iterator();
                while (iter.hasNext()) {
                    String elem = iter.next();

                    Instruction i = GameServer.this.currentInstructions.get(elem);

                    if (i == null) {
                        continue;
                    }

                    GameServer.this.engine.receiveInstruction(i);
                }

                GameServer.this.currentInstructions.clear();


        }

    }



}



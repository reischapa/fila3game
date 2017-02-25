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
    public static final int GAME_ENGINE_SENDING_INTERVAL = 30;
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

        this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
        this.outgoingDatagramSocket = new DatagramSocket();

        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new ClientBroadcastWorker(), 0, SEND_INTERVAL, TimeUnit.MILLISECONDS);
        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new InstructionTableCleaner(), 0, GAME_ENGINE_SENDING_INTERVAL, TimeUnit.MILLISECONDS);

        this.normalExecutorService.execute(new ClientReceiverWorker());


        while (true) {
            Socket socket = this.serverSocket.accept();
//            System.out.println("Server accepted connection");
            this.normalExecutorService.execute(new ClientStatusWorker(socket));
        }

    }


    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }


    private class ClientReceiverWorker implements Runnable {

        @Override
        public void run() {

            try {

                while (true) {
                    byte[] buf = new byte[20000];

                    DatagramPacket dp = new DatagramPacket(buf, buf.length);

                    GameServer.this.incomingDatagramSocket.receive(dp);

                    String command = new String(buf, 0, dp.getLength(), STRING_ENCODING).trim();
                    System.out.println("System received message: " + command );
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


    private class ClientStatusWorker implements Runnable {

        private Socket socket;
        private InetAddress clientIPAddress;

        private BufferedReader reader;
        private BufferedWriter writer;

        private String identifier;

        public ClientStatusWorker(Socket socket) throws UnknownHostException, IOException {
            this.socket = socket;
            this.clientIPAddress = InetAddress.getByName(socket.getInetAddress().toString().substring(1));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), STRING_ENCODING));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), STRING_ENCODING));
        }

        @Override
        public void run() {



            try {


                int playerNumber = this.addPlayer();

                if (playerNumber < 0) {
                    this.handleGameFull();
                }

                System.out.println(playerNumber);

                this.constructClientIdentifier(playerNumber);

                this.send(playerNumber + "");

                this.registerSelf();

            } catch (IOException e) {
                e.printStackTrace();
                this.safelyShutdownClientConnection();
            }


        }

        public void constructClientIdentifier(int playerNumber) {
            this.identifier =  playerNumber + " " +  this.clientIPAddress.toString().concat(" " + Long.toString(System.currentTimeMillis()));
            System.out.println(this.identifier);
        }

        public int addPlayer() {
            return GameServer.this.engine.addTank();
        }

        public void handleGameFull() throws IOException{
            throw new IOException("Game Full");
            //TODO
        }

        public void send(String msg) throws IOException {
            this.writer.write(msg + "\n");
            this.writer.flush();
        }

        public void safelyShutdownClientConnection() {
            this.deRegisterSelf();
            this.safelyShutdownTCP();
        }


        public void safelyShutdownTCP() {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private boolean registerSelf() {
            return GameServer.this.currentClients.put(this.identifier, this) != null;
        }

        private void deRegisterSelf() {
            GameServer.this.currentClients.remove(this.identifier);
        }

        private String getKeepAliveMessage() {
            return KEEPALIVE_MESSAGE;
        }


        public InetAddress getClientIPAddress() {
            return clientIPAddress;
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

                    System.out.println(i.getPlayerNumber());

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

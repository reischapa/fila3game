package net.fila3game.server;

import com.sun.xml.internal.ws.api.pipe.Engine;
import net.fila3game.server.gameengine.GameEngine;
//import net.jchapa.chapautils.FileIOManager;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by chapa on 2/18/2017.
 */
public class GameServer {
    public static final int SEND_INTERVAL = 25;
    public static final int GAME_ENGINE_SENDING_INTERVAL = 100;
    public static final int KEEPALIVE_INTERVAL = 100;
    public static final String KEEPALIVE_MESSAGE = "a\n";
    public static final int TCP_CONNECTION_PORT = 8080;
    public static final int RECEIVING_UDP_CONNECTION_PORT = 55355;
    public static final int SENDING_UDP_CONNECTION_PORT = 55356;
    public static final String STRING_ENCODING = "UTF-8";
    public static final String COMMAND_TOKEN_SEPARATOR = " ";

    public static final int RECEIVE_INTERVAL = 25;

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
    private ConcurrentMap<String, Instruction> currentInstructions;
    private ExecutorService normalExecutorService;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public GameServer() throws IOException {
        this.serverSocket = new ServerSocket(TCP_CONNECTION_PORT);
        this.currentClients = new ConcurrentHashMap<>();
        this.currentInstructions = new ConcurrentHashMap<>();
        this.normalExecutorService = Executors.newFixedThreadPool(100);
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);
    }

    private void start() throws IOException {

        if (D_REQUIRE_ENGINE && this.engine == null) {
            throw new IllegalStateException("no engine");
        }

        this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
        this.outgoingDatagramSocket = new DatagramSocket();

        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new ClientBroadcastWorker(this.outgoingDatagramSocket, this.currentClients), 0, RECEIVE_INTERVAL, TimeUnit.MILLISECONDS);
        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new InstructionTableCleaner(this.currentInstructions, this.engine), 0, GAME_ENGINE_SENDING_INTERVAL, TimeUnit.MILLISECONDS);

        this.normalExecutorService.execute(new ClientReceiverWorker(this.incomingDatagramSocket, this.currentInstructions));


        while (true) {
            Socket socket = this.serverSocket.accept();
            this.normalExecutorService.execute(new ClientStatusWorker(socket));
        }

    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }


    private class ClientReceiverWorker implements Runnable {

        private DatagramSocket datagramSocket;
        private ConcurrentMap<String, Instruction> instructionTable;

        public ClientReceiverWorker(DatagramSocket datagramSocket, ConcurrentMap<String, Instruction> instructionTable) {
            this.datagramSocket = datagramSocket;
            this.instructionTable = instructionTable;
        }

        @Override
        public void run() {

            try {

                while (true) {
                    byte[] buf = new byte[20000];

                    DatagramPacket dp = new DatagramPacket(buf, buf.length);

                    this.datagramSocket.receive(dp);

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

            if (this.instructionTable.containsKey(identifier)) {
                return;
            }

            this.instructionTable.put(identifier, new Instruction(Integer.parseInt(tokens[0]), Instruction.Type.D) );

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

        private ConcurrentMap<String, ClientStatusWorker> serverClients;
        private DatagramSocket outgoingDatagramSocket;


        public ClientBroadcastWorker(DatagramSocket outgoingDatagramSocket, ConcurrentMap<String, ClientStatusWorker> serverClients ) {
            this.outgoingDatagramSocket = outgoingDatagramSocket;
            this.serverClients = serverClients;
        }

        @Override
        public void run() {

            Iterator<String> iterator = this.serverClients.keySet().iterator();

            while (iterator.hasNext()) {
                ClientStatusWorker worker = serverClients.get(iterator.next());

                //TODO get data from the gameEngine
                String message = "Hello world";

                byte[] b = message.getBytes();
                DatagramPacket p = new DatagramPacket(b, 0, b.length, worker.getClientIPAddress(), SENDING_UDP_CONNECTION_PORT);
                try {
                    this.outgoingDatagramSocket.send(p);
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

        private ScheduledThreadPoolExecutor executorService;

        private String identifier;

        public ClientStatusWorker(Socket socket) throws UnknownHostException, IOException {
            this.socket = socket;
            this.clientIPAddress = InetAddress.getByName(socket.getInetAddress().toString().substring(1));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), STRING_ENCODING));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), STRING_ENCODING));
            this.executorService = new ScheduledThreadPoolExecutor(2);
            this.identifier = this.clientIPAddress.toString().concat(" " + Long.toString(System.currentTimeMillis()));
            this.registerSelf();
        }

        @Override
        public void run() {


            try {
                //TODO server side comunication with client
                //define protocol?
                this.performHandshake();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
            }

            this.safelyShutdownClientConnection();

        }

        public void performHandshake() throws InterruptedException, IOException {
            while (true) {
                writer.write(this.getKeepAliveMessage());
                writer.flush();
                Thread.sleep(KEEPALIVE_INTERVAL);
            }
        }

        public void safelyShutdownClientConnection() {
            this.deRegisterSelf();
            this.safelyShutdownTCP();
            this.safelyUnscheduleThreadExecutions();
        }

        public void safelyUnscheduleThreadExecutions() {
            this.executorService.shutdownNow();
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

        private final ConcurrentMap<String, Instruction> instructionTable;
        private GameEngine engine;

        public InstructionTableCleaner(ConcurrentMap<String, Instruction> instructionTable, GameEngine engine) {
            this.instructionTable = instructionTable;
            this.engine = engine;
        }

        @Override
        public void run() {


            synchronized (this.instructionTable) {
                StringBuilder sb = new StringBuilder();
                Iterator<String> iter = this.instructionTable.keySet().iterator();
                while (iter.hasNext()) {
                    String elem = iter.next();
                    sb.append("\n" + elem);
                }

                System.out.println(sb.toString());
                this.instructionTable.clear();
            }




        }
    }


}

package net.fila3game.server;

import net.fila3game.server.gameengine.GameEngine;
//import net.jchapa.chapautils.FileIOManager;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * Created by chapa on 2/18/2017.
 */
public class GameServer {
    public static final int SEND_INTERVAL = 25;
    public static final int KEEPALIVE_INTERVAL = 100;
    public static final String KEEPALIVE_MESSAGE = "a";
    public static final int TCP_CONNECTION_PORT = 8080;
    public static final int RECEIVING_UDP_CONNECTION_PORT = 55355;
    public static final int SENDING_UDP_CONNECTION_PORT = 55356;
    public static final String STRING_ENCODING = "UTF-8";

    public static final int RECEIVE_INTERVAL = 25;


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
    private ConcurrentMap<String, ClientStatusAndReceiverWorker> currentClients;
    private ExecutorService normalExecutorService;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public GameServer() throws IOException {
        this.serverSocket = new ServerSocket(TCP_CONNECTION_PORT);
        this.currentClients = new ConcurrentHashMap<>();
        this.normalExecutorService = Executors.newFixedThreadPool(100);
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);
    }

    private void start() throws IOException {

//        if (this.engine == null) {
//            throw new IllegalStateException("no engine");
//        }

        this.incomingDatagramSocket = new DatagramSocket(RECEIVING_UDP_CONNECTION_PORT);
        this.outgoingDatagramSocket = new DatagramSocket();

        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new ClientCommandReceiverWorker(this.incomingDatagramSocket, null), 0, SEND_INTERVAL, TimeUnit.MILLISECONDS);
        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(new ClientBroadcastWorker(this.outgoingDatagramSocket, this.currentClients), 0, RECEIVE_INTERVAL, TimeUnit.MILLISECONDS);


        while (true) {
            Socket socket = this.serverSocket.accept();
            this.normalExecutorService.execute(new ClientStatusAndReceiverWorker(socket));
        }

    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }



    private class ClientCommandReceiverWorker implements Runnable {

        private DatagramSocket datagramSocket;
        private GameEngine engine;

        public ClientCommandReceiverWorker(DatagramSocket datagramSocket, GameEngine engine) {
            this.datagramSocket = datagramSocket;
            this.engine = engine;
        }

        @Override
        public void run() {

            try {

                byte[] buf = new byte[20000];

                DatagramPacket dp = new DatagramPacket(buf, buf.length);

                this.datagramSocket.receive(dp);

                String command = new String(buf, 0, dp.getLength(), STRING_ENCODING);
                System.out.println(command);

            } catch (IOException e) {
                // not handled here
            }
        }

        private Instruction parseCommand(String command) {
            return null;
        }

    }


    private class ClientBroadcastWorker implements Runnable {

        private ConcurrentMap<String, ClientStatusAndReceiverWorker> serverClients;
        private DatagramSocket outgoingDatagramSocket;


        public ClientBroadcastWorker(DatagramSocket outgoingDatagramSocket, ConcurrentMap<String, ClientStatusAndReceiverWorker> serverClients ) {
            this.outgoingDatagramSocket = outgoingDatagramSocket;
            this.serverClients = serverClients;
        }

        @Override
        public void run() {

            Iterator<String> iterator = this.serverClients.keySet().iterator();

            while (iterator.hasNext()) {
                ClientStatusAndReceiverWorker worker = serverClients.get(iterator.next());

                String message = "Hello world";
                byte[] b = message.getBytes();
                DatagramPacket p = new DatagramPacket(b, 0, b.length, worker.getClientIPAddress(), SENDING_UDP_CONNECTION_PORT);

                try {
                    this.outgoingDatagramSocket.send(p);
                    System.out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    private class ClientStatusAndReceiverWorker implements Runnable {

        private Socket socket;

        private ScheduledThreadPoolExecutor executorService;

        private InetAddress clientIPAddress;
        private String identifier;

        public ClientStatusAndReceiverWorker(Socket socket) throws UnknownHostException, IOException {
            this.socket = socket;
            this.executorService = new ScheduledThreadPoolExecutor(2);
            this.clientIPAddress = InetAddress.getByName(socket.getInetAddress().toString().substring(1));
            this.identifier = this.clientIPAddress.toString().concat(" " + Long.toString(System.currentTimeMillis()));
            this.registerSelf();
        }

        @Override
        public void run() {

            try {

                this.executorService.scheduleAtFixedRate( new ClientStatusCheckerWorker(this.socket) ,0, KEEPALIVE_INTERVAL, TimeUnit.MILLISECONDS );

            } catch (IOException e) {
                e.printStackTrace();
                this.safelyShutdownClientConnection();
            }


        }

        public InetAddress getClientIPAddress() {
            return clientIPAddress;
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


        private class ClientStatusCheckerWorker implements Runnable {

            private BufferedWriter tcpWriter;
            private BufferedReader tcpReader;

            public ClientStatusCheckerWorker(Socket socket) throws IOException {
                this.tcpReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), STRING_ENCODING));
                this.tcpWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), STRING_ENCODING));
            }

            @Override
            public void run() {
                try {
                    tcpWriter.write(KEEPALIVE_MESSAGE);
                    tcpWriter.flush();
                } catch (IOException e) {
                    ClientStatusAndReceiverWorker.this.safelyShutdownClientConnection();
                }
            }

        }



    }

}

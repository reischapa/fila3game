package net.fila3game.server;

import net.fila3game.server.gameengine.Field;
//import net.jchapa.chapautils.FileIOManager;

import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.concurrent.*;

/**
 * Created by chapa on 2/18/2017.
 */
public class GameServer {
    public static final int SEND_INTERVAL = 10000;
    public static final int KEEPALIVE_INTERVAL = 100;
    public static final int TCP_CONNECTION_PORT = 8080;
    public static final int INCOMING_UDP_CONNECTION_PORT = 55354;
    public static final int OUTGOING_UDP_CONNECTION_PORT = 55355;
    public static final int FIELD_WIDTH = 10;
    public static final int FIELD_HEIGHT = 10;


    public static void main(String[] args) {
        GameServer gm = null;
        try {
            gm = new GameServer();
            gm.start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally  {
        }

    }

    private final Field field;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private Timer timer;

    public GameServer() throws IOException{
        this.field = new Field(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        this.serverSocket = new ServerSocket(TCP_CONNECTION_PORT);
        this.executorService = Executors.newFixedThreadPool(100);
        this.timer = new Timer();
    }

    private void start() throws IOException {

        while (true) {
            Socket socket = this.serverSocket.accept();
            this.executorService.execute(new ClientConnectionWorker(socket));
        }
    }

    private class ClientConnectionWorker implements Runnable {

        private Socket socket;
        private DatagramSocket datagramSocket;
        private BufferedWriter tcpWriter;
        private BufferedReader tcpReader;
        private ScheduledThreadPoolExecutor executorService;
        private Inet4Address clientIPAddress;

        public ClientConnectionWorker(Socket socket) throws IOException {
            this.socket = socket;
            this.tcpWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.tcpReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.datagramSocket = new DatagramSocket(INCOMING_UDP_CONNECTION_PORT);
            this.executorService = new ScheduledThreadPoolExecutor(2);
            this.clientIPAddress = (Inet4Address) Inet4Address.getByName(socket.getInetAddress().toString().substring(1));
        }

        @Override
        public void run() {

             Runnable tt = new Runnable() {
                @Override
                public void run() {
                    try {
                        tcpWriter.write("a");
                        tcpWriter.flush();
                    } catch (IOException e) {
                        ClientConnectionWorker.this.GTFO();
                    }
                }
            };


            Runnable dataSender = new Runnable() {
                @Override
                public void run() {

                    try {

                        String content = Long.toString(System.currentTimeMillis());
                        GameServer.this.field.set(0, 0, content.charAt(content.length() - 1));

                        String message;
                        synchronized (GameServer.this.field) {
                            message = GameServer.this.field.returnAsString();
                        }
                        System.out.println(message);

                        byte[] messagebytes = message.getBytes("UTF-8");
                        DatagramPacket packet = new DatagramPacket(messagebytes, messagebytes.length, ClientConnectionWorker.this.clientIPAddress,OUTGOING_UDP_CONNECTION_PORT);
                        ClientConnectionWorker.this.datagramSocket.send(packet);

                    } catch (IOException e) {
                        ClientConnectionWorker.this.GTFO();
                    }

                }
            };



            Runnable dataReceiver = new Runnable() {
                @Override
                public void run() {

                    try {

                        byte[] buf = new byte[20000];

                        DatagramPacket dp = new DatagramPacket(buf, buf.length);

                        ClientConnectionWorker.this.datagramSocket.receive(dp);

                        String result = new String(buf, 0, dp.getLength(),"UTF-8");
                        String[] fff = result.split("\\s+");

                        if (fff.length < 3) {
                            return;
                        }

                        int x = Integer.parseInt(fff[0]);
                        int y = Integer.parseInt(fff[1]);
                        char c = fff[2].charAt(0);

                        synchronized (GameServer.this.field) {
                            GameServer.this.field.set(x, y, c);
                        }

                    } catch (IOException e) {
                        ClientConnectionWorker.this.GTFO();
                    }

                }
            };


            this.executorService.scheduleAtFixedRate(tt, 0, KEEPALIVE_INTERVAL, TimeUnit.MILLISECONDS);
            this.executorService.scheduleAtFixedRate(dataReceiver, 0, SEND_INTERVAL/4, TimeUnit.MILLISECONDS);
            this.executorService.scheduleAtFixedRate(dataSender, 0, SEND_INTERVAL, TimeUnit.MILLISECONDS);

        }

        public void GTFO() {
//            FileIOManager.CLOSEWriter(tcpWriter);
            this.datagramSocket.close();
            this.executorService.shutdownNow();
            System.out.println("closed");
        }

    }

}

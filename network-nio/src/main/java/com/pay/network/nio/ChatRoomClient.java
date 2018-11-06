package com.pay.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Create by mingshuo.gu on 2018/11/5.
 */
public class ChatRoomClient {

    private static String nextLine = null;

    public static void start() throws IOException {
        start("127.0.0.1", 9091);
    }

    public static void start(String host, int port) throws IOException {



        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_WRITE);

        ClientWriter clientWriter = new ClientWriter();
        clientWriter.start();

        while (true) {

            int preparedChannel = selector.select();
            if (preparedChannel == 0) {
                continue;
            }

            Set<SelectionKey> preparedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = preparedKeys.iterator();
            while (iterator.hasNext()) {

                SelectionKey currentKey = iterator.next();
                iterator.remove();

                if (currentKey.isWritable()) {

                    if(nextLine != null){
                        ByteBuffer buffer = ByteBuffer.wrap(nextLine.getBytes());
                        socketChannel.write(buffer);

                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);

                        nextLine = null;
                    }

                } else if (currentKey.isReadable()) {

                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int readNum = socketChannel.read(readBuffer);

                    if (readNum > 0) {

                        readBuffer.flip();

                        byte[] readArray = new byte[readNum];
                        readBuffer.get(readArray);

                        String result = new String(readArray);
                        System.out.println("receive message : " + result);

                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }

    static class ClientWriter extends Thread{
        @Override
        public void run() {

            try {

                while (!Thread.currentThread().isInterrupted()){
                    Scanner scanner = new Scanner(System.in);

                    /*if(Contant.EXIT_MARK.equals(nextLine)) {
                        System.out.println("ready to shutdown...");
                        SHUT_DOWN = true;
                        break;
                    }*/
                    nextLine = scanner.nextLine();
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {

            }
        }
    }

    public static void main(String[] args) throws IOException {
        ChatRoomClient.start();
    }
}

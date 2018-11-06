package com.pay.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Create by mingshuo.gu on 2018/11/5.
 */
public class ChatRoomServer {

    public static void start() throws IOException {
        start(9091);
    }

    public static void start(int port) throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (!Thread.currentThread().isInterrupted()) {

            int prepareChannels = selector.select();
            if (prepareChannels == 0) {
                continue;
            }

            Set<SelectionKey> preparedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = preparedKeys.iterator();
            while (iterator.hasNext()) {

                SelectionKey currentKey = iterator.next();
                iterator.remove();

                if (currentKey.isAcceptable()) {

                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);

                } else if (currentKey.isReadable()) {

                    SocketChannel socketChannel = (SocketChannel) currentKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                    int num = socketChannel.read(byteBuffer);
                    if (num > 0) {

                        String receiveMessage = new String(byteBuffer.array());
                        System.out.println("receive message : " + receiveMessage);

                        ByteBuffer responseBuffer = ByteBuffer.wrap(receiveMessage.getBytes());
                        socketChannel.write(responseBuffer);

                    } else {
                        socketChannel.close();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ChatRoomServer.start();
    }
}


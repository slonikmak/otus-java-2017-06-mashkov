package ru.otus.socketserver.server;

import com.google.gson.Gson;
import ru.otus.socketserver.common.messages.Msg;
import ru.otus.socketserver.common.messages.MsgToServer;
import ru.otus.socketserver.common.socket.SocketMsgClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageServer {
    private static final Logger logger = Logger.getLogger(MessageServer.class.getName());

    private static final int THREADS_NUMBER = 1;
    private static final int PORT = 5050;
    private static final int ECHO_DELAY = 100;
    private static final int CAPACITY = 256;
    private static final String MESSAGES_SEPARATOR = "\n\n";

    private final ExecutorService executor;
    private final Map<String, ChannelMessages> channelMessages;

    public MessageServer() {
        executor = Executors.newFixedThreadPool(THREADS_NUMBER);
        channelMessages = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void start() throws Exception {
        executor.submit(this::echo);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress("localhost", PORT));

            serverSocketChannel.configureBlocking(false); //non blocking mode
            int ops = SelectionKey.OP_ACCEPT;
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, ops, null);

            logger.info("Started on port: " + PORT);

            while (true) {
                selector.select();//blocks
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    try {
                        if (key.isAcceptable()) {
                            SocketChannel channel = serverSocketChannel.accept(); //non blocking accept
                            String remoteAddress = channel.getRemoteAddress().toString();
                            System.out.println("Connection Accepted: " + remoteAddress);

                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);

                            channelMessages.put(remoteAddress, new ChannelMessages(channel));

                        } else if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();

                            ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
                            int read = channel.read(buffer);
                            if (read != -1) {
                                String result = new String(buffer.array()).trim();
                                System.out.println("Message received: " + result + " from: " + channel.getRemoteAddress());

                                //тут обработаем сообщение, получив его из json
                                //и добавим ответ в канал
                                if (!result.equals("")){
                                    MsgToServer msg = (MsgToServer) SocketMsgClient.getMsgFromJSON(result);
                                    msg.setChannelMessages(channelMessages.get(channel.getRemoteAddress().toString()));
                                    Optional<Msg> optional = msg.exec();
                                    optional.ifPresent(m->{
                                        try {
                                            channelMessages.get(channel.getRemoteAddress().toString()).messages.add(new Gson().toJson(m));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }

                                //System.out.println("Message from json:"+result);
                                //channelMessages.get(channel.getRemoteAddress().toString()).messages.add(result);
                            } else {
                                key.cancel();
                                String remoteAddress = channel.getRemoteAddress().toString();
                                channelMessages.remove(remoteAddress);
                                System.out.println("Connection closed, key canceled");
                            }
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, e.getMessage());
                    } finally {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private Object echo() throws InterruptedException {
        while (true) {
            for (Map.Entry<String, ChannelMessages> entry : channelMessages.entrySet()) {
                ChannelMessages channelMessages = entry.getValue();
                if (channelMessages.channel.isConnected()) {
                    channelMessages.messages.forEach(message -> {
                        System.out.println("Echoing message to: " + entry.getKey());
                        sendMessageToChannel(message, channelMessages.channel);
                    });
                    channelMessages.messages.clear();
                }
            }
            Thread.sleep(ECHO_DELAY);
        }
    }

    public void sendMessageToChannel(String msg, SocketChannel channel){
        try {
            ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
            buffer.put(msg.getBytes());
            buffer.put(MESSAGES_SEPARATOR.getBytes());
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }




}
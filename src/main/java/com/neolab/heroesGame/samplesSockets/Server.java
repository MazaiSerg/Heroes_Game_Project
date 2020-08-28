package com.neolab.heroesGame.samplesSockets;

import com.neolab.heroesGame.enumerations.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;


public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    public static final PropsServerManager props = new PropsServerManager();
    public static final int PORT = props.PORT;
    private static final ConcurrentLinkedQueue<PlayerSocket> serverList = new ConcurrentLinkedQueue<>();
    private static final Queue<PlayerSocket> queuePlayers = new LinkedList<>();
    private static final AtomicInteger countGameRooms = new AtomicInteger(0);

    private static void startServer() throws IOException {
        LOGGER.warn("Server started, port: {}", PORT);
        try (final ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {

                // Блокируется до возникновения нового соединения
                final Socket socket = serverSocket.accept();
                final int countPlayers = serverList.size();

                try {
                    if (countPlayers < props.MAX_COUNT_PLAYERS) {
                        createResponseSocket(socket);
                        createGameRoom();
                    } else {
                        final PlayerSocket playerSocket = new PlayerSocket(socket, 0);
                        playerSocket.send(GameEvent.MAX_COUNT_PLAYERS.toString());
                        playerSocket.downService();
                    }
                } catch (final IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его:
                    e.printStackTrace();
                    socket.close();
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
            downService();
        }
    }

    public static AtomicInteger getCountGameRooms() {
        return countGameRooms;
    }

    public static ConcurrentLinkedQueue<PlayerSocket> getServerList() {
        return serverList;
    }

    public static Queue<PlayerSocket> getQueuePlayers() {
        return queuePlayers;
    }

    /**
     * создаём серверный сокет для общения с конкретным клиентом
     *
     * @param socket копия клиенсткого сокета на стороне сервера
     */
    private static void createResponseSocket(final Socket socket) throws IOException, InterruptedException {
        final int playerId = idGenerator.getAsInt();
        final PlayerSocket playerSocket = new PlayerSocket(socket, playerId);
        final String name = playerSocket.getIn().readLine();
        playerSocket.setPlayerName(name);
        playerSocket.send(String.valueOf(playerId));

        if (playerSocket.isAssignIdAndNameClient()) {
            serverList.add(playerSocket);
            queuePlayers.add(playerSocket);
        }
    }

    /**
     * если в очереди есть 2 свободных игрока то создаем комнату
     */
    private static void createGameRoom() {

        if (countGameRooms.get() < props.MAX_COUNT_GAME_ROOMS && queuePlayers.size() >= 2) {
            countGameRooms.incrementAndGet();
            new GameRoom(queuePlayers, props.MAX_COUNT_BATTLES).start();
        }
    }

    /**
     * закрытие сервера, удаление себя из списка нитей
     */
    private static void downService() throws IOException {
        for (final PlayerSocket playerSocket : serverList) {
            playerSocket.downService();
        }
    }

    /**
     * Для генеариия id игроков
     */
    private static final IntSupplier idGenerator = new IntSupplier() {
        private int id = 1;

        public int getAsInt() {
            return id++;
        }
    };

    private static String generatePlayerName(final int playerId) {
        return String.format("player_%d", playerId);
    }


    public static void main(final String[] args) throws IOException {
        Server.startServer();
    }
}


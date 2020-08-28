package com.neolab.heroesGame.samplesSockets;

import com.neolab.heroesGame.enumerations.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class PlayerSocket {

    private final Socket socket;
    private final BufferedReader in; // поток чтения из сокета
    private final BufferedWriter out; // поток завписи в сокет
    private final int playerId;
    private String playerName;
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerSocket.class);

    public BufferedReader getIn() {
        return in;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    /**
     * Для общения с клиентом необходим сокет (адресные данные)
     *
     * @param socket сокет
     */
    PlayerSocket(final Socket socket, final int playerId) throws IOException {
        this.playerId = playerId;
        this.socket = socket;
        // если потоки ввода/вывода приведут к генерированию искдючения, оно проброситься дальше
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * проверяем что клиент получил от сервера id и name, и готов к сессии
     *
     * @return флаг указывающий что клиент установил полученные id и name
     */
    public boolean isAssignIdAndNameClient() throws IOException, InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread.sleep(50);
            if (in.ready()) {
                final String response = in.readLine();
                if (response.equals(GameEvent.CLIENT_IS_CREATED.toString())) {
                    return true;
                }
                downService();
                return false;
            }
            Thread.sleep(500);
        }
        downService();
        return false;
    }


    /**
     * отсылка одного сообщения клиенту
     *
     * @param msg сообщение
     */
    public void send(final String msg) throws IOException {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (final IOException ex) {
            LOGGER.error("Игрок {} разорвал соединение", playerId);
            downService();
            Server.getServerList().remove(this);
            Server.getQueuePlayers().remove(this);
        }
    }

    public void downService() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
            in.close();
            out.close();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerSocket)) return false;
        final PlayerSocket that = (PlayerSocket) o;
        return getPlayerId() == that.getPlayerId() &&
                socket.equals(that.socket) &&
                getIn().equals(that.getIn()) &&
                out.equals(that.out) &&
                getPlayerName().equals(that.getPlayerName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket, getIn(), out, getPlayerId(), getPlayerName());
    }
}
package com.neolab.heroesGame.samplesSockets;

import com.neolab.heroesGame.ClientPlayerImitation;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.dto.ExtendedServerResponse;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;

import java.io.*;
import java.net.Socket;

/**
 * Консольный многопользовательский чат.
 * Клиент
 */
public class Client {

    private static final String IP = "135.181.85.225";//"localhost";
    private static final int PORT = 8081;

    private final String ip; // ip адрес клиента
    private final int port; // порт соединения

    private Socket socket = null;
    private BufferedReader in = null; // поток чтения из сокета
    private BufferedWriter out = null; // поток записи в сокет
    private ClientPlayerImitation player;

    /**
     * для создания необходимо принять адрес и номер порта
     *
     * @param ip   ip адрес клиента
     * @param port порт соединения
     */
    private Client(final String ip, final int port) {
        this.ip = ip;
        this.port = port;
    }

    private void startClient() {
        try {
            socket = new Socket(ip, port);
        } catch (final IOException e) {
            System.err.println("Socket failed");
            return;
        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (final IOException ex) {
            ex.printStackTrace();
            downService();
            return;
        }

        System.out.println(String.format("Client started, ip: %s, port: %d", ip, port));
        gameRun(); // читаем и пишем в поток сообщений в бесконечном цикле
    }

    /**
     * отсылка одного сообщения клиенту
     *
     * @param message сообщение
     */
    private void send(final String message) throws IOException {
        out.write(message + "\n");
        out.flush();
    }

    /**
     * закрытие сокета
     */
    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (final IOException ignored) {
        }
    }


    private void gameRun() {

        try {
            /*
             * получаем id и имя игрока от сервера и создаем нашего бота
             * если на сервере уже максимум игроков, то подключиться не удастся
             */
            if (!createPlayer()) {
                System.out.println("На сервере подключено максимально число игроков, попробуйте позже...");
                return;
            }

            collectArmy();

            while (true) {
                final String requestJson = in.readLine(); // ждем сообщения с сервера
                if (requestJson == null) {
                    downService();
                    break;
                }

                final ExtendedServerResponse response = ExtendedServerResponse.getResponseFromString(requestJson);

                switch (response.event) {
                    case NOW_YOUR_TURN -> send(player.getAnswer(response));
                    case WAIT_ITS_NOT_YOUR_TURN -> player.sendInformation(response);
                    case YOU_WIN_GAME, YOU_LOSE_GAME, GAME_END_WITH_A_TIE -> {
                        player.endGame(response);
                        System.out.printf("You %s\n", response.event.getDescription());
                        collectArmy();
                    }
                    default -> throw new HeroExceptions(HeroErrorCode.ERROR_EVENT);
                }
            }
        } catch (final Exception e) {
            downService();
            e.printStackTrace();
        }
    }

    private boolean createPlayer() throws IOException {
        send(player.getPlayerName());
        final String res = in.readLine();
        // на сервере уже максимально число игроков
        if (res.equals(GameEvent.MAX_COUNT_PLAYERS.toString())) {
            return false;
        }
        final int playerId = Integer.parseInt(res);
        player.setPlayerId(playerId);
        send(GameEvent.CLIENT_IS_CREATED.toString());
        return true;
    }

    /**
     * получаем от сервера размер армии и армию
     * противника и на основе этих данных
     * создаем собственную армию
     */
    private void collectArmy() throws IOException, HeroExceptions {
        final String armySize = in.readLine();
        final String enemyArmy = in.readLine();
        if (enemyArmy.isEmpty()) {
            send(player.getArmyFirst(Integer.parseInt(armySize)));
        } else {
            send(player.getArmySecond(Integer.parseInt(armySize), new StringArmyFactory(enemyArmy).create()));
        }

        final String serverRes = in.readLine();
        if (serverRes.equals(GameEvent.ERROR_ARMY_CREATED.toString())) {
            collectArmy();
        }
    }


    public static void main(final String[] args) {
        final Client client = new Client(IP, PORT);
        if (args.length == 0) {
            client.player = ClientPlayerImitation.createBot(0, BotType.MULTI_ARMED_WITHOUT_RECURSIVE);
        } else {
            client.player = ClientPlayerImitation.createBot(0, BotType.valueOf(args[0]));
        }
        client.startClient();
    }
}

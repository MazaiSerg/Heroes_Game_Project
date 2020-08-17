package com.neolab.heroesGame;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerBot;
import com.neolab.heroesGame.client.ai.PlayerFactory;
import com.neolab.heroesGame.client.ai.PlayerHuman;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.dto.ClientRequest;
import com.neolab.heroesGame.client.dto.ExtendedServerResponse;
import com.neolab.heroesGame.client.gui.IGraphics;
import com.neolab.heroesGame.client.gui.NullGraphics;
import com.neolab.heroesGame.client.gui.console.AsciiGraphics;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;

import java.io.IOException;
import java.util.Scanner;

public class ClientPlayerImitation {
    private final Player player;
    private final IGraphics gui;

    public ClientPlayerImitation(final int playerId, final String name) {
        player = new PlayerBot(playerId, name);
        gui = new NullGraphics();
    }

    public ClientPlayerImitation(final int playerId, final String name, final IGraphics gui) {
        player = new PlayerBot(playerId, name);
        this.gui = gui;
    }

    private ClientPlayerImitation(final Player player,
                                  final IGraphics gui) {
        this.player = player;
        this.gui = gui;
    }

    public static ClientPlayerImitation createPlayerWithAsciiGraphics(final int playerId,
                                                                      final String name) throws IOException {
        final IGraphics graphics = new AsciiGraphics(playerId);
        return new ClientPlayerImitation(playerId, name, graphics);
    }

    public static ClientPlayerImitation createHumanPlayerWithAsciiGraphics(final int playerId,
                                                                           final String name) throws IOException {
        final IGraphics graphics = new AsciiGraphics(playerId);
        final Player human = new PlayerHuman(playerId, name, graphics);
        return new ClientPlayerImitation(human, graphics);
    }

    public static ClientPlayerImitation createBot(final int playerId, final BotType type) {
        final Player simpleBot = PlayerFactory.createPlayerBot(type, playerId);
        return new ClientPlayerImitation(simpleBot, new NullGraphics());
    }

    public static ClientPlayerImitation createCustomPlayer(final int playerId, final String name) throws IOException {
        System.out.println("1. Создать бота без графической отрисовки");
        System.out.println("2. Создать бота с графической отрисовки");
        System.out.println("3. Создать игрока человека");
        while (true) {
            final Scanner in = new Scanner(System.in);
            int choose = 1;
            try {
                choose = in.nextInt();
                if (choose < 1 || choose > 3) {
                    System.out.println("Неверный формат. Нужно ввести число от 1 до 3");
                    continue;
                }
            } catch (final Exception e) {
                System.out.println("Неверный формат. Нужно ввести число от 1 до 3");
            }
            if (choose == 2) {
                return createPlayerWithAsciiGraphics(playerId, name);
            } else if (choose == 3) {
                return createHumanPlayerWithAsciiGraphics(playerId, name);
            }
            return new ClientPlayerImitation(playerId, name);
        }
    }

    public String getAnswer(final ExtendedServerResponse response) throws IOException, HeroExceptions {
        gui.showPosition(response);
        final Answer answer = player.getAnswer(response.arena);
        return new ClientRequest(answer).jsonAnswer;
    }

    public void sendInformation(final ExtendedServerResponse response) throws IOException {
        gui.showPosition(response);
    }

    public void endGame(final ExtendedServerResponse response) throws IOException {
        gui.endGame(response);
    }

    public String getArmyFirst(final int armySize) throws IOException {
        return player.getStringArmyFirst(armySize);
    }

    public String getArmySecond(final int armySize, final Army army) throws IOException {
        return player.getStringArmySecond(armySize, army);
    }

    public Player getPlayer() {
        return player;
    }

    public int getPlayerId() {
        return player.getId();
    }

    public String getPlayerName() {
        return player.getName();
    }

    public void setPlayerId(final int id) {
        player.setId(id);
        if (gui instanceof AsciiGraphics) {
            final AsciiGraphics temp = (AsciiGraphics) gui;
            temp.setPresenterPlayerId(id);
        }
    }

    public void setPlayerName(final String name) {
        player.setName(name);
    }
}

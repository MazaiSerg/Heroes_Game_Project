package com.neolab.heroesGame.client.ai.server;

import com.neolab.heroesGame.client.ai.enums.ServerType;
import com.neolab.heroesGame.client.ai.server.elo.ServerForCorrectingElo;
import com.neolab.heroesGame.client.ai.version.basic.AbstractServer;
import com.neolab.heroesGame.client.ai.version.third.ArmiesStatisticsCollector;
import com.neolab.heroesGame.client.ai.version.third.Evolver;

public class Server {
    private static final int maxCountGameRoom = 5;
    private static final int queueSize = 10;

    public static void main(final String[] args) throws Exception {
        AbstractServer server;
        if (args.length == 0) {
            server = createServer(ServerType.ELO_MATCH_UPS);
        } else {
            server = createServer(ServerType.valueOf(args[0]));
        }
        server.matching();
    }

    private static AbstractServer createServer(final ServerType serverType) {
        return switch (serverType) {
            case ONE_MATCH_UP_TESTS -> new SelfPlayServer(maxCountGameRoom, queueSize);
            case FEW_BOTS_MATCH_UPS -> new SelfPlayServerForNight(maxCountGameRoom, queueSize);
            case ELO_MATCH_UPS -> new ServerForCorrectingElo(maxCountGameRoom, queueSize);
            case EVOLVER -> new Evolver(maxCountGameRoom, queueSize);
            case ARMIES_STATISTICS_COLLECTOR -> new ArmiesStatisticsCollector(maxCountGameRoom, queueSize);
        };
    }
}

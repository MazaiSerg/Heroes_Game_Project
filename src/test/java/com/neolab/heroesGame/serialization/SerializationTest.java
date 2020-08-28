package com.neolab.heroesGame.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.FactoryArmies;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.*;
import com.neolab.heroesGame.heroes.factory.*;
import com.neolab.heroesGame.server.ActionEffect;
import com.neolab.heroesGame.server.answers.Answer;
import com.neolab.heroesGame.server.dto.ExtendedServerRequest;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void squareCoordinateSerializationDeserializationTest() throws JsonProcessingException {
        final SquareCoordinate sq = new SquareCoordinate(1, 2);
        final String json = mapper.writeValueAsString(sq);
        final SquareCoordinate newSq = mapper.readValue(json, SquareCoordinate.class);

        assertEquals(sq, newSq);
    }

    @Test
    public void archerSerializationDeserializationTest() throws IOException {
        final Hero archer = new ArcherFactory().create();
        final String json = mapper.writeValueAsString(archer);
        final Hero newArcher = mapper.readValue(json, Hero.class);

        assertEquals(archer, newArcher);
        assertTrue(newArcher instanceof Archer);
    }

    @Test
    public void footmanSerializationDeserializationTest() throws IOException {
        final Hero footman = new FootmanFactory().create();
        final String json = mapper.writeValueAsString(footman);
        final Hero newFootman = mapper.readValue(json, Hero.class);

        assertEquals(footman, newFootman);
        assertTrue(newFootman instanceof Footman);
    }

    @Test
    public void healerSerializationDeserializationTest() throws IOException {
        final Hero healer = new HealerFactory().create();
        final String json = mapper.writeValueAsString(healer);
        final Hero newHealer = mapper.readValue(json, Hero.class);

        assertEquals(healer, newHealer);
        assertTrue(newHealer instanceof Healer);
    }

    @Test
    public void magicianSerializationDeserializationTest() throws IOException {
        final Hero magician = new MagicianFactory().create();
        final String json = mapper.writeValueAsString(magician);
        final Hero newMagician = mapper.readValue(json, Hero.class);

        assertEquals(magician, newMagician);
        assertTrue(newMagician instanceof Magician);
    }

    @Test
    public void warlordFootmanSerializationDeserializationTest() throws IOException {
        final Hero warlordFootman = new WarlordFootmanFactory().create();
        final String json = mapper.writeValueAsString(warlordFootman);
        final Hero newWarlordFootman = mapper.readValue(json, Hero.class);

        assertEquals(warlordFootman, newWarlordFootman);
        assertTrue(newWarlordFootman instanceof WarlordFootman);

        final IWarlord warlord = (IWarlord) new WarlordFootmanFactory().create();
        final String json2 = mapper.writeValueAsString(warlord);
        final IWarlord warlord1 = mapper.readValue(json2, IWarlord.class);

        assertEquals(warlord, warlord1);
    }

    @Test
    public void warlordMagicianSerializationDeserializationTest() throws IOException {
        final Hero warlordMagician = new WarlordMagicianFactory().create();
        final String json = mapper.writeValueAsString(warlordMagician);
        final Hero newWarlordMagician = mapper.readValue(json, Hero.class);

        assertEquals(warlordMagician, newWarlordMagician);
        assertTrue(newWarlordMagician instanceof WarlordMagician);

        final IWarlord warlord = (IWarlord) new WarlordMagicianFactory().create();
        final String json2 = mapper.writeValueAsString(warlord);
        final IWarlord warlord1 = mapper.readValue(json2, IWarlord.class);

        assertEquals(warlord, warlord1);
    }

    @Test
    public void warlordVampireSerializationDeserializationTest() throws IOException {
        final Hero warlordVampire = new WarlordVampireFactory().create();
        final String json = mapper.writeValueAsString(warlordVampire);
        final Hero newWarlordVampire = mapper.readValue(json, Hero.class);

        assertEquals(warlordVampire, newWarlordVampire);
        assertTrue(newWarlordVampire instanceof WarlordVampire);

        final IWarlord warlord = (IWarlord) new WarlordVampireFactory().create();
        final String json2 = mapper.writeValueAsString(warlord);
        final IWarlord warlord1 = mapper.readValue(json2, IWarlord.class);

        assertEquals(warlord, warlord1);
    }

    @Test
    public void armySerializationDeserializationTest() throws IOException, HeroExceptions {
        final Army army = FactoryArmies.generateArmies(1, 2).get(1);
        final String json = mapper.writeValueAsString(army);
        final Army newArmy = mapper.readValue(json, Army.class);

        assertEquals(army, newArmy);
    }

    @Test
    public void battleArenaSerializationDeserializationTest() throws IOException, HeroExceptions {
        final BattleArena arena = new BattleArena(FactoryArmies.generateArmies(1, 2));
        final String json = mapper.writeValueAsString(arena);
        final BattleArena newArena = mapper.readValue(json, BattleArena.class);

        assertEquals(arena, newArena);
    }

    @Test
    public void answerSerializationDeserializationTest() throws JsonProcessingException {
        final Answer a = new Answer(new SquareCoordinate(1, 2), HeroActions.ATTACK,
                new SquareCoordinate(2, 2), 1);
        final String json = mapper.writeValueAsString(a);
        final Answer newA = mapper.readValue(json, Answer.class);

        assertEquals(a, newA);
    }

    @Test
    public void actionEffectSerializationDeserializationTest() throws JsonProcessingException {
        final Map<SquareCoordinate, Integer> map = new HashMap<>();
        map.put(new SquareCoordinate(2, 2), 2);
        final ActionEffect ae = new ActionEffect(HeroActions.ATTACK, new SquareCoordinate(1, 2), map, 1);
        final String json = mapper.writeValueAsString(ae);
        System.out.println(json);
        final ActionEffect newAe = mapper.readValue(json, ActionEffect.class);

        assertEquals(ae, newAe);
    }

    @Test
    public void extendedServerRequestTest() throws Exception {
        final BattleArena arena = new BattleArena(FactoryArmies.generateArmies(1, 2));
        final Map<SquareCoordinate, Integer> map = new HashMap<>();
        map.put(new SquareCoordinate(2, 2), 2);
        final ActionEffect ae = new ActionEffect(HeroActions.ATTACK, new SquareCoordinate(1, 2), map, 1);

        final String request = ExtendedServerRequest.getRequestString(GameEvent.GAME_END_WITH_A_TIE, arena, ae);
        final ExtendedServerRequest extendedServerRequest = ExtendedServerRequest.getRequestFromString(request);

        assertEquals(arena, extendedServerRequest.arena);
        assertEquals(ae, extendedServerRequest.effect);
        assertEquals(GameEvent.GAME_END_WITH_A_TIE, extendedServerRequest.event);
    }
}

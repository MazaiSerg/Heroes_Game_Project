package com.neolab.heroesGame.aditional;

import com.neolab.heroesGame.validators.StringArmyValidator;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringArmiesGeneratorTest {

    @Test
    public void armiesGeneratorSizeSixTest() {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(6);
        final Set<String> setArmies = new HashSet<>(armies);
        assertEquals(setArmies.size(), armies.size());
        assertEquals(135, armies.size());
        for (String army : armies) {
            assertTrue(StringArmyValidator.validateArmyString(army, 6));
        }
    }

    @Test
    public void armiesGeneratorSizeFiveTest() {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(5);
        final Set<String> setArmies = new HashSet<>(armies);

        assertEquals(setArmies.size(), armies.size());

        for (String army : armies) {
            assertTrue(StringArmyValidator.validateArmyString(army, 5));
        }
    }

    @Test
    public void armiesGeneratorSizeFourTest() {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(4);
        final Set<String> setArmies = new HashSet<>(armies);

        assertEquals(setArmies.size(), armies.size());

        for (String army : armies) {
            assertTrue(StringArmyValidator.validateArmyString(army, 4));
        }
    }

    @Test
    public void armiesGeneratorSizeThreeTest() {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(3);
        final Set<String> setArmies = new HashSet<>(armies);

        assertEquals(setArmies.size(), armies.size());

        for (String army : armies) {
            assertTrue(StringArmyValidator.validateArmyString(army, 3));
        }
    }

    @Test
    public void armiesGeneratorSizeTwoTest() {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(2);
        final Set<String> setArmies = new HashSet<>(armies);

        assertEquals(setArmies.size(), armies.size());

        for (String army : armies) {
            assertTrue(StringArmyValidator.validateArmyString(army, 2));
        }
    }
}

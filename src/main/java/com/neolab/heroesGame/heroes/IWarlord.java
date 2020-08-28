package com.neolab.heroesGame.heroes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WarlordFootman.class, name = "WarlordFootman"),
        @JsonSubTypes.Type(value = WarlordMagician.class, name = "WarlordMagician"),
        @JsonSubTypes.Type(value = WarlordVampire.class, name = "WarlordVampire")
})
public interface IWarlord {
    float getImproveCoefficient();

    int getUnitId();
}

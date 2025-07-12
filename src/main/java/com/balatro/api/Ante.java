package com.balatro.api;

import com.balatro.enums.*;
import com.balatro.structs.EditionItem;
import com.balatro.structs.JokerData;
import com.balatro.structs.Pack;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Ante extends CommonQueries {

    @JsonIgnore
    default String toJson() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    int getAnte();

    Shop getShopQueue();

    Set<Tag> getTags();

    Voucher getVoucher();

    Boss getBoss();

    List<Pack> getPacks();

    @JsonIgnore
    Map<String, JokerData> getLegendaryJokers();

    @JsonIgnore
    int getBufferedJokerCount();

    @JsonIgnore
    Set<EditionItem> getJokers();

    @JsonIgnore
    Set<Joker> getRareJokers();

    @JsonIgnore
    Set<Joker> getUncommonJokers();

    @JsonIgnore
    int getNegativeJokerCount();

    @JsonIgnore
    Set<Tarot> getTarots();

    @JsonIgnore
    Set<Planet> getPlanets();

    @JsonIgnore
    Set<Spectral> getSpectrals();

    @JsonIgnore
    int getStandardPackCount();

    @JsonIgnore
    int getJokerPackCount();

    @JsonIgnore
    int getSpectralPackCount();

    @JsonIgnore
    int getTarotPackCount();

    @JsonIgnore
    int getPlanetPackCount();
}

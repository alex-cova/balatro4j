package com.balatro.api;

import com.balatro.api.filter.InBuffonPackFilter;
import com.balatro.enums.Edition;
import com.balatro.enums.JokerType;
import com.balatro.jackson.ItemSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ItemSerializer.class)
public interface Joker extends Item {

    default Filter inBuffonPack() {
        return new InBuffonPackFilter(this);
    }

    default Filter inBuffonPack(int ante) {
        return new InBuffonPackFilter(ante, this, Edition.NoEdition);
    }

    default Filter inBuffonPack(int ante, Edition edition) {
        return new InBuffonPackFilter(ante, this, edition);
    }

    JokerType getType();

    default boolean isRare() {
        return getType() == JokerType.RARE;
    }

    default boolean isCommon() {
        return getType() == JokerType.COMMON;
    }

    default boolean isUncommon() {
        return getType() == JokerType.UNCOMMON;
    }

    default boolean isLegendary() {
        return getType() == JokerType.LEGENDARY;
    }

    @Override
    default Filter auto(int ante, Edition edition) {
        return inPack(ante, edition).or(inShop(ante, edition));
    }
}

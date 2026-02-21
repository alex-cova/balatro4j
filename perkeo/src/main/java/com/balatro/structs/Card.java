package com.balatro.structs;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.enums.*;
import org.jetbrains.annotations.Nullable;

public record Card(com.balatro.enums.Card base, @Nullable Enhancement enhancement, Edition edition,
                   Seal seal) implements Item {

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public int getYIndex() {
        return -1;
    }

    @Override
    public int ordinal() {
        return 0;
    }

    public Rank getRank() {
        return base.getRank();
    }

    public Suit getSuit() {
        return base.getSuit();
    }

    @Override
    public Filter auto(int ante, Edition edition) {
        return null;
    }
}
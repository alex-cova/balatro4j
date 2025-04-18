package com.balatro.structs;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ShopQueue extends ArrayList<EditionItem> {

    public ShopQueue(@NotNull Collection<? extends EditionItem> c) {
        super(c);
    }

    public ShopQueue() {
        super(20);
    }

}

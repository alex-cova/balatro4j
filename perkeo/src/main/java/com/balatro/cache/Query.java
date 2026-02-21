package com.balatro.cache;

import com.balatro.enums.Edition;
import org.jetbrains.annotations.NotNull;

public class Query {
    private String item;
    private Edition edition;

    public Query() {
    }

    public Query(String item) {
        this.item = item;
        this.edition = Edition.NoEdition;
    }

    public Query(String item, @NotNull Edition edition) {
        this.item = item;
        this.edition = edition;
    }

    public String getItem() {
        return item;
    }

    public Query setItem(String item) {
        this.item = item;
        return this;
    }

    public Edition getEdition() {
        return edition;
    }

    public Query setEdition(Edition edition) {
        this.edition = edition;
        return this;
    }
}

package com.balatro.structs;

import com.balatro.api.Item;
import com.balatro.enums.Edition;
import com.balatro.enums.PackKind;
import com.balatro.enums.PackType;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pack {

    private final PackType type;
    private Set<EditionItem> options;
    /** Name-indexed lookup for O(1) containsOption queries. */
    private Map<String, EditionItem> optionsByName;

    public Pack(@NotNull PackType type) {
        this.type = type;
    }

    public Set<EditionItem> getOptions() {
        return options;
    }

    public void setOptions(Set<EditionItem> options) {
        this.options = options;
        this.optionsByName = new HashMap<>(options.size() + 1, 1.0f);
        for (EditionItem item : options) {
            optionsByName.put(item.item().getName(), item);
        }
    }

    public PackType getType() {
        return type;
    }

    public PackKind getKind() {
        return type.getKind();
    }

    public boolean containsOption(@NotNull Item item) {
        return containsOption(item.getName());
    }

    public boolean containsOption(String name) {
        return optionsByName.containsKey(name);
    }

    public boolean containsOption(String name, Edition edition) {
        var item = optionsByName.get(name);
        if (item == null) return false;
        return edition == Edition.NoEdition || item.edition() == edition;
    }

    public int getSize() {
        return type.getSize();
    }
}
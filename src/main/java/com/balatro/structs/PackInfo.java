package com.balatro.structs;

import com.balatro.api.Item;
import com.balatro.enums.Edition;
import com.balatro.enums.PackKind;
import com.balatro.enums.PackType;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackInfo {

    private final PackType type;
    private Set<EditionItem> options;

    public PackInfo(@NotNull PackType type) {
        this.type = type;
    }

    public Set<EditionItem> getOptions() {
        return options;
    }

    public void setOptions(Set<EditionItem> options) {
        this.options = options;
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
        for (EditionItem option : options) {
            if (option.item().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsOption(String name, Edition edition) {
        for (EditionItem option : options) {
            if (option.item().equals(name)) {
                if (edition == Edition.NoEdition) {
                    return true;
                }
                return option.edition() == edition;
            }
        }

        return false;
    }

    public int getSize() {
        return type.getSize();
    }
}
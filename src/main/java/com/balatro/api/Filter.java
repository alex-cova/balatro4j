package com.balatro.api;


import com.balatro.CompoundFilter;
import com.balatro.api.filter.AndFilter;
import com.balatro.api.filter.OrFilter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Filter {

    boolean filter(Run run);

    void configure(Balatro balatro);

    default Filter or(Filter filter) {
        return new OrFilter(this, filter);
    }

    default Filter and(Filter filter) {
        return new AndFilter(this, filter);
    }

    static @NotNull Filter compound(@NotNull Collection<Filter> filters) {
        return new CompoundFilter(filters);
    }

    @Contract(pure = true)
    static @NotNull Filter findAll() {
        return new Filter() {
            @Override
            public boolean filter(Run run) {
                return true;
            }

            @Override
            public void configure(Balatro balatro) {
                balatro.enableAll();
            }
        };
    }
}

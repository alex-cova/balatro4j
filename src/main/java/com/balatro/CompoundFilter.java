package com.balatro;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.api.Run;

import java.util.Collection;

public class CompoundFilter implements Filter {

    private final Collection<Filter> must;

    public CompoundFilter(Collection<Filter> must) {
        this.must = must;
    }

    @Override
    public boolean filter(Run run) {
        return must.stream().allMatch(filter -> filter.filter(run));
    }

    @Override
    public void configure(Balatro balatro) {
        for (Filter filter : must) {
            filter.configure(balatro);
        }
    }
}

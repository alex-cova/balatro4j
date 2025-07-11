package com.balatro.api.filter;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.api.Run;

public class OrFilter implements Filter {

    private final Filter a;
    private final Filter b;

    public OrFilter(Filter a, Filter b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean filter(Run run) {
        return a.filter(run) || b.filter(run);
    }

    @Override
    public void configure(Balatro balatro) {
        a.configure(balatro);
        b.configure(balatro);
    }
}

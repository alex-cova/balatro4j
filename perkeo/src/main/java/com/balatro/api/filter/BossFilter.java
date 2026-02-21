package com.balatro.api.filter;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.api.Run;
import com.balatro.enums.Boss;

public record BossFilter(Boss boss, boolean negated) implements Filter {
    @Override
    public boolean filter(Run run) {
        if (negated) {
            return !run.hasBoss(boss);
        }
        return run.hasBoss(boss);
    }

    @Override
    public void configure(Balatro balatro) {
        balatro.enableBoss();
    }
}

package com.balatro.api;

import com.balatro.impl.AnteScorerImpl;
import org.jetbrains.annotations.NotNull;

public interface AnteScorer {

    static AnteScorer getDefault() {
        return AnteScorerImpl.INSTANCE;
    }

    float calculate(@NotNull Run run);

    default float calculate(@NotNull Ante ante) {
        return AnteScorerImpl.INSTANCE.calculate(ante);
    }
}

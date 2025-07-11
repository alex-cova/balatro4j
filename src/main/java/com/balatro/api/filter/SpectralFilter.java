package com.balatro.api.filter;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.api.Run;
import com.balatro.enums.Edition;

public record SpectralFilter(int ante, Item item, Edition edition) implements Filter {

    public SpectralFilter(Item item) {
        this(-1, item, Edition.NoEdition);
    }

    @Override
    public boolean filter(Run run) {
        if (ante == -1) {
            return run.hasInSpectral(item);
        }
        return run.hasInSpectral(ante, item, edition);
    }

    @Override
    public void configure(Balatro balatro) {
        balatro.enableSpectralPack();
    }
}

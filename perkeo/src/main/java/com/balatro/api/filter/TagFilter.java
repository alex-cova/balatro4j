package com.balatro.api.filter;

import com.balatro.api.Balatro;
import com.balatro.api.Filter;
import com.balatro.api.Run;
import com.balatro.enums.Tag;

public record TagFilter(Tag tag, int ante) implements Filter {

    public TagFilter(Tag tag) {
        this(tag, -1);
    }

    @Override
    public boolean filter(Run run) {
        if (ante == -1) {
            return run.hasTag(tag);
        }

        return run.hasTag(ante, tag);
    }

    @Override
    public void configure(Balatro balatro) {
        balatro.enableTags();
    }
}

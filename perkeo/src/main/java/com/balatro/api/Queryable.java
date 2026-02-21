package com.balatro.api;

import com.balatro.enums.*;
import org.jetbrains.annotations.NotNull;

public interface Queryable extends CommonQueries {

    boolean hasInPack(int ante, Item item, Edition edition);

    boolean hasInShop(int ante, Item item, Edition edition);

    long countLegendary(int ante);

    boolean hasInShop(int ante, Item item, int index, Edition edition);

    boolean hasLegendary(int ante, LegendaryJoker joker, Edition edition);

    boolean hasInSpectral(int ante, @NotNull Item item, Edition edition);

    boolean hasInBuffonPack(int ante, @NotNull Item item, Edition edition);

    boolean hasPack(int ante, PackType packType);

    boolean hasVoucher(int ante, Voucher voucher);

    boolean hasBoss(int ante, Boss boss);

    boolean hasTag(int ante, Tag tag);
}

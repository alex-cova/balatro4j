package com.balatro.impl;

import com.balatro.api.Ante;
import com.balatro.api.Item;
import com.balatro.api.Joker;
import com.balatro.api.Shop;
import com.balatro.enums.*;
import com.balatro.structs.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
final class AnteImpl implements Ante {

    private final int ante;
    private final ShopQueue shopQueue;
    private final Set<Tag> tags;
    private Voucher voucher;
    private Boss boss;
    private final List<Pack> packs;
    //Cache
    @JsonIgnore
    private Map<String, JokerData> legendaryJokers;

    AnteImpl(int ante, @NotNull Configuration configuration) {
        this.ante = ante;

        if (configuration.analyzeTags) {
            this.tags = EnumSet.noneOf(Tag.class);
        } else {
            this.tags = Collections.emptySet();
        }

        if (configuration.analyzeShopQueue) {
            this.shopQueue = new ShopQueue();
        } else {
            this.shopQueue = ShopQueue.EMPTY;
        }

        if (configuration.isAnalyzePacks()) {
            this.packs = new ArrayList<>(10);
        } else {
            this.packs = Collections.emptyList();
        }
    }

    @Override
    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    @Override
    public boolean hasBoss(Boss boss) {
        return this.boss == boss;
    }

    @Override
    public boolean hasInShop(@NotNull Item item, Edition edition) {
        return shopQueue.contains(item, edition);
    }

    @Override
    public boolean hasInShop(@NotNull Item item, int index, Edition edition) {
        if (index > shopQueue.size()) {
            return false;
        }

        for (int i = 0; i < index; i++) {
            if (shopQueue.get(i).equals(item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public long countLegendary() {
        hasLegendary(LegendaryJoker.Perkeo, Edition.NoEdition);
        return legendaryJokers.size();
    }

    void addTag(Tag tag) {
        tags.add(tag);
    }

    void addToQueue(@NotNull ShopItem value) {
        shopQueue.add(value);
    }

    void setBoss(Boss boss) {
        this.boss = boss;
    }

    void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    void addPack(@NotNull Pack pack, Set<EditionItem> options) {
        pack.setOptions(options);
        packs.add(pack);
    }

    @Contract(" -> new")
    @Override
    public @NotNull HashMap<String, JokerData> getLegendaryJokers() {
        hasLegendary(LegendaryJoker.Perkeo, Edition.NoEdition);//pre-compute
        return new HashMap<>(legendaryJokers);
    }

    @Override
    public int getBufferedJokerCount() {
        int count = 0;
        for (EditionItem ei : shopQueue) {
            if (ei.hasSticker() && ei.item() instanceof Joker) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean hasLegendary(LegendaryJoker joker, Edition edition) {
        if (legendaryJokers != null) {
            if (legendaryJokers.containsKey(joker.getName())) {
                if (edition == Edition.NoEdition) {
                    return true;
                }
                return legendaryJokers.get(joker.getName()).getEdition() == edition;
            }
            return false;
        }

        legendaryJokers = new HashMap<>();

        for (Pack pack : packs) {
            if (pack.getKind() == PackKind.Buffoon) {
                continue;
            }

            if (pack.getKind() == PackKind.Standard) {
                continue;
            }

            if (pack.getKind() == PackKind.Celestial) {
                continue;
            }

            for (EditionItem option : pack.getOptions()) {
                if (option.isLegendary()) {
                    legendaryJokers.put(option.item().getName(), option.jokerData());
                }
            }
        }

        if (legendaryJokers.containsKey(joker.getName())) {
            if (edition == Edition.NoEdition) {
                return true;
            }
            return legendaryJokers.get(joker.getName()).getEdition() == edition;
        }
        return false;
    }

    @Override
    public boolean hasInPack(@NotNull Item item, Edition edition) {
        if (item instanceof LegendaryJoker joker) {
            return hasLegendary(joker, edition);
        }

        if (item instanceof EditionItem editionItem) {
            if (editionItem.isLegendary()) {
                return hasLegendary((LegendaryJoker) editionItem.item(), edition);
            }
        }

        for (Pack pack : packs) {
            if (pack.containsOption(item.getName(), edition)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPack(PackType packType) {
        for (Pack pack : packs) {
            if (pack.getType() == packType) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasInSpectral(@NotNull Item item, Edition edition) {
        if (item instanceof LegendaryJoker joker) {
            return hasLegendary(joker, edition);
        }

        if (item instanceof EditionItem editionItem) {
            if (editionItem.isLegendary()) {
                return hasLegendary((LegendaryJoker) editionItem.item(), edition);
            }
        }

        for (Pack pack : packs) {
            if (pack.getKind() != PackKind.Spectral) {
                continue;
            }

            if (pack.containsOption(item.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasVoucher(Voucher voucher) {
        return this.voucher == voucher;
    }

    @Override
    public int countInPack(@NotNull Item item) {
        if (item instanceof LegendaryJoker) {
            item = Specials.THE_SOUL;
        }

        int count = 0;
        for (Pack pack : packs) {
            if (pack.containsOption(item.getName())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean hasInBuffonPack(@NotNull Item item, Edition edition) {
        for (Pack pack : packs) {
            if (pack.getKind() != PackKind.Buffoon) {
                continue;
            }

            if (pack.containsOption(item.getName(), edition)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getAnte() {
        return ante;
    }

    @JsonIgnore
    @Override
    public Shop getShopQueue() {
        return shopQueue.copy();
    }

    @Override
    public Set<Tag> getTags() {
        return tags.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(tags);
    }

    @Override
    public Voucher getVoucher() {
        return voucher;
    }

    @Override
    public Boss getBoss() {
        return boss;
    }

    @Override
    public List<Pack> getPacks() {
        return new ArrayList<>(packs);
    }

    @Override
    public List<Pack> getPacks(PackKind kind) {
        var result = new ArrayList<Pack>();
        for (Pack pack : packs) {
            if (pack.getKind() == kind) {
                result.add(pack);
            }
        }
        return result;
    }

    @Override
    public Set<EditionItem> getJokers() {
        var result = new HashSet<EditionItem>();
        for (EditionItem ei : shopQueue) {
            if (ei.isJoker()) {
                result.add(ei);
            }
        }
        for (Pack pack : packs) {
            if (pack.getKind() == PackKind.Buffoon) {
                result.addAll(pack.getOptions());
            }
        }
        return result;
    }

    @Override
    public Set<Joker> getRareJokers() {
        var result = new HashSet<Joker>();
        for (EditionItem ei : shopQueue) {
            if (ei.item() instanceof Joker joker && joker.isRare()) {
                result.add(joker);
            }
        }
        return result;
    }

    @Override
    public Set<Joker> getUncommonJokers() {
        var result = new HashSet<Joker>();
        for (EditionItem ei : shopQueue) {
            if (ei.item() instanceof Joker joker && joker.isUncommon()) {
                result.add(joker);
            }
        }
        return result;
    }

    @Override
    public int getNegativeJokerCount() {
        int count = 0;
        for (EditionItem ei : shopQueue) {
            if (ei.hasEdition(Edition.Negative) && ei.item() instanceof Joker) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Set<Tarot> getTarots() {
        var result = new HashSet<Tarot>();
        for (EditionItem ei : shopQueue) {
            if (ei.item() instanceof Tarot tarot) {
                result.add(tarot);
            }
        }
        return result;
    }

    @Override
    public Set<Planet> getPlanets() {
        var result = new HashSet<Planet>();
        for (EditionItem ei : shopQueue) {
            if (ei.item() instanceof Planet planet) {
                result.add(planet);
            }
        }
        return result;
    }

    @Override
    public Set<Spectral> getSpectrals() {
        var result = new HashSet<Spectral>();
        for (Pack pack : packs) {
            if (pack.getKind() == PackKind.Spectral) {
                for (EditionItem option : pack.getOptions()) {
                    if (option.item() instanceof Spectral spectral) {
                        result.add(spectral);
                    }
                }
            }
        }
        return result;
    }

    private int countPacksByKind(PackKind kind) {
        int count = 0;
        for (Pack pack : packs) {
            if (pack.getKind() == kind) count++;
        }
        return count;
    }

    @Override
    public int getStandardPackCount() {
        return countPacksByKind(PackKind.Standard);
    }

    @Override
    public int getJokerPackCount() {
        return countPacksByKind(PackKind.Buffoon);
    }

    @Override
    public int getSpectralPackCount() {
        return countPacksByKind(PackKind.Spectral);
    }

    @Override
    public int getTarotPackCount() {
        return countPacksByKind(PackKind.Arcana);
    }

    @Override
    public int getPlanetPackCount() {
        return countPacksByKind(PackKind.Celestial);
    }
}

package com.balatro;

import com.balatro.api.Ante;
import com.balatro.api.Item;
import com.balatro.api.Joker;
import com.balatro.enums.*;
import com.balatro.structs.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
final class AnteImpl implements Ante {

    private final int ante;
    private final ShopQueue shopQueue;
    @JsonIgnore
    private final Map<String, Edition> shop;
    private final Set<Tag> tags;
    private Voucher voucher;
    private Boss boss;
    private final List<PackInfo> packInfos;
    //Cache
    @JsonIgnore
    private Map<String, JokerData> legendaryJokers;

    AnteImpl(int ante) {
        this.ante = ante;
        this.tags = new HashSet<>(2);
        this.shopQueue = new ShopQueue();
        this.shop = new HashMap<>(20);
        this.packInfos = new ArrayList<>(10);
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
        var i = shop.get(item.getName());

        if (i != null) {
            return i == edition;
        }

        return false;
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
        shop.put(value.getItem().getName(), value.getEdition());
        shopQueue.add(value, value.getEdition());
    }

    void setBoss(Boss boss) {
        this.boss = boss;
    }

    void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    void addPack(@NotNull PackInfo packInfo, Set<EditionItem> options) {
        packInfo.setOptions(options);
        packInfos.add(packInfo);
    }

    @Contract(" -> new")
    @Override
    public @NotNull HashMap<String, JokerData> getLegendaryJokers() {
        hasLegendary(LegendaryJoker.Perkeo, Edition.NoEdition);//pre-compute
        return new HashMap<>(legendaryJokers);
    }

    @Override
    public int getBufferedJokerCount() {
        return (int) shopQueue.stream()
                .filter(EditionItem::hasSticker)
                .filter(a -> a.item() instanceof Joker)
                .count();
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

        for (PackInfo packInfo : packInfos) {
            if (packInfo.getKind() == PackKind.Buffoon) {
                continue;
            }

            if (packInfo.getKind() == PackKind.Standard) {
                continue;
            }

            if (packInfo.getKind() == PackKind.Celestial) {
                continue;
            }

            for (EditionItem option : packInfo.getOptions()) {
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

        for (PackInfo packInfo : packInfos) {
            if (packInfo.containsOption(item.getName(), edition)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPack(PackType packType) {
        for (PackInfo packInfo : packInfos) {
            if (packInfo.getType() == packType) {
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

        for (PackInfo packInfo : packInfos) {
            if (packInfo.getKind() != PackKind.Spectral) {
                continue;
            }

            if (packInfo.containsOption(item.getName())) {
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
        for (PackInfo packInfo : packInfos) {
            if (packInfo.containsOption(item.getName())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean hasInBuffonPack(@NotNull Item item, Edition edition) {
        for (PackInfo packInfo : packInfos) {
            if (packInfo.getKind() != PackKind.Buffoon) {
                continue;
            }

            if (packInfo.containsOption(item.getName(), edition)) {
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
    public Set<String> getShop() {
        return shop.keySet();
    }

    @Override
    public ShopQueue getShopQueue() {
        return shopQueue;
    }

    @Override
    public Set<Tag> getTags() {
        return new HashSet<>(tags);
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
    public List<PackInfo> getPacks() {
        return new ArrayList<>(packInfos);
    }

    @Override
    public Set<EditionItem> getJokers() {
        var a = shopQueue.stream()
                .filter(EditionItem::isJoker)
                .collect(Collectors.toSet());

        var b = packInfos.stream()
                .filter(p -> p.getKind() == PackKind.Buffoon)
                .flatMap(pack -> pack.getOptions().stream())
                .collect(Collectors.toSet());

        a.addAll(b);

        return a;
    }

    @Override
    public Set<Joker> getRareJokers() {
        return shopQueue.stream()
                .map(EditionItem::item)
                .filter(a -> a instanceof Joker)
                .map(a -> (Joker) a)
                .filter(Joker::isRare)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Joker> getUncommonJokers() {
        return shopQueue.stream()
                .map(EditionItem::item)
                .filter(a -> a instanceof Joker)
                .map(a -> (Joker) a)
                .filter(Joker::isUncommon)
                .collect(Collectors.toSet());
    }

    @Override
    public int getNegativeJokerCount() {
        return (int) shopQueue.stream()
                .filter(a -> a.hasEdition(Edition.Negative))
                .map(EditionItem::item)
                .filter(a -> a instanceof Joker)
                .count();
    }

    @Override
    public Set<Tarot> getTarots() {
        return shopQueue.stream()
                .map(EditionItem::item)
                .filter(a -> a instanceof Tarot)
                .map(a -> (Tarot) a)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Planet> getPlanets() {
        return shopQueue.stream()
                .map(EditionItem::item)
                .filter(a -> a instanceof Planet)
                .map(a -> (Planet) a)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Spectral> getSpectrals() {
        return packInfos.stream()
                .filter(a -> a.getKind() == PackKind.Spectral)
                .flatMap(a -> a.getOptions().stream()
                        .map(EditionItem::item))
                .filter(a -> a instanceof Spectral)
                .map(a -> (Spectral) a)
                .collect(Collectors.toSet());
    }

    @Override
    public int getStandardPackCount() {
        return (int) packInfos.stream()
                .filter(a -> a.getKind() == PackKind.Standard)
                .count();
    }

    @Override
    public int getJokerPackCount() {
        return (int) packInfos.stream()
                .filter(a -> a.getKind() == PackKind.Buffoon)
                .count();
    }

    @Override
    public int getSpectralPackCount() {
        return (int) packInfos.stream()
                .filter(a -> a.getKind() == PackKind.Spectral)
                .count();
    }

    @Override
    public int getTarotPackCount() {
        return (int) packInfos.stream()
                .filter(a -> a.getKind() == PackKind.Arcana)
                .count();
    }

    @Override
    public int getPlanetPackCount() {
        return (int) packInfos.stream()
                .filter(a -> a.getKind() == PackKind.Celestial)
                .count();
    }
}

package com.balatro.api;


import com.balatro.AnteScorerImpl;
import com.balatro.enums.Edition;
import com.balatro.enums.Tag;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Run extends Queryable, Iterable<Ante> {

    ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    String seed();

    String toJson();

    Ante getAnte(int ante);

    List<Ante> antes();

    @JsonIgnore
    default Ante getFirstAnte() {
        return getAnte(1);
    }

    @JsonIgnore
    default Ante getSecondAnte() {
        return getAnte(2);
    }

    @JsonIgnore
    default Ante getThirdAnte() {
        return getAnte(3);
    }

    @JsonIgnore
    default int getBufferedJokerCount() {
        return antes()
                .stream()
                .mapToInt(Ante::getBufferedJokerCount)
                .sum();
    }

    @JsonIgnore
    default float getScore() {
        return new AnteScorerImpl().calculate(this);
    }

    @JsonIgnore
    default Set<String> getJokers() {
        return antes().stream()
                .flatMap(a -> a.getJokers().stream().map(Item::getName))
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    default Set<String> getRareJokers() {
        return antes().stream()
                .flatMap(a -> a.getRareJokers().stream().map(Item::getName))
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    default Set<String> getUncommonJokers() {
        return antes().stream()
                .flatMap(a -> a.getUncommonJokers().stream().map(Item::getName))
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    default int getNegativeJokerCount() {
        return antes().stream()
                .mapToInt(Ante::getNegativeJokerCount)
                .sum();
    }

    @JsonIgnore
    default Set<String> getTarots() {
        return antes().stream()
                .flatMap(a -> a.getTarots().stream().map(Item::getName))
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    default Set<String> getTags() {
        return antes().stream()
                .flatMap(a -> a.getTags().stream().map(Tag::getName))
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    default Set<String> getPlanets() {
        return antes().stream()
                .flatMap(a -> a.getPlanets().stream().map(Item::getName))
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    default Set<String> getVouchers() {
        return antes().stream()
                .map(a -> a.getVoucher().getName())
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    default Set<String> getBosses() {
        return antes().stream()
                .map(a -> a.getBoss().getName())
                .collect(Collectors.toSet());
    }


    @JsonIgnore
    default Set<String> getLegendaryJokers() {
        return antes().stream()
                .flatMap(a -> a.getLegendaryJokers().keySet().stream())
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    default int getStandardPackCount() {
        return antes().stream()
                .mapToInt(Ante::getStandardPackCount)
                .sum();
    }

    @JsonIgnore
    default int getJokerPackCount() {
        return antes().stream()
                .mapToInt(Ante::getJokerPackCount)
                .sum();
    }

    @JsonIgnore
    default int getSpectralPackCount() {
        return antes().stream()
                .mapToInt(Ante::getSpectralPackCount)
                .sum();
    }

    @JsonIgnore
    default int getTarotPackCount() {
        return antes().stream()
                .mapToInt(Ante::getTarotPackCount)
                .sum();
    }

    @JsonIgnore
    default int getPlanetPackCount() {
        return antes().stream()
                .mapToInt(Ante::getPlanetPackCount)
                .sum();
    }

    @JsonIgnore
    default boolean hasJoker(Joker joker) {
        return hasInBuffonPack(joker) || hasInShop(joker);
    }

    @JsonIgnore
    default boolean hasJoker(Joker joker, int maxShopIndex) {
        return hasInBuffonPack(joker) || hasInShop(joker, maxShopIndex, Edition.NoEdition);
    }

    @Override
    @NotNull
    default Iterator<Ante> iterator() {
        return antes().iterator();
    }
}

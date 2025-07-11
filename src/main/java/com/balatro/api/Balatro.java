package com.balatro.api;

import com.balatro.BalatroImpl;
import com.balatro.SeedFinderImpl;
import com.balatro.enums.Deck;
import com.balatro.enums.PackKind;
import com.balatro.enums.Stake;
import com.balatro.enums.Version;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface Balatro {

    Set<PackKind> defaultPacks = Set.of(PackKind.Arcana, PackKind.Buffoon, PackKind.Spectral);
    List<Integer> firstAnte = List.of(15);

    @Contract("_,_ -> new")
    static @NotNull Balatro builder(String seed, int maxAnte) {
        return builder(seed.getBytes(), maxAnte);
    }

    static @NotNull Balatro builder(byte[] seed, int maxAnte) {
        List<Integer> cardsPerAnte = firstAnte;

        if (maxAnte > 1) {
            cardsPerAnte = new ArrayList<>();
            for (int i = 0; i < maxAnte; i++) {
                if (i == 0) {
                    cardsPerAnte.add(15);
                    continue;
                }
                cardsPerAnte.add(50);
            }
        }

        return new BalatroImpl(seed, maxAnte, cardsPerAnte, Deck.RED_DECK, Stake.White_Stake, Version.v_101f, defaultPacks,
                false, false, true);
    }

    @Contract(" -> new")
    static @NotNull SeedFinder search() {
        return new SeedFinderImpl();
    }

    @Contract("_, _ -> new")
    static @NotNull SeedFinder search(int parallelism, int seedsPerThread) {
        return new SeedFinderImpl(parallelism, seedsPerThread);
    }

    Run analyze();

    Run analyzeAll();

    Balatro disableAll();

    Balatro enableBoss();

    Balatro enableAll();

    Balatro maxAnte(int ante);

    Balatro disableShopQueue();

    Balatro disablePack(PackKind packKind);

    Balatro version(Version version);

    Balatro deck(Deck deck);

    Balatro stake(Stake stake);

    Balatro freshProfile(boolean freshProfile);

    Balatro freshRun(boolean freshRun);

    Balatro showman(boolean showman);

    Balatro enableShop();

    Balatro enableSpectralPack();

    Balatro enableVouchers();

    Balatro enableJokerPack();

    Balatro enableCelestialPack();

    Balatro enableArcanaPack();

    Balatro enableTags();

    void printConfigurations();
}

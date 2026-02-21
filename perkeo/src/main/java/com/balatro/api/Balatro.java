package com.balatro.api;

import com.balatro.Functions;
import com.balatro.enums.Deck;
import com.balatro.enums.PackKind;
import com.balatro.enums.Stake;
import com.balatro.impl.BalatroImpl;
import com.balatro.impl.SeedFinderImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public interface Balatro {

    Set<PackKind> defaultPacks = Set.of(PackKind.Arcana, PackKind.Buffoon, PackKind.Spectral);
    int[] firstAnte = {15};

    @Contract("_ -> new")
    static @NotNull Balatro builder(String seed) {
        return builder(seed, 8);
    }

    @Contract("_,_ -> new")
    static @NotNull Balatro builder(@NotNull String seed, int maxAnte) {
        return builder(seed.getBytes(), maxAnte);
    }

    static @NotNull Balatro random(int maxAnte) {
        return builder(BalatroImpl.generateRandomSeed(), maxAnte);
    }

    static @NotNull Balatro builder(byte[] seed, int maxAnte) {
        int[] cardsPerAnte;

        if (maxAnte <= 1) {
            cardsPerAnte = firstAnte;
        } else {
            cardsPerAnte = new int[maxAnte];
            cardsPerAnte[0] = 15;
            Arrays.fill(cardsPerAnte, 1, maxAnte, 50);
        }

        return new BalatroImpl(seed, maxAnte, cardsPerAnte, Deck.RED_DECK, Stake.White_Stake, defaultPacks,
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

    static @NotNull SeedFinder search(int seedsPerThread) {
        return new SeedFinderImpl(Runtime.getRuntime().availableProcessors(), seedsPerThread);
    }

    Functions functions();

    Run analyze();

    Run analyzeAll();

    Balatro disableAll();

    Balatro enableBoss();

    Balatro enableAll();

    Balatro maxAnte(int ante);

    Balatro disableShopQueue();

    Balatro disablePack(PackKind packKind);

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

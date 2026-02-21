package com.balatro;

public record ShopInstance(double jokerRate, double tarotRate, double planetRate, double playingCardRate,
                           double spectralRate) {

    public double getTotalRate() {
        return jokerRate + tarotRate + planetRate + playingCardRate + spectralRate;
    }

}
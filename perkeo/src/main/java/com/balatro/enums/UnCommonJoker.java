package com.balatro.enums;

import com.balatro.api.Joker;
import com.balatro.api.Stored;

public enum UnCommonJoker implements Joker, Stored {
    Joker_Stencil("Joker Stencil"),
    Four_Fingers("Four Fingers"),
    Mime("Mime"),
    Ceremonial_Dagger("Ceremonial Dagger"),
    Marble_Joker("Marble Joker"),
    Loyalty_Card("Loyalty Card"),
    Dusk("Dusk"),
    Fibonacci("Fibonacci"),
    Steel_Joker("Steel Joker"),
    Hack("Hack"),
    Pareidolia("Pareidolia"),
    Space_Joker("Space Joker"),
    Burglar("Burglar"),
    Blackboard("Blackboard"),
    Sixth_Sense("Sixth Sense"),
    Constellation("Constellation"),
    Hiker("Hiker"),
    Card_Sharp("Card Sharp"),
    Madness("Madness"),
    Seance("Seance"),
    Vampire("Vampire"),
    Shortcut("Shortcut"),
    Hologram("Hologram"),
    Cloud_9("Cloud 9"),
    Rocket("Rocket"),
    Midas_Mask("Midas Mask"),
    Luchador("Luchador"),
    Gift_Card("Gift Card"),
    Turtle_Bean("Turtle Bean"),
    Erosion("Erosion"),
    To_the_Moon("To the Moon"),
    Stone_Joker("Stone Joker"),
    Lucky_Cat("Lucky Cat"),
    Bull("Bull"),
    Diet_Cola("Diet Cola"),
    Trading_Card("Trading Card"),
    Flash_Card("Flash Card"),
    Spare_Trousers("Spare Trousers"),
    Ramen("Ramen"),
    Seltzer("Seltzer"),
    Castle("Castle"),
    Mr_Bones("Mr. Bones"),
    Acrobat("Acrobat"),
    Sock_and_Buskin("Sock and Buskin"),
    Troubadour("Troubadour"),
    Certificate("Certificate"),
    Smeared_Joker("Smeared Joker"),
    Throwback("Throwback"),
    Rough_Gem("Rough Gem"),
    Bloodstone("Bloodstone"),
    Arrowhead("Arrowhead"),
    Onyx_Agate("Onyx Agate"),
    Glass_Joker("Glass Joker"),
    Showman("Showman"),
    Flower_Pot("Flower Pot"),
    Merry_Andy("Merry Andy"),
    Oops_All_6s("Oops! All 6s"),
    The_Idol("The Idol"),
    Seeing_Double("Seeing Double"),
    Matador("Matador"),
    Satellite("Satellite"),
    Cartomancer("Cartomancer"),
    Astronomer("Astronomer"),
    Bootstrap("Bootstrap");

    private final String name;

    UnCommonJoker(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 1;
    }

    @Override
    public JokerType getType() {
        return JokerType.UNCOMMON;
    }


    @Override
    public int getIndex() {
        return 64 + ordinal();
    }
}

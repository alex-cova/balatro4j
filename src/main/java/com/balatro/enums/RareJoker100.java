package com.balatro.enums;

import com.balatro.api.Joker;

public enum RareJoker100 implements Joker {
    DNA("DNA"),
    Sixth_Sense("Sixth Sense"),
    Seance("Seance"),
    Baron("Baron"),
    Obelisk("Obelisk"),
    Baseball_Card("Baseball Card"),
    Ancient_Joker("Ancient Joker"),
    Campfire("Campfire"),
    Blueprint("Blueprint"),
    Wee_Joker("Wee Joker"),
    Hit_the_Road("Hit the Road"),
    The_Duo("The Duo"),
    The_Trio("The Trio"),
    The_Family("The Family"),
    The_Order("The Order"),
    The_Tribe("The Tribe"),
    Invisible_Joker("Invisible Joker"),
    Brainstorm("Brainstorm"),
    Drivers_License("Drivers Licens");

    private final String name;

    RareJoker100(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 2;
    }

    @Override
    public JokerType getType() {
        return JokerType.RARE;
    }



}

package com.balatro.enums;

import com.balatro.api.Joker;
import com.balatro.api.Stored;

public enum CommonJoker implements Joker, Stored {
    Joker("Joker"),
    Greedy_Joker("Greedy Joker"),
    Lusty_Joker("Lusty Joker"),
    Wrathful_Joker("Wrathful Joker"),
    Gluttonous_Joker("Gluttonous Joker"),
    Jolly_Joker("Jolly Joker"),
    Zany_Joker("Zany Joker"),
    Mad_Joker("Mad Joker"),
    Crazy_Joker("Crazy Joker"),
    Droll_Joker("Droll Joker"),
    Sly_Joker("Sly Joker"),
    Wily_Joker("Wily Joker"),
    Clever_Joker("Clever Joker"),
    Devious_Joker("Devious Joker"),
    Crafty_Joker("Crafty Joker"),
    Half_Joker("Half Joker"),
    Credit_Card("Credit Card"),
    Banner("Banner"),
    Mystic_Summit("Mystic Summit"),
    Ball("8 Ball"),
    Misprint("Misprint"),

    Raised_Fist("Raised Fist"),

    Chaos_the_Clown("Chaos the Clown"),

    Scary_Face("Scary Face"),

    Abstract_Joker("Abstract Joker"),

    Delayed_Gratification("Delayed Gratification"),

    Gros_Michel("Gros Michel"),

    Even_Steven("Even Steven"),

    Odd_Todd("Odd Todd"),

    Scholar("Scholar"),

    Business_Card("Business Card"),

    Supernova("Supernova"),

    Ride_the_Bus("Ride the Bus"),

    Egg("Egg"),

    Runner("Runner"),

    Ice_Cream("Ice Cream"),

    Splash("Splash"),

    Blue_Joker("Blue Joker"),

    Faceless_Joker("Faceless Joker"),

    Green_Joker("Green Joker"),

    Superposition("Superposition"),

    To_Do_List("To Do List"),

    Cavendish("Cavendish"),

    Red_Card("Red Card"),

    Square_Joker("Square Joker"),

    Riffraff("Riff-raff"),

    Photograph("Photograph"),

    Reserved_Parking("Reserved Parking"),

    Mail_In_Rebate("Mail In Rebate"),

    Hallucination("Hallucination"),

    Fortune_Teller("Fortune Teller"),

    Juggler("Juggler"),

    Drunkard("Drunkard"),

    Golden_Joker("Golden Joker"),

    Popcorn("Popcorn"),

    Walkie_Talkie("Walkie Talkie"),

    Smiley_Face("Smiley Face"),

    Golden_Ticket("Golden Ticket"),

    Swashbuckler("Swashbuckler"),

    Hanging_Chad("Hanging Chad"),

    Shoot_the_Moo("Shoot the Moon");

    private final String name;

    CommonJoker(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getYIndex() {
        return 0;
    }

    @Override
    public JokerType getType() {
        return JokerType.COMMON;
    }

    @Override
    public int getIndex() {
        return 128 + ordinal();
    }
}

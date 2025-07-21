package com.balatro;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public interface Seed32bit {

    char[] CHARACTERS_ON = {
            'L', '7', 'H', '1', 'V', 'C', '5', '4',
            '9', 'T', 'M', 'O', '8', '3', 'J', 'S',
            'K', 'B', 'N', 'E', 'R', '6', 'G', 'P',
            'D', 'F', '2', 'Q', 'U', 'I', 'W', 'A'
    };

    char[] CHARACTERS_OFF = {
            'N', '6', 'F', '8', 'E', 'T', 'H', 'C',
            '4', 'W', '5', 'M', '7', 'P', 'Q', 'B',
            'S', 'R', 'A', 'I', 'L', 'D', 'K', 'U',
            'X', 'G', '9', 'Z', 'Y', 'O', 'V', 'J'
    };

    static boolean isSeedable(int value) {
        int bitCount = Integer.bitCount(value);
        return bitCount == 8 || bitCount == 24;
    }

    default String generateSeed() {
        return decode(generateIntSeed());
    }

    default int generateIntSeed() {
        boolean generateOff = ThreadLocalRandom.current().nextBoolean();
        return generateSeedWithBits(generateOff ? 24 : 8, generateOff);
    }

    private int generateSeedWithBits(int bitCount, boolean off) {
        int value = off ? Integer.MAX_VALUE : 0;

        while (Integer.bitCount(value) != bitCount) {
            int bit = 1 << ThreadLocalRandom.current().nextInt(32);
            value = off ? (value & ~bit) : (value | bit);
        }

        return value;
    }

    default int encode(@NotNull String seed) {
        int valueOn = 0;
        int valueOff = Integer.MAX_VALUE;
        boolean hasOffChar = false;

        for (char c : seed.toCharArray()) {
            if (c > 'W') hasOffChar = true;

            for (int i = 0; i < 32; i++) {
                if (c == CHARACTERS_ON[i]) {
                    valueOn |= (1 << i);
                }
                if (c == CHARACTERS_OFF[i]) {
                    valueOff &= ~(1 << i);
                }
            }
        }

        if (hasOffChar) {
            return validateAndReturn(valueOff, 24, seed);
        }

        if (Integer.bitCount(valueOn) == 8 && seed.equals(decode(valueOn))) {
            return valueOn;
        }

        return validateAndReturn(valueOff, 24, seed);
    }

    private int validateAndReturn(int value, int expectedBits, String seed) {
        if (Integer.bitCount(value) != expectedBits) {
            throw new IllegalArgumentException("Not a valid 32-bit seed: " + seed);
        }
        return value;
    }

    default String decode(int value) {
        int bitCount = Integer.bitCount(value);
        if (bitCount != 8 && bitCount != 24) {
            throw new IllegalArgumentException("Invalid seed bit count: " + value);
        }

        StringBuilder sb = new StringBuilder(8);
        char[] charset = (bitCount == 8) ? CHARACTERS_ON : CHARACTERS_OFF;

        for (int i = 0; i < 32 && sb.length() < 8; i++) {
            boolean match = ((value >>> i) & 1) == (bitCount == 8 ? 1 : 0);
            if (match) sb.append(charset[i]);
        }

        return sb.toString();
    }
}

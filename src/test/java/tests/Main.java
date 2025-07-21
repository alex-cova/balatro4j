package tests;

public class Main {

    static int[][] comb = new int[32][32];

    public static void main(String[] args) {
        // pre-compute binomial coefficients

        comb[0][0] = 1;
        for (int i = 1; i < 32; i++) {
            comb[i][0] = comb[i][i] = 1;
            for (int j = 1; j < 32; j++) {
                comb[i][j] = comb[i - 1][j - 1] + comb[i - 1][j];
            }
        }

        int original = 0b00000000_00000000_00001111_00001111; // 8 bits set
        int compressed = compress(original);
        int decompressed = decompress(compressed);

        System.out.println("Original:     " + Integer.toBinaryString(original));
        System.out.println("Compressed:   " + compressed);
        System.out.println("Decompressed: " + Integer.toBinaryString(decompressed));
        System.out.println("Match:        " + (original == decompressed));

    }

    public static int compress(int seed32) {
        int seed24 = 0;
        for (int i = 31, rem = 8; 0 <= i; i--) {
            if (((seed32 >>> i) & 1) == 1) {
                seed24 += comb[i][rem];
                rem--;
            }
        }

        return seed24;
    }

    public static int decompress(int seed24) {
        int seed32 = 0;
        for (int i = 31, rem = 8; 0 <= i; i--) {
            if (comb[i][rem] <= seed24) {
                seed24 -= comb[i][rem];
                seed32 |= 1 << i;   // adjust here to convert directly into char seed instead
                rem--;
            }
        }

        return seed32;
    }

}
package tests;

import com.balatro.BalatroImpl;
import com.balatro.enums.*;

import java.io.IOException;

public class CompressionTest {

    public static void main(String[] args) throws IOException {
        int len = 0;

        len += BalatroImpl.CHARACTERS_MIN.length;
        len += LegendaryJoker.values().length;
        len += RareJoker101C.values().length;
        len += UnCommonJoker101C.values().length;
        len += CommonJoker100.values().length;
        len += Spectral.values().length;
        len += Tag.values().length;
        len += 1;//Blackhole
        len += Voucher.values().length;

        System.out.println("Chars: " + BalatroImpl.CHARACTERS_MIN.length);
        System.out.println("LegendaryJoker: " + LegendaryJoker.values().length);
        System.out.println("RareJoker101C: " + RareJoker101C.values().length);
        System.out.println("UnCommonJoker101C: " + UnCommonJoker101C.values().length);
        System.out.println("CommonJoker100: " + CommonJoker100.values().length);
        System.out.println("Spectral: " + Spectral.values().length);
        System.out.println("Tag.values: " + Tag.values().length);
        System.out.println("Specials: " + Specials.values().length);
        System.out.println("Voucher: " + Voucher.values().length);

        System.out.println("Required len: " + len + " " + (len / 64.0f));

        /*
        LegendaryJoker: 5
RareJoker101C: 20
UnCommonJoker101C: 64
CommonJoker100: 60
Spectral: 16
Tag.values: 24
BlackHole: 1
         */

        /*
        [UnCommonJoker101C] : 64
        [CommonJoker100, BlackHole] : 61
        [RareJoker101C: 20, Spectral: 16, Tag: 24, Legendary: 5] : 65
         */
    }
}

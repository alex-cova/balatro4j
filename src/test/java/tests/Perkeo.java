package tests;

import com.balatro.api.Item;
import com.balatro.enums.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Perkeo {
    public static void main(String[] args) {
        var items = new ArrayList<Item>();

        items.addAll(Arrays.asList(Boss.values()));
        items.addAll(Arrays.asList(CommonJoker.values()));
        items.addAll(Arrays.asList(CommonJoker100.values()));
        items.addAll(Arrays.asList(LegendaryJoker.values()));
        items.addAll(Arrays.asList(Planet.values()));
        items.addAll(Arrays.asList(RareJoker.values()));
        items.addAll(Arrays.asList(RareJoker100.values()));
        items.addAll(Arrays.asList(RareJoker101C.values()));
        items.addAll(Arrays.asList(Spectral.values()));
        items.addAll(Arrays.asList(Tag.values()));
        items.addAll(Arrays.asList(Tarot.values()));
        items.addAll(Arrays.asList(UnCommonJoker.values()));
        items.addAll(Arrays.asList(UnCommonJoker100.values()));
        items.addAll(Arrays.asList(UnCommonJoker101C.values()));
        items.addAll(Arrays.asList(Voucher.values()));

        items.stream()
                .map(Item::getName)
                .sorted()
                .distinct()
                .map("\"%s\","::formatted)
                .forEach(System.out::println);
    }

    private static void print(Enum<?>[] values) {
        for (Enum<?> value : values) {
            if (value instanceof Item item) {
                System.out.println(item.getName());
            }
        }
    }
}

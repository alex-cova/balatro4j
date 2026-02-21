package tests;

import com.balatro.Functions;
import com.balatro.api.Balatro;
import org.junit.jupiter.api.Test;

public class RNGTests {

    @Test
    void testEditions() {
        var functions = Balatro.builder("FHSRBAMA", 8)
                .functions();

        for (int i = 1; i < 20; i++) {
            functions.getEdition(1, Functions.editionBufArr);
        }
    }

}

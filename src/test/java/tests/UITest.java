package tests;

import com.balatro.api.Balatro;
import com.balatro.ui.SeedRenderer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class UITest {

    @Test
    void testRender() {
        var run = Balatro.random(8)
                .analyzeAll();

        var image = new SeedRenderer(run)
                .render();

        try {
            ImageIO.write(image, "PNG", new File("rendered.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.balatro.ui;

import com.balatro.api.Ante;
import com.balatro.api.Run;
import com.balatro.structs.EditionItem;
import com.balatro.structs.Pack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public record SeedRenderer(Run run) {

    public @NotNull BufferedImage render() {
        var image = new BufferedImage(2048, 4540, BufferedImage.TYPE_INT_RGB);

        var graphics = image.getGraphics();
        int y = 30;

        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        graphics.setFont(new Font("Arial", Font.BOLD, 20));
        graphics.setColor(Color.WHITE);
        graphics.drawString("Seed: " + run.seed(), 10, y);

        y += 30;

        for (Ante ante : run) {
            var imageAnte = renderAnte(ante);
            graphics.drawImage(imageAnte, 0, y, null);
            y += imageAnte.getHeight() + 10;
        }

        return image;
    }

    private @NotNull BufferedImage renderAnte(@NotNull Ante ante) {
        var image = new BufferedImage(2048, 550, BufferedImage.TYPE_INT_RGB);
        var graphics = image.getGraphics();
        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setFont(new Font("Arial", Font.BOLD, 20));
        graphics.setColor(Color.WHITE);

        int x = 10;
        int y = 30;

        graphics.drawString("Ante: %s, Boss: %s, Voucher: %s"
                .formatted(ante.getAnte(), ante.getBoss(), ante.getVoucher()), x, y);
        graphics.drawLine(0, y + 4, image.getWidth(), y +4 );
        y += 30;

        graphics.drawString("Shop Queue", x, y);
        y += 15;

        int s = 0;
        for (EditionItem item : ante.getShopQueue()) {
            graphics.drawImage(SpriteUtil.getSprite(item), x, y, null);
            x += 71;
            s += 1;

            if (s % 25 == 0) {
                y += 100;
                x = 10;
            }
        }

        if (ante.getAnte() == 1) {
            y += 100;
        }

        s = 0;
        x = 10;
        y += 30;

        for (Pack pack : ante.getPacks()) {
            graphics.drawString(pack.getType().getName(), x, y);

            for (EditionItem option : pack.getOptions()) {
                graphics.drawImage(SpriteUtil.getSprite(option), x, y + 5, null);
                x += 71;
            }

            s++;

            if (s % 5 == 0) {
                y += 130;
                x = 10;
            } else {
                x += 30;
            }
        }

        return image;
    }

}

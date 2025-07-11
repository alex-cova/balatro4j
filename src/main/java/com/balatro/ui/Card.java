package com.balatro.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Card extends JLabel {

    private final BufferedImage image;

    public Card(String text, BufferedImage image) {
        super(text);
        this.image = image;
        setPreferredSize(new Dimension(71, 95));
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }
}

package com.balatro.ui;

import com.balatro.api.Filter;
import com.balatro.api.Item;
import com.balatro.api.Joker;
import com.balatro.enums.Edition;
import com.balatro.enums.LegendaryJoker;
import com.balatro.structs.EditionItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ItemCheckbox extends JPanel {

    private final Item item;
    private final JSpinner spinner;
    private final JCheckBox checkBox;
    private BufferedImage image;
    private final JComboBox<Edition> comboBox;

    public ItemCheckbox(@NotNull Item item) {
        this.item = item;

        checkBox = new JCheckBox(item.getName());
        spinner = new JSpinner();
        comboBox = new JComboBox<>();

        for (Edition value : Edition.values()) {
            comboBox.addItem(value);
        }

        comboBox.addActionListener(event -> {
            image = SpriteUtil.getSprite(new EditionItem(item, (Edition) comboBox.getSelectedItem()));
            repaint();
        });

        comboBox.setSelectedItem(Edition.NoEdition);

        image = SpriteUtil.getSprite(new EditionItem(item, (Edition) comboBox.getSelectedItem()));

        setLayout(new Layout());

        JPanel spinnerPanel = new JPanel(new GridLayout(1, 2)) {
            @Override
            public void paint(Graphics g) {
                super.paintChildren(g);
            }
        };
        spinnerPanel.add(new JLabel("Max Ante:"));
        spinnerPanel.add(spinner);

        add(checkBox);

        checkBox.addItemListener(event -> {
            if (checkBox.isSelected()) {
                add(spinnerPanel);

                if (item instanceof Joker || item instanceof LegendaryJoker) {
                    add(comboBox);
                }

            } else {
                remove(spinnerPanel);
                remove(comboBox);
            }

            updateUI();
            repaint();
        });

        setPreferredSize(new Dimension(220, 110));
    }

    @Override
    public void setEnabled(boolean enabled) {
        checkBox.setEnabled(enabled);
        spinner.setEnabled(enabled);
        if (comboBox != null) {
            comboBox.setEnabled(enabled);
        }
    }

    static class Layout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {

        }

        @Override
        public void removeLayoutComponent(Component comp) {

        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(220, 91);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(220, 91);
        }

        @Override
        public void layoutContainer(Container parent) {
            int y = 0;
            for (Component component : parent.getComponents()) {
                component.setBounds(75, y, 140, 35);
                y += 35;
            }
        }
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public int getAnte() {
        return (Integer) spinner.getValue();
    }

    public Filter filter() {
        var ante = (int) spinner.getValue();
        var edition = (Edition) comboBox.getSelectedItem();

        if (ante <= 0) {
            ante = -1;
        }

        System.out.println("Filtering: " + item + " " + edition);

        return item.auto(ante, edition);
    }

    public EditionItem getEditionItem() {
        return new EditionItem(item, (Edition) comboBox.getSelectedItem());
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 2, 7, this);
        }
        super.paintChildren(g);
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    public Item getItem() {
        return item;
    }
}

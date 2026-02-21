package com.balatro.ui;

import java.awt.*;

/**
 * Custom Horizontal Layout Manager
 * Arranges components in a single row from left to right
 */
public class HorizontalLayoutManager implements LayoutManager {
    private int hgap = 5; // horizontal gap between components
    private int vgap = 5; // vertical gap from container edges

    public HorizontalLayoutManager() {
        this(5, 5);
    }

    public HorizontalLayoutManager(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // Not used in this implementation
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // Not used in this implementation
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return calculateLayoutSize(parent, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return calculateLayoutSize(parent, false);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();

            if (componentCount == 0) return;

            int availableWidth = parent.getWidth() - insets.left - insets.right;
            int availableHeight = parent.getHeight() - insets.top - insets.bottom - (2 * vgap);

            // Calculate total preferred width and find maximum height
            int totalPreferredWidth = 0;
            int maxHeight = 0;

            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    Dimension pref = comp.getPreferredSize();
                    totalPreferredWidth += pref.width;
                    maxHeight = Math.max(maxHeight, pref.height);
                    if (i > 0) totalPreferredWidth += hgap;
                }
            }

            // Calculate component width (distribute available space)
            int componentWidth = availableWidth;
            if (totalPreferredWidth > 0) {
                componentWidth = Math.max(1, (availableWidth - ((componentCount - 1) * hgap)) / componentCount);
            }

            // Position components
            int x = insets.left;
            int y = insets.top + vgap;
            int height = Math.min(maxHeight, availableHeight);

            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    comp.setBounds(x, y, componentWidth, height);
                    x += componentWidth + hgap;
                }
            }
        }
    }

    private Dimension calculateLayoutSize(Container parent, boolean preferred) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();

            int width = 0;
            int height = 0;

            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    Dimension size = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                    width += size.width;
                    height = Math.max(height, size.height);
                    if (i > 0) width += hgap;
                }
            }

            width += insets.left + insets.right;
            height += insets.top + insets.bottom + (2 * vgap);

            return new Dimension(width, height);
        }
    }
}
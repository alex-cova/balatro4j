package com.balatro.ui;

import java.awt.*;

/**
 * Custom Vertical Layout Manager
 * Arranges components in a single column from top to bottom
 */
public class VerticalLayoutManager implements LayoutManager {
    private int hgap = 5; // horizontal gap from container edges
    private int vgap = 5; // vertical gap between components

    public VerticalLayoutManager() {
        this(5, 5);
    }

    public VerticalLayoutManager(int hgap, int vgap) {
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

            int availableWidth = parent.getWidth() - insets.left - insets.right - (2 * hgap);
            int availableHeight = parent.getHeight() - insets.top - insets.bottom;

            // Calculate total preferred height and find maximum width
            int totalPreferredHeight = 0;
            int maxWidth = 0;

            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    Dimension pref = comp.getPreferredSize();
                    totalPreferredHeight += pref.height;
                    maxWidth = Math.max(maxWidth, pref.width);
                    if (i > 0) totalPreferredHeight += vgap;
                }
            }

            // Calculate component height (distribute available space)
            int componentHeight = availableHeight;
            if (totalPreferredHeight > 0) {
                componentHeight = Math.max(1, (availableHeight - ((componentCount - 1) * vgap)) / componentCount);
            }

            // Position components
            int x = insets.left + hgap;
            int y = insets.top;
            int width = Math.min(maxWidth, availableWidth);

            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    comp.setBounds(x, y, width, componentHeight);
                    y += componentHeight + vgap;
                }
            }
        }
    }

    private Dimension calculateLayoutSize(Container parent, boolean preferred) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int componentCount = parent.getComponentCount();

            int height = 0;

            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    Dimension size = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                    height += size.height;
                    if (i > 0) height += vgap;
                }
            }

            height += insets.top + insets.bottom;

            return new Dimension(parent.getWidth(), height);
        }
    }
}
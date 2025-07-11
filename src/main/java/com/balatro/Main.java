package com.balatro;

import com.balatro.api.*;
import com.balatro.enums.*;
import com.balatro.structs.EditionItem;
import com.balatro.structs.PackInfo;
import com.balatro.ui.*;
import com.balatro.ui.Card;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends JFrame {

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel progressLabel = new JLabel();
    private final JButton startButton = new JButton("Start");
    private final List<ItemCheckbox> selections = new ArrayList<>();

    public Main() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1024, 500));

        add(tabbedPane, BorderLayout.CENTER);
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        add(toolBar, BorderLayout.NORTH);

        progressLabel.setPreferredSize(new Dimension(250, 20));

        toolBar.add(startButton);
        toolBar.add(progressLabel);
        toolBar.add(progressBar);

        toolBar.setPreferredSize(new Dimension(1024, 45));

        render(LegendaryJoker.values(), "Legendary Joker");
        render(RareJoker101C.values(), "Rare Joker");
        render(UnCommonJoker101C.values(), "Uncommon Joker");
        render(CommonJoker100.values(), "Common Joker");
        render(Spectral.values(), "Spectral");
        render(Voucher.values(), "Voucher");
        render(Tag.values(), "Tag");
        render(Tarot.values(), "Arcana");
        render(Planet.values(), "Celestial");

        setLocationRelativeTo(null);

        setTitle("Pierrot Seed Searcher");

        startButton.addActionListener(e -> search());
    }

    private void search() {
        var filter = buildFilter();

        if (filter == null) {
            JOptionPane.showMessageDialog(this, "Please select an Item", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        startButton.setEnabled(false);

        new Thread(() -> {
            var seeds = Balatro.search(Runtime.getRuntime().availableProcessors(), 1_000_000)
                    .filter(filter)
                    .autoConfigure()
                    .progressListener((speed, progress) -> {
                        SwingUtilities.invokeLater(() -> {
                            progressLabel.setText(speed);
                            progressBar.setValue(progress);
                        });
                    })
                    .find();

            var runs = seeds.parallelStream()
                    .map(seed -> Balatro.builder(seed, 8)
                            .analyzeAll())
                    .sorted((A, B) -> Float.compare(A.getScore(), B.getScore()) * -1)
                    .toList();

            renderRuns(runs);

            startButton.setEnabled(true);
        }).start();

    }

    private void renderRuns(@NotNull List<Run> runs) {
        JTable table = new JTable();

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{"Seed", "Score"});

        for (int i = 0; i < runs.size(); i++) {
            Run run = runs.get(i);
            model.addRow(new Object[]{run, run.getScore()});
        }

        table.setModel(model);

        tabbedPane.add("Results", new JScrollPane(table));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (table.getSelectedRow() != -1) {
                    Run run = (Run) model.getValueAt(table.getSelectedRow(), 0);

                    JDialog dialog = new JDialog(Main.this, true);
                    dialog.setContentPane(buildPreview(run));
                    dialog.setTitle(run.seed() + " " + run.getScore());
                    dialog.setLocationRelativeTo(Main.this);
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setSize(new Dimension(1024, 768));
                    dialog.setVisible(true);
                }
            }
        });
    }

    @Contract("_ -> new")
    private @NotNull JComponent buildPreview(@NotNull Run run) {
        JPanel preview = new JPanel();
        preview.setLayout(new GridLayout(8, 1));

        for (Ante ante : run) {
            var antePanel = new JPanel();
            antePanel.setLayout(new VerticalLayoutManager());
            antePanel.setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), "Ante " + ante.getAnte()));

            var shop = new JPanel();
            shop.setLayout(new HorizontalLayoutManager());
            shop.setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), "Shop"));
            var scrollShop = new JScrollPane(shop);
            scrollShop.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            antePanel.add(scrollShop);

            for (EditionItem editionItem : ante.getShopQueue()) {
                shop.add(new Card(editionItem.getName(), SpriteUtil.getSprite(editionItem.item())));
            }

            for (PackInfo pack : ante.getPacks()) {
                var packPanel = new JPanel();
                packPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                packPanel.setBorder(new TitledBorder(pack.getType().getName()));
                packPanel.setMinimumSize(new Dimension(100, 100));

                for (EditionItem option : pack.getOptions()) {
                    packPanel.add(new Card(option.getName(), SpriteUtil.getSprite(option.item())));
                }

                antePanel.add(packPanel);
            }

            preview.add(antePanel);
        }

        var sc = new JScrollPane(preview);
        sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        return sc;
    }

    private @Nullable Filter buildFilter() {
        var items = selections.stream()
                .filter(ItemCheckbox::isSelected)
                .map(ItemCheckbox::filter)
                .filter(Objects::nonNull)
                .toList();

        if (items.isEmpty()) {
            return null;
        }

        return Filter.compound(items);
    }

    private void render(Item[] items, String tabName) {
        JPanel panel = new JPanel();
        tabbedPane.add(tabName, new JScrollPane(panel));

        panel.setLayout(new WrapLayout());

        for (Item item : items) {
            var selector = new ItemCheckbox(item);
            panel.add(selector);
            selections.add(selector);
        }
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }
        new Main()
                .setVisible(true);
    }
}

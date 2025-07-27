package com.balatro;

import com.balatro.api.*;
import com.balatro.cache.JokerFile;
import com.balatro.cache.PreProcessedSeeds;
import com.balatro.cache.QueryResult;
import com.balatro.enums.*;
import com.balatro.structs.EditionItem;
import com.balatro.structs.Pack;
import com.balatro.ui.*;
import com.balatro.ui.Card;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Main extends JFrame {

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel progressLabel = new JLabel();
    private final JButton startButton = new JButton("Start");
    private final List<ItemCheckbox> selections = new ArrayList<>();
    private final JComboBox<Stake> stakeComboBox = new JComboBox<>();
    private final JComboBox<Deck> deckComboBox = new JComboBox<>();
    private final JComboBox<Integer> seedsPerThread = new JComboBox<>();
    private final JCheckBox cache = new JCheckBox("Search in cache");

    public Main() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1024, 500));

        add(tabbedPane, BorderLayout.CENTER);
        JPanel toolBar = new JPanel();
        JPanel footer = new JPanel();
        footer.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        add(toolBar, BorderLayout.NORTH);
        add(footer, BorderLayout.SOUTH);

        footer.add(progressLabel);
        footer.add(progressBar);

        progressLabel.setPreferredSize(new Dimension(250, 20));

        toolBar.add(startButton);
        toolBar.add(new JLabel("Deck:"));
        toolBar.add(deckComboBox);
        toolBar.add(new JLabel("Stake:"));
        toolBar.add(stakeComboBox);
        toolBar.add(new JLabel("Seeds per thread:"));
        toolBar.add(seedsPerThread);
        toolBar.add(cache);

        var button = new JButton("Enter");
        button.addActionListener(e -> {
            var seed = JOptionPane.showInputDialog(this, "Enter seed");

            var run = Balatro.builder(seed, 8)
                    .analyzeAll();

            showRun(run);
        });

        toolBar.add(button);

        cache.setToolTipText("Way Faster search but limited");

        seedsPerThread.addItem(500_000);
        seedsPerThread.addItem(1_000_000);
        seedsPerThread.addItem(2_000_000);
        seedsPerThread.addItem(5_000_000);
        seedsPerThread.addItem(10_000_000);
        seedsPerThread.addItem(25_000_000);
        seedsPerThread.addItem(50_000_000);
        seedsPerThread.addItem(100_000_000);
        seedsPerThread.addItem(500_000_000);

        for (Stake value : Stake.values()) {
            stakeComboBox.addItem(value);
        }

        for (Deck value : Deck.values()) {
            deckComboBox.addItem(value);
        }

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

    private List<EditionItem> getSelections() {
        return selections.stream()
                .filter(ItemCheckbox::isSelected)
                .map(ItemCheckbox::getEditionItem)
                .filter(Objects::nonNull)
                .toList();
    }

    private void search() {
        var filter = buildFilter();

        if (filter == null) {
            JOptionPane.showMessageDialog(this, "Please select an Item", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        startButton.setEnabled(false);
        deckComboBox.setEnabled(false);
        stakeComboBox.setEnabled(false);
        seedsPerThread.setEnabled(false);
        cache.setEnabled(false);

        for (ItemCheckbox selection : selections) {
            selection.setEnabled(false);
        }

        final var selectedDeck = (Deck) deckComboBox.getSelectedItem();
        final var selectedStake = (Stake) stakeComboBox.getSelectedItem();

        new Thread(() -> {
            Set<String> seeds;

            var file = new File("cache.jkr");

            if (cache.isSelected() && file.exists()) {
                seeds = new PreProcessedSeeds()
                        .loadFile(file)
                        .search(getSelections())
                        .stream()
                        .map(QueryResult::seed)
                        .collect(Collectors.toSet());
            } else {
                seeds = Balatro.search(Runtime.getRuntime().availableProcessors(),
                                Runtime.getRuntime().availableProcessors() * (Integer) seedsPerThread.getSelectedItem())
                        .configuration(balatro -> balatro.deck(selectedDeck)
                                .stake(selectedStake))
                        .filter(filter)
                        .autoConfigure()
                        .progressListener((speed, progress) -> {
                            SwingUtilities.invokeLater(() -> {
                                progressLabel.setText(speed);
                                progressBar.setValue(progress);
                            });
                        })
                        .find();
            }

            progressBar.setValue(-1);
            progressBar.setMaximum(Math.min(seeds.size(), 25000));
            progressBar.setMinimum(0);

            progressLabel.setText("Processing seeds...");

            var runs = seeds.parallelStream()
                    .limit(Math.min(seeds.size(), 25000))
                    .map(seed -> {
                        var run = Balatro.builder(seed, 8)
                                .analyzeAll();

                        progressBar.setValue(progressBar.getValue() + 1);

                        return run;
                    })
                    .sorted((A, B) -> Float.compare(A.getScore(), B.getScore()) * -1)
                    .toList();

            progressBar.setMaximum(100);
            progressBar.setValue(0);
            renderRuns(runs);

            startButton.setEnabled(true);
            deckComboBox.setEnabled(true);
            stakeComboBox.setEnabled(true);
            seedsPerThread.setEnabled(true);
            cache.setEnabled(true);

            for (ItemCheckbox selection : selections) {
                selection.setEnabled(true);
            }

            progressLabel.setText("Finished");

        }).start();

    }

    private void appendToCache(List<Run> runs) {
        progressLabel.setText("Adding found seeds to cache...");
        progressBar.setValue(JProgressBar.UNDEFINED_CONDITION);
        JokerFile.appendToCache(new File("cache.jkr"), runs);
    }

    private void renderRuns(@NotNull List<Run> runs) {
        if (runs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No seeds found, try again, or try with a bigger number of seeds per thread",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!cache.isSelected()) {
            appendToCache(runs);
        }

        JTable table = new JTable();

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{"Seed", "Score"});

        for (Run run : runs) {
            model.addRow(new Object[]{run, run.getScore()});
        }

        table.setModel(model);

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getComponentAt(i) instanceof JScrollPane s) {
                if (s.getClientProperty("kekw") != null) {
                    tabbedPane.removeTabAt(i);
                    break;
                }
            } else {
                System.out.println(tabbedPane.getTabComponentAt(i));
            }
        }

        var scroll = new JScrollPane(table);
        scroll.putClientProperty("kekw", "kekw");

        tabbedPane.insertTab("Results", null, scroll, "Results", 0);
        tabbedPane.setSelectedIndex(0);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (table.getSelectedRow() != -1) {
                    Run run = (Run) model.getValueAt(table.getSelectedRow(), 0);

                    showRun(run);
                }
            }
        });

        JOptionPane.showMessageDialog(this, "Found " + new DecimalFormat("#,###").format(runs.size())
                                            + " seeds", "Finished", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRun(Run run) {
        JDialog dialog = new JDialog(Main.this, true);
        dialog.setContentPane(buildPreview(run));
        dialog.setTitle(run.seed() + " " + run.getScore());
        dialog.setLocationRelativeTo(Main.this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(new Dimension(1024, 768));
        dialog.setVisible(true);
    }

    @Contract("_ -> new")
    private @NotNull JComponent buildPreview(@NotNull Run run) {
        JTabbedPane preview = new JTabbedPane();

        for (Ante ante : run) {
            var antePanel = new JPanel();
            antePanel.setLayout(new VerticalLayoutManager());

            var shop = new JPanel();
            shop.setLayout(new HorizontalLayoutManager());
            shop.setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), "Shop"));

            var scrollShop = new JScrollPane(shop);
            scrollShop.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
            scrollShop.setBorder(new EmptyBorder(0, 0, 0, 0));
            scrollShop.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            antePanel.add(scrollShop);

            for (EditionItem editionItem : ante.getShopQueue()) {
                shop.add(new Card(editionItem.getName(), SpriteUtil.getSprite(editionItem)));
            }

            for (Pack pack : ante.getPacks()) {
                var packPanel = new JPanel();
                packPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                packPanel.setBorder(new TitledBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK), pack.getType().getName()));
                packPanel.setMinimumSize(new Dimension(100, 100));

                for (EditionItem option : pack.getOptions()) {
                    packPanel.add(new Card(option.getName(), SpriteUtil.getSprite(option)));
                }

                antePanel.add(packPanel);
            }

            preview.addTab("Ante " + ante.getAnte(), antePanel);
        }

        var sc = new JScrollPane(preview);
        sc.setBorder(new EmptyBorder(0, 0, 0, 0));
        sc.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
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

    private void render(Item @NotNull [] items, String tabName) {
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

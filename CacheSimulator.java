import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

// Common interface for different cache types
interface Cache {
    void accessMemory(int address);
}

class FullyAssociativeCache implements Cache {
    private int[] tags;
    private int[] contents;
    private LinkedList<Integer> lruQueue;
    private int numberOfBlocks;
    private CachePanel cachePanel;

    public FullyAssociativeCache(int cacheSize, int blockSize, CachePanel cachePanel) {
        this.numberOfBlocks = cacheSize / blockSize;
        this.tags = new int[numberOfBlocks];
        this.contents = new int[numberOfBlocks];
        this.lruQueue = new LinkedList<>();
        this.cachePanel = cachePanel;

        for (int i = 0; i < numberOfBlocks; i++) {
            tags[i] = -1;
            contents[i] = -1;
            lruQueue.add(i);
        }
    }

    @Override
    public void accessMemory(int address) {
        int blockSize = cachePanel.getBlockSize();
        int tag = address / blockSize;

        boolean hit = false;
        for (int i = 0; i < numberOfBlocks; i++) {
            if (tags[i] == tag) {
                JOptionPane.showMessageDialog(null,"CACHE HIT");
                contents[i] = address;
                lruQueue.remove((Integer) i);
                lruQueue.addFirst(i);
                hit = true;
                break;
            }
        }

        if (!hit && address < CacheSimulator.mMemory) {
            JOptionPane.showMessageDialog(null,"CACHE MISS");
            int index = lruQueue.removeLast();
            tags[index] = tag;
            contents[index] = address;
            lruQueue.addFirst(index);
        } else if (address >= CacheSimulator.mMemory) {
            JOptionPane.showMessageDialog(null, "Memory Access violation / Segmentation Fault");
        }
        cachePanel.updateCache(tags, contents);
    }
}

class DirectMappedCache implements Cache {
    private int[] tags;
    private int[] contents;
    private int numberOfBlocks;
    private CachePanel cachePanel;

    public DirectMappedCache(int cacheSize, int blockSize, CachePanel cachePanel) {
        this.numberOfBlocks = cacheSize / blockSize;
        this.tags = new int[numberOfBlocks];
        this.contents = new int[numberOfBlocks];
        this.cachePanel = cachePanel;

        for (int i = 0; i < numberOfBlocks; i++) {
            tags[i] = -1;
            contents[i] = -1;
        }
    }

    @Override
    public void accessMemory(int address) {
        int blockSize = cachePanel.getBlockSize();
        int blockNumber = address / blockSize;
        int cacheIndex = blockNumber % numberOfBlocks;
        int tag = address / (numberOfBlocks * blockSize);

        if (tags[cacheIndex] == tag) {
            JOptionPane.showMessageDialog(null,"CACHE HIT");
            contents[cacheIndex] = address; // Update content even for cache hit
        } else if (tags[cacheIndex] != tag && address < CacheSimulator.mMemory) {
            JOptionPane.showMessageDialog(null,"CACHE MISS");
            tags[cacheIndex] = tag;
            contents[cacheIndex] = address;
        } else {
            JOptionPane.showMessageDialog(null, "Memory Access violation / Segmentation Fault");
        }
        cachePanel.updateCache(tags, contents);
    }
}

class SetAssociativeCache implements Cache {
    private int[][] tags;
    private int[][] contents;
    private LinkedList<Integer>[] lruQueue;
    private int numberOfSets;
    private int setSize;
    private int cacheLine;
    private CachePanel cachePanel;

    public SetAssociativeCache(int cacheSize, int blockSize, int setSize, CachePanel cachePanel) {
        this.numberOfSets = (cacheSize / blockSize) / setSize;
        this.setSize = setSize;
        this.cacheLine=cacheSize/blockSize;
        this.tags = new int[numberOfSets][setSize];
        this.contents = new int[numberOfSets][setSize];
        this.lruQueue = new LinkedList[numberOfSets];
        this.cachePanel = cachePanel;

        for (int i = 0; i < numberOfSets; i++) {
            lruQueue[i] = new LinkedList<>();
            for (int j = 0; j < setSize; j++) {
                tags[i][j] = -1;
                contents[i][j] = -1;
                lruQueue[i].add(j);
            }
        }
    }

    @Override
    public void accessMemory(int address) {
        int blockSize = cachePanel.getBlockSize();
        int blockNumber = address /blockSize;
        int setIndex = blockNumber % numberOfSets;
        int tag = address / (numberOfSets * blockSize);

        boolean hit = false;
        for (int i = 0; i < setSize; i++) {
            if (tags[setIndex][i] == tag) {
                JOptionPane.showMessageDialog(null,"CACHE HIT");
                contents[setIndex][i] = address;
                lruQueue[setIndex].remove((Integer) i);
                lruQueue[setIndex].addFirst(i);
                hit = true;
                break;
            }
        }

        if (!hit && address < CacheSimulator.mMemory) {
            JOptionPane.showMessageDialog(null,"CACHE MISS");
            int lruIndex = lruQueue[setIndex].removeLast();
            tags[setIndex][lruIndex] = tag;
            contents[setIndex][lruIndex] = address;
            lruQueue[setIndex].addFirst(lruIndex);
        } else if (address >= CacheSimulator.mMemory) {
            JOptionPane.showMessageDialog(null, "Memory Access violation / Segmentation Fault");
        }
        cachePanel.updateCache(tags, contents);
    }
}

class CachePanel extends JPanel {
    private int[][] tags;
    private int[][] contents;
    private int numberOfSets;
    private int setSize;
    private static final int BLOCK_WIDTH = 200;
    private static final int BLOCK_HEIGHT = 30;
    private static final int PADDING = 10;
    private int blockSize;

    public CachePanel(int numberOfSets, int setSize, int blockSize) {
        this.numberOfSets = numberOfSets;
        this.setSize = setSize;
        this.blockSize = blockSize;
        this.tags = new int[numberOfSets][setSize];
        this.contents = new int[numberOfSets][setSize];
        for (int i = 0; i < numberOfSets; i++) {
            for (int j = 0; j < setSize; j++) {
                tags[i][j] = -1;
                contents[i][j] = -1;
            }
        }
    }

    public int getBlockSize() {
        return blockSize;
    }

    // Overloaded method to update cache for Direct Mapped Cache
    public void updateCache(int[] newTags, int[] newContents) {
        for (int i = 0; i < newTags.length; i++) {
            tags[i][0] = newTags[i];
            contents[i][0] = newContents[i];
        }
        repaint();
    }

    // Overloaded method to update cache for Set Associative and Fully Associative Cache
    public void updateCache(int[][] newTags, int[][] newContents) {
        this.tags = newTags;
        this.contents = newContents;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < numberOfSets; i++) {
            for (int j = 0; j < setSize; j++) {
                int x = PADDING;
                int y = (i * setSize + j) * (BLOCK_HEIGHT + PADDING) + PADDING;

                if (tags[i][j] == -1) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.GREEN);
                }
                g.fillRect(x, y, BLOCK_WIDTH, BLOCK_HEIGHT);

                g.setColor(Color.BLACK);
                g.drawRect(x, y, BLOCK_WIDTH, BLOCK_HEIGHT);
                String text;
                if (tags[i][j] == -1) {
                    text = "Set " + i + ", Block " + j + ": [Empty]";
                } else {
                    text = "Set " + i + ", Block " + (contents[i][j]/blockSize) + ": Tag " + tags[i][j] + ", Content " + contents[i][j];
                }
                g.drawString(text, x + 10, y + 20);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BLOCK_WIDTH + 2 * PADDING, numberOfSets * setSize * (BLOCK_HEIGHT + PADDING) + PADDING);
    }
}

public class CacheSimulator extends JFrame {
    private JTextField addressField;
    private JButton accessButton;
    private Cache cache;
    private CachePanel cachePanel;
    public static int mMemory;

    public CacheSimulator(int cacheSize, int blockSize, int setSize, String mappingType, int mMemory) {
        CacheSimulator.mMemory = mMemory;
        setTitle("Cache Simulator");
        setSize(500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize components
        addressField = new JTextField(15);
        accessButton = new JButton("Access Memory");

        // Panel for input
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Memory Address:"));
        inputPanel.add(addressField);
        inputPanel.add(accessButton);

        int numberOfSets;
if ("Direct Mapped".equals(mappingType)) {
    numberOfSets = cacheSize / blockSize;
} else if ("Set Associative".equals(mappingType)) {
    if (setSize == 0) {
        throw new IllegalArgumentException("Set size cannot be zero for Set Associative mapping.");
    }
    numberOfSets = (cacheSize / blockSize) / setSize;
} else if ("Fully Associative".equals(mappingType)) {
    numberOfSets = 1; // Fully associative cache has one set
} else {
    throw new IllegalArgumentException("Invalid mapping type.");
}

int associativity;
if ("Fully Associative".equals(mappingType)) {
    associativity = cacheSize / blockSize;
} else {
    associativity = setSize;
}

if (blockSize == 0) {
    throw new IllegalArgumentException("Block size cannot be zero.");
}

CachePanel cachePanel = new CachePanel(numberOfSets, associativity, blockSize);
JScrollPane cacheScrollPane = new JScrollPane(cachePanel);
        // Add components to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(cacheScrollPane, BorderLayout.CENTER);

        // Initialize cache based on the mapping type
        if (mappingType.equals("Direct Mapped")) {
            cache = new DirectMappedCache(cacheSize, blockSize, cachePanel);
        } else if (mappingType.equals("Set Associative")) {
            cache = new SetAssociativeCache(cacheSize, blockSize, setSize, cachePanel);
        } else if (mappingType.equals("Fully Associative")) {
            cache = new FullyAssociativeCache(cacheSize, blockSize, cachePanel);
        }

        // Action listener for the button
        accessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String addressText = addressField.getText();
                try {
                    int address = Integer.parseInt(addressText);
                    cache.accessMemory(address);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(CacheSimulator.this, "Invalid address. Please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                addressField.setText("");
            }
        });
    }

    public static void main(String[] args) {
        // Input for cache and block size
        int mMemory = Integer.parseInt(JOptionPane.showInputDialog("Enter Main Memory size (in bytes):"));
        int cacheSize = Integer.parseInt(JOptionPane.showInputDialog("Enter cache size (in bytes):"));
        int blockSize = Integer.parseInt(JOptionPane.showInputDialog("Enter block size (in bytes):"));
        int setSize = 1;

        String[] options = {"Direct Mapped", "Fully Associative", "Set Associative"};
        String mappingType = (String) JOptionPane.showInputDialog(null, "Choose Cache Mapping Type:", "Cache Mapping Type", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (mappingType.equals("Set Associative")) {
            setSize = Integer.parseInt(JOptionPane.showInputDialog("Enter set size (number of blocks per set):"));
        }

        // Create and display the simulator
        CacheSimulator simulator = new CacheSimulator(cacheSize, blockSize, setSize, mappingType, mMemory);
        simulator.setVisible(true);
    }
}
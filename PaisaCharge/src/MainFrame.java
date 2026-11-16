import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

/** Unit 5: Swing UI. BorderLayout shell, GridLayout forms, ActionListeners on every button. */
public class MainFrame extends JFrame {
    private final RechargeService svc = new RechargeService();
    private boolean dark = true;

    private final JPanel header = new JPanel(new BorderLayout());
    private final JLabel title = new JLabel("  PaisaCharge  |  Mobile Recharge & Billing");
    private final JButton themeBtn = new JButton("Light mode");
    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
    private final JLabel status = new JLabel(" Expiry watcher starting...");

    private final DefaultTableModel subModel = model("Name", "Mobile", "Type", "Status", "Valid till");
    private final DefaultTableModel planModel = model("Plan", "Type", "Price (Rs.)", "Details");
    private final DefaultTableModel histModel = model("Bill #", "Date", "Mobile", "Plan", "Amount (Rs.)");
    private final JTable subTable = new JTable(subModel), planTable = new JTable(planModel), histTable = new JTable(histModel);

    private final JTextField nameF = new JTextField(), mobileF = new JTextField(), rcMobile = new JTextField();
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Prepaid", "Postpaid"});
    private final JComboBox<Plan> planBox = new JComboBox<>();
    private final JTextArea billArea = new JTextArea();
    private List<Subscriber> subList = List.of();
    private List<Recharge> history = List.of();

    private static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }

    public MainFrame() {
        super("PaisaCharge - Mobile Recharge and Billing System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // NORTH: header, CENTER: module tabs, SOUTH: status bar
        title.setFont(new Font("Serif", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 12));
        header.add(title, BorderLayout.WEST);
        header.add(themeBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        tabs.addTab("Subscribers", subscribersTab());
        tabs.addTab("Plan catalogue", new JScrollPane(planTable));
        tabs.addTab("Recharge", rechargeTab());
        tabs.addTab("Bill", billTab());
        tabs.addTab("History", historyTab());

        themeBtn.addActionListener(e -> { dark = !dark; themeBtn.setText(dark ? "Light mode" : "Dark mode"); applyTheme(); });
        applyTheme();
        refreshAll();
        new ExpiryChecker(svc, status::setText, this::refreshSubscribers).start();   // Unit 3 thread
    }

    // ---------- Module 1: subscriber records ----------
    private JPanel subscribersTab() {
        JPanel form = new JPanel(new GridLayout(2, 4, 10, 6));
        JButton add = new JButton("Add subscriber");
        form.add(new JLabel("Name")); form.add(new JLabel("Mobile number")); form.add(new JLabel("Connection")); form.add(new JLabel(""));
        form.add(nameF); form.add(mobileF); form.add(typeBox); form.add(add);
        add.addActionListener(e -> {
            try {
                svc.addSubscriber(nameF.getText().trim(), mobileF.getText().trim(), (String) typeBox.getSelectedItem());
                nameF.setText(""); mobileF.setText(""); refreshSubscribers();
            } catch (Exception ex) { showError(ex); }
        });
        JButton del = new JButton("Remove selected");
        del.addActionListener(e -> {
            int row = subTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a subscriber in the table first."); return; }
            try { svc.deleteSubscriber(subList.get(row).getId()); refreshAll(); } catch (Exception ex) { showError(ex); }
        });
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(form, BorderLayout.NORTH);
        p.add(new JScrollPane(subTable), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT)); south.add(del);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    // ---------- Module 3: recharge ----------
    private JPanel rechargeTab() {
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        JButton go = new JButton("Recharge now");
        form.add(new JLabel("Mobile number")); form.add(rcMobile);
        form.add(new JLabel("Plan")); form.add(planBox);
        form.add(new JLabel("")); form.add(go);
        go.addActionListener(e -> {
            try {
                Plan p = (Plan) planBox.getSelectedItem();
                if (p == null) throw new IllegalArgumentException("Select a plan.");
                Recharge r = svc.recharge(rcMobile.getText().trim(), p.getId());
                billArea.setText(r.bill());
                refreshAll();
                tabs.setSelectedIndex(3);
            } catch (Exception ex) { showError(ex); }
        });
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(form, BorderLayout.NORTH);
        return p;
    }

    // ---------- Module 4: bill generation ----------
    private JPanel billTab() {
        billArea.setEditable(false);
        billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        billArea.setText("\n   No bill yet. Complete a recharge or pick one from History.");
        JButton print = new JButton("Print bill");
        print.addActionListener(e -> { try { billArea.print(); } catch (Exception ex) { showError(ex); } });
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(billArea), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT)); south.add(print);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    // ---------- Module 5: recharge history ----------
    private JPanel historyTab() {
        JButton show = new JButton("Show bill for selected");
        show.addActionListener(e -> {
            int row = histTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a recharge in the table first."); return; }
            billArea.setText(history.get(row).bill());
            tabs.setSelectedIndex(3);
        });
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(histTable), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT)); south.add(show);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    // ---------- data refresh ----------
    private void refreshAll() { refreshSubscribers(); refreshPlans(); refreshHistory(); }

    private void refreshSubscribers() {
        try {
            subList = svc.subscribers();
            subModel.setRowCount(0);
            for (Subscriber s : subList)
                subModel.addRow(new Object[]{s.getName(), s.getMobile(), s.getType(), s.status(), Recharge.fmt(s.getExpiry())});
        } catch (SQLException ex) { showError(ex); }
    }

    private void refreshPlans() {
        try {
            planModel.setRowCount(0);
            planBox.removeAllItems();
            for (Plan p : svc.plans()) {
                planModel.addRow(new Object[]{p.getName(), p.getType(), String.format("%.2f", p.cost()), p.describe()});
                planBox.addItem(p);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void refreshHistory() {
        try {
            history = svc.history();
            histModel.setRowCount(0);
            for (Recharge r : history)
                histModel.addRow(new Object[]{"PC-" + String.format("%05d", r.getId()), Recharge.fmt(r.getDate()),
                        r.getMobile(), r.getPlanName(), String.format("%.2f", r.getAmount())});
        } catch (SQLException ex) { showError(ex); }
    }

    private void showError(Exception ex) {
        String msg = ex instanceof InvalidNumberException ? "Invalid number: " + ex.getMessage()
                   : ex instanceof SQLException ? "Database error: " + ex.getMessage() + "\nCheck Db.java and that MySQL is running."
                   : ex.getMessage();
        JOptionPane.showMessageDialog(this, msg, "PaisaCharge", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- green money theme, dark and light ----------
    private void applyTheme() {
        paint(getContentPane());
        Color bar = dark ? new Color(11, 42, 25) : new Color(11, 122, 68);
        header.setBackground(bar);
        title.setForeground(Color.WHITE);
        status.setForeground(dark ? new Color(138, 174, 151) : new Color(88, 114, 95));
        repaint();
    }

    private void paint(Component c) {
        Color bg = dark ? new Color(6, 18, 12) : new Color(238, 247, 241);
        Color card = dark ? new Color(13, 33, 22) : Color.WHITE;
        Color fg = dark ? new Color(228, 244, 234) : new Color(16, 36, 26);
        Color pri = dark ? new Color(46, 204, 113) : new Color(15, 157, 88);
        Color onPri = dark ? new Color(4, 21, 11) : Color.WHITE;
        if (c instanceof JButton) { c.setBackground(pri); c.setForeground(onPri); }
        else if (c instanceof JTextComponent || c instanceof JComboBox || c instanceof JTable) { c.setBackground(card); c.setForeground(fg); }
        else { c.setBackground(bg); c.setForeground(fg); }
        if (c instanceof JTable t) {
            t.setGridColor(dark ? new Color(26, 58, 40) : new Color(207, 227, 214));
            t.setRowHeight(24);
            t.setSelectionBackground(pri); t.setSelectionForeground(onPri);
            t.getTableHeader().setBackground(pri); t.getTableHeader().setForeground(onPri);
        }
        if (c instanceof JTextComponent tc) tc.setCaretColor(fg);
        if (c instanceof JScrollPane sp) sp.getViewport().setBackground(card);
        if (c instanceof Container ct) for (Component k : ct.getComponents()) paint(k);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) { }
            new MainFrame().setVisible(true);
        });
    }
}

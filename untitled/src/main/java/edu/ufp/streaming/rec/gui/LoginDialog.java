package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.managers.StreamingDatabase;
import edu.ufp.streaming.rec.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;

/**
 * Janela de entrada da plataforma de streaming.
 *
 * <p>Apresenta dois modos num único ecrã:
 * <ul>
 *   <li><b>Entrar</b> — autenticação com ID + password existentes.</li>
 *   <li><b>Criar Conta</b> — registo de novo utilizador com password imediata.</li>
 * </ul>
 *
 * @author Diogo Vicente
 */
public class LoginDialog extends JDialog {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(10, 10, 18);
    private static final Color PANEL_BG  = new Color(18, 18, 30);
    private static final Color ACCENT    = new Color(229, 57, 53);   // vermelho streaming
    private static final Color ACCENT2   = new Color(255, 109, 0);   // laranja
    private static final Color FG        = new Color(240, 240, 245);
    private static final Color FG_DIM    = new Color(140, 140, 160);
    private static final Color FIELD_BG  = new Color(28, 28, 42);
    private static final Color FIELD_BD  = new Color(50, 50, 70);
    private static final Color TAB_SEL   = new Color(229, 57, 53);
    private static final Color TAB_UNS   = new Color(35, 35, 52);
    private static final Color SUCCESS   = new Color(46, 204, 113);
    private static final Color ERR_CLR   = new Color(255, 80, 80);

    // ── Estado ────────────────────────────────────────────────────────────────
    private final StreamingDatabase db;
    private boolean authenticated = false;
    private User    loggedUser    = null;

    // ── Componentes partilhados ───────────────────────────────────────────────
    private JPanel     cardPanel;
    private CardLayout cards;
    private JLabel     tabLogin, tabRegisto;

    // Campos Login
    private StyledField     loginId;
    private StyledPassword  loginPwd;
    private JLabel          loginMsg;

    // Campos Registo
    private StyledField     regId, regNome, regEmail, regRegiao;
    private StyledPassword  regPwd, regConfirm;
    private JLabel          regMsg;

    public LoginDialog(Frame parent, StreamingDatabase db) {
        super(parent, "StreamingApp", true);
        this.db = db;
        setUndecorated(true);
        buildUI();
        pack();
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        setLocationRelativeTo(parent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Construção UI
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Glow accent no topo
                GradientPaint gp = new GradientPaint(0, 0, new Color(229, 57, 53, 60),
                        getWidth(), 0, new Color(255, 109, 0, 30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 6, 3, 3);
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 32, 16, 32));

        JLabel logo = new JLabel("▶ StreamingApp");
        logo.setFont(new Font("Georgia", Font.BOLD, 22));
        logo.setForeground(FG);
        header.add(logo, BorderLayout.WEST);

        // Botão fechar (X)
        JLabel btnClose = new JLabel("✕");
        btnClose.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        btnClose.setForeground(FG_DIM);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dispose(); }
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(ACCENT); }
            public void mouseExited(MouseEvent e)  { btnClose.setForeground(FG_DIM); }
        });
        header.add(btnClose, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ── Tabs ──────────────────────────────────────────────────────────────
        JPanel tabBar = new JPanel(new GridLayout(1, 2, 0, 0));
        tabBar.setOpaque(false);
        tabBar.setBorder(new EmptyBorder(0, 32, 0, 32));

        tabLogin   = makeTab("Entrar", true);
        tabRegisto = makeTab("Criar Conta", false);

        tabLogin.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTab(true); }
        });
        tabRegisto.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { switchTab(false); }
        });

        tabBar.add(tabLogin);
        tabBar.add(tabRegisto);
        root.add(tabBar, BorderLayout.CENTER);  // temporário, vamos reorganizar

        // ── Cards ─────────────────────────────────────────────────────────────
        cards     = new CardLayout();
        cardPanel = new JPanel(cards);
        cardPanel.setOpaque(false);
        cardPanel.add(buildLoginPanel(),   "login");
        cardPanel.add(buildRegistoPanel(), "registo");

        // Montar centro: tabs + cards
        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(0, 32, 28, 32));
        centro.add(tabBar,    BorderLayout.NORTH);
        centro.add(cardPanel, BorderLayout.CENTER);

        root.remove(tabBar);   // remover o temporário
        root.add(centro, BorderLayout.CENTER);

        // Arrastar a janela pelo header
        addDragSupport(header);
        addDragSupport(logo);

        // ESC fecha
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        root.getActionMap().put("close", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });

        setContentPane(root);
        setPreferredSize(new Dimension(440, 560));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Painel Login
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        loginId  = new StyledField("ID de utilizador", false);
        loginPwd = new StyledPassword("Password");
        loginMsg = makeMsg();

        JButton btnEntrar = makeButton("Entrar", true);
        btnEntrar.addActionListener(e -> onEntrar());

        // Enter no campo password → entrar
        loginPwd.field.addActionListener(e -> onEntrar());
        loginId.field.addActionListener(e -> loginPwd.field.requestFocus());

        p.add(loginId);
        p.add(Box.createVerticalStrut(10));
        p.add(loginPwd);
        p.add(Box.createVerticalStrut(6));
        p.add(loginMsg);
        p.add(Box.createVerticalStrut(18));
        p.add(btnEntrar);
        p.add(Box.createVerticalStrut(14));
        p.add(makeSeparador("Não tens conta?"));
        p.add(Box.createVerticalStrut(12));

        JButton btnIrRegisto = makeButton("Criar nova conta", false);
        btnIrRegisto.addActionListener(e -> switchTab(false));
        p.add(btnIrRegisto);

        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Painel Registo
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildRegistoPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 0, 0, 0));

        regId      = new StyledField("ID único (ex: u5)", false);
        regNome    = new StyledField("Nome completo", false);
        regEmail   = new StyledField("E-mail", false);
        regRegiao  = new StyledField("Região (ex: PT)", false);
        regPwd     = new StyledPassword("Password");
        regConfirm = new StyledPassword("Confirmar password");
        regMsg     = makeMsg();

        JButton btnCriar = makeButton("Criar Conta", true);
        btnCriar.addActionListener(e -> onCriarConta());
        regConfirm.field.addActionListener(e -> onCriarConta());

        p.add(rowDuplo(regId, regRegiao));
        p.add(Box.createVerticalStrut(8));
        p.add(regNome);
        p.add(Box.createVerticalStrut(8));
        p.add(regEmail);
        p.add(Box.createVerticalStrut(8));
        p.add(regPwd);
        p.add(Box.createVerticalStrut(8));
        p.add(regConfirm);
        p.add(Box.createVerticalStrut(6));
        p.add(regMsg);
        p.add(Box.createVerticalStrut(14));
        p.add(btnCriar);
        p.add(Box.createVerticalStrut(12));
        p.add(makeSeparador("Já tens conta?"));
        p.add(Box.createVerticalStrut(10));

        JButton btnIrLogin = makeButton("Entrar na minha conta", false);
        btnIrLogin.addActionListener(e -> switchTab(true));
        p.add(btnIrLogin);

        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Handlers
    // ─────────────────────────────────────────────────────────────────────────

    private void onEntrar() {
        String id  = loginId.getText();
        String pwd = loginPwd.getText();

        if (id.isEmpty()) { loginMsg.setForeground(ERR_CLR); loginMsg.setText("Introduz o teu ID."); return; }
        if (pwd.isEmpty()) { loginMsg.setForeground(ERR_CLR); loginMsg.setText("Introduz a tua password."); return; }

        User user = db.users().get(id);
        if (user == null) {
            loginMsg.setForeground(ERR_CLR);
            loginMsg.setText("ID ou password incorretos.");
            loginPwd.clear();
            return;
        }

        // Utilizador sem password ainda definida
        if (!user.hasPassword()) {
            loginMsg.setForeground(new Color(255, 180, 0));
            loginMsg.setText("Sem password definida. Cria uma conta primeiro.");
            return;
        }

        User auth = db.authenticate(id, pwd);
        if (auth == null) {
            loginMsg.setForeground(ERR_CLR);
            loginMsg.setText("ID ou password incorretos.");
            loginPwd.clear();
            return;
        }

        loginMsg.setForeground(SUCCESS);
        loginMsg.setText("Bem-vindo(a), " + auth.getName() + "!");
        authenticated = true;
        loggedUser    = auth;

        // Pequena pausa visual antes de fechar
        Timer t = new Timer(600, ev -> dispose());
        t.setRepeats(false);
        t.start();
    }

    private void onCriarConta() {
        String id      = regId.getText();
        String nome    = regNome.getText();
        String email   = regEmail.getText();
        String regiao  = regRegiao.getText();
        String pwd     = regPwd.getText();
        String confirm = regConfirm.getText();

        if (id.isEmpty() || nome.isEmpty()) {
            regMsg.setForeground(ERR_CLR);
            regMsg.setText("ID e Nome são obrigatórios.");
            return;
        }
        if (pwd.isEmpty()) {
            regMsg.setForeground(ERR_CLR);
            regMsg.setText("Define uma password.");
            return;
        }
        if (!pwd.equals(confirm)) {
            regMsg.setForeground(ERR_CLR);
            regMsg.setText("As passwords não coincidem.");
            regConfirm.clear();
            return;
        }
        if (db.users().contains(id)) {
            regMsg.setForeground(ERR_CLR);
            regMsg.setText("O ID \"" + id + "\" já existe.");
            return;
        }
        if (!email.isEmpty()) {
            for (User u : db.users().listAll()) {
                if (u.getEmail().equalsIgnoreCase(email)) {
                    regMsg.setForeground(ERR_CLR);
                    regMsg.setText("E-mail já registado por outro utilizador.");
                    return;
                }
            }
        }

        String regiaoFinal = regiao.isEmpty() ? "PT" : regiao.toUpperCase();
        User novo = new User(id, nome, email, regiaoFinal, LocalDate.now(), pwd);
        db.addUser(novo);

        regMsg.setForeground(SUCCESS);
        regMsg.setText("Conta criada! A entrar...");

        authenticated = true;
        loggedUser    = novo;

        Timer t = new Timer(700, ev -> dispose());
        t.setRepeats(false);
        t.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de UI
    // ─────────────────────────────────────────────────────────────────────────

    private void switchTab(boolean login) {
        tabLogin.setBackground(  login  ? TAB_SEL : TAB_UNS);
        tabRegisto.setBackground(!login ? TAB_SEL : TAB_UNS);
        tabLogin.setForeground(  login  ? Color.WHITE : FG_DIM);
        tabRegisto.setForeground(!login ? Color.WHITE : FG_DIM);
        cards.show(cardPanel, login ? "login" : "registo");
        if (login) loginId.field.requestFocus();
        else       regId.field.requestFocus();
    }

    private JLabel makeTab(String text, boolean selected) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(selected ? TAB_SEL : TAB_UNS);
        l.setForeground(selected ? Color.WHITE : FG_DIM);
        l.setFont(new Font("Georgia", Font.BOLD, 13));
        l.setBorder(new EmptyBorder(10, 0, 10, 0));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    private JButton makeButton(String text, boolean primary) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (primary) {
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT2);
                    g2.setPaint(gp);
                } else {
                    g2.setColor(FIELD_BG);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(primary ? Color.WHITE : FG_DIM);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        b.setFont(new Font("Georgia", Font.BOLD, 13));
        b.setForeground(primary ? Color.WHITE : FG_DIM);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setPreferredSize(new Dimension(200, 42));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel makeMsg() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        l.setForeground(ERR_CLR);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel makeSeparador(String texto) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(FIELD_BD);
        gc.weightx = 1; gc.gridx = 0;
        p.add(sep1, gc);

        JLabel lbl = new JLabel(" " + texto + " ");
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        lbl.setForeground(FG_DIM);
        gc.weightx = 0; gc.gridx = 1;
        p.add(lbl, gc);

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(FIELD_BD);
        gc.weightx = 1; gc.gridx = 2;
        p.add(sep2, gc);

        return p;
    }

    /** Coloca dois campos lado a lado numa linha. */
    private JPanel rowDuplo(StyledField a, StyledField b) {
        JPanel row = new JPanel(new GridLayout(1, 2, 8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        row.add(a);
        row.add(b);
        return row;
    }

    private void addDragSupport(Component c) {
        final Point[] origin = {null};
        c.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { origin[0] = e.getPoint(); }
        });
        c.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (origin[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - origin[0].x,
                        loc.y + e.getY() - origin[0].y);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Componentes estilizados internos
    // ─────────────────────────────────────────────────────────────────────────

    /** Campo de texto com placeholder e estilo dark. */
    private class StyledField extends JPanel {
        final JTextField field;

        StyledField(String placeholder, boolean readOnly) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            field = new JTextField() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(FIELD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    super.paintComponent(g);
                    if (getText().isEmpty() && !isFocusOwner()) {
                        g2.setColor(FG_DIM);
                        g2.setFont(getFont().deriveFont(Font.PLAIN));
                        g2.drawString(placeholder, 12, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 1);
                    }
                    g2.dispose();
                }
            };
            field.setOpaque(false);
            field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(FIELD_BD, 1, true),
                    new EmptyBorder(0, 12, 0, 12)));
            field.setForeground(FG);
            field.setCaretColor(ACCENT);
            field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            field.setPreferredSize(new Dimension(0, 44));
            field.setEditable(!readOnly);

            // Highlight no foco
            field.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(ACCENT, 1, true), new EmptyBorder(0, 12, 0, 12)));
                }
                public void focusLost(FocusEvent e) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(FIELD_BD, 1, true), new EmptyBorder(0, 12, 0, 12)));
                }
            });

            add(field, BorderLayout.CENTER);
        }

        String getText() { return field.getText().trim(); }
    }

    /** Campo de password com botão mostrar/ocultar. */
    private class StyledPassword extends JPanel {
        final JPasswordField field;
        private boolean visible = false;

        StyledPassword(String placeholder) {
            setLayout(new BorderLayout(0, 0));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel wrapper = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(FIELD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
            };
            wrapper.setOpaque(false);
            wrapper.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(FIELD_BD, 1, true), new EmptyBorder(0, 0, 0, 0)));
            wrapper.setPreferredSize(new Dimension(0, 44));

            field = new JPasswordField() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (getPassword().length == 0 && !isFocusOwner()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(FG_DIM);
                        g2.setFont(getFont().deriveFont(Font.PLAIN));
                        g2.drawString(placeholder, 2, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 1);
                        g2.dispose();
                    }
                }
            };
            field.setOpaque(false);
            field.setBorder(new EmptyBorder(0, 12, 0, 4));
            field.setForeground(FG);
            field.setCaretColor(ACCENT);
            field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            field.setEchoChar('●');

            // Botão olho
            JLabel eye = new JLabel("◉");
            eye.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            eye.setForeground(FG_DIM);
            eye.setBorder(new EmptyBorder(0, 4, 0, 10));
            eye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eye.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    visible = !visible;
                    field.setEchoChar(visible ? (char) 0 : '●');
                    eye.setForeground(visible ? ACCENT : FG_DIM);
                }
            });

            // Highlight no foco
            field.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    wrapper.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(ACCENT, 1, true), new EmptyBorder(0, 0, 0, 0)));
                }
                public void focusLost(FocusEvent e) {
                    wrapper.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(FIELD_BD, 1, true), new EmptyBorder(0, 0, 0, 0)));
                }
            });

            wrapper.add(field, BorderLayout.CENTER);
            wrapper.add(eye,   BorderLayout.EAST);
            add(wrapper, BorderLayout.CENTER);
        }

        String getText() { return new String(field.getPassword()); }
        void clear()     { field.setText(""); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API pública
    // ─────────────────────────────────────────────────────────────────────────

    /** @return {@code true} se autenticado com sucesso */
    public boolean isAuthenticated() { return authenticated; }

    /** @return o {@link User} autenticado, ou {@code null} */
    public User getLoggedUser() { return loggedUser; }
}
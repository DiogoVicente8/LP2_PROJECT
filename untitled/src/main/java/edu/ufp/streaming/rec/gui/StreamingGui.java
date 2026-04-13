package edu.ufp.streaming.rec.gui;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.enums.InterationType;
import edu.ufp.streaming.rec.managers.*;
import edu.ufp.streaming.rec.models.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface gráfica da plataforma de streaming (R9).
 * Permite gerir utilizadores, artistas, conteúdos e explorar o grafo.
 *
 * @author Diogo Vicente & Pedro
 */
public class StreamingGUI    extends JFrame {

    private final StreamingDatabase db;

    // Painel principal com separadores
    private JTabbedPane tabbedPane;

    // -----------------------------------------------------------------------
    // Construtor
    // -----------------------------------------------------------------------

    public StreamingGUI() {
        super("Plataforma de Streaming — LP2/AED2");
        this.db = buildSampleDB();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("👤 Utilizadores",  buildUtilizadoresPanel());
        tabbedPane.addTab("🎬 Conteúdos",     buildConteudosPanel());
        tabbedPane.addTab("🎭 Artistas",      buildArtistasPanel());
        tabbedPane.addTab("🔗 Grafo / R8",    buildGrafoPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // -----------------------------------------------------------------------
    // Painel Utilizadores
    // -----------------------------------------------------------------------

    private JPanel buildUtilizadoresPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Tabela
        String[] cols = {"ID", "Nome", "Email", "Região", "Data Registo"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        refreshUserTable(model);

        // Formulário de inserção
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(new TitledBorder("Inserir Utilizador"));

        JTextField fId     = new JTextField();
        JTextField fNome   = new JTextField();
        JTextField fEmail  = new JTextField();
        JTextField fRegiao = new JTextField("PT");

        form.add(new JLabel("ID:")); form.add(fId);
        form.add(new JLabel("Nome:")); form.add(fNome);
        form.add(new JLabel("Email:")); form.add(fEmail);
        form.add(new JLabel("Região:")); form.add(fRegiao);

        JButton btnAdd = new JButton("Adicionar");
        JButton btnRemove = new JButton("Remover Selecionado");

        btnAdd.addActionListener(e -> {
            String id = fId.getText().trim();
            String nome = fNome.getText().trim();
            String email = fEmail.getText().trim();
            String regiao = fRegiao.getText().trim();

            if (id.isEmpty() || nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID e Nome são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User u = new User(id, nome, email, regiao, LocalDate.now());
            if (db.addUser(u)) {
                refreshUserTable(model);
                fId.setText(""); fNome.setText(""); fEmail.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "ID já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRemove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um utilizador."); return; }
            String id = (String) model.getValueAt(row, 0);
            db.removeUser(id);
            refreshUserTable(model);
        });

        // Pesquisa
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.setBorder(new TitledBorder("Pesquisar por Nome"));
        JTextField fSearch = new JTextField(15);
        JButton btnSearch = new JButton("Pesquisar");
        btnSearch.addActionListener(e -> {
            String q = fSearch.getText().trim();
            model.setRowCount(0);
            List<User> found = db.users().searchByNameSubstring(q);
            for (User u : found)
                model.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(), u.getRegion(), u.getRegisterDate()});
        });
        JButton btnAll = new JButton("Todos");
        btnAll.addActionListener(e -> refreshUserTable(model));
        search.add(fSearch); search.add(btnSearch); search.add(btnAll);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout());
        btns.add(btnAdd); btns.add(btnRemove);
        south.add(btns, BorderLayout.SOUTH);
        south.add(search, BorderLayout.NORTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshUserTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (User u : db.users().listAll())
            model.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(), u.getRegion(), u.getRegisterDate()});
    }

    // -----------------------------------------------------------------------
    // Painel Conteúdos
    // -----------------------------------------------------------------------

    private JPanel buildConteudosPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"ID", "Tipo", "Título", "Género", "Data Lançamento", "Duração", "Rating"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        refreshContentTable(model);

        // Formulário de inserção
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(new TitledBorder("Inserir Conteúdo"));

        JTextField fId     = new JTextField();
        JTextField fTitulo = new JTextField();
        JTextField fGenre  = new JTextField();
        JTextField fData   = new JTextField("2024-01-01");
        JTextField fDuracao= new JTextField("120");
        JTextField fRegiao = new JTextField("PT");
        String[] tipos = {"Filme", "Série", "Documentário"};
        JComboBox<String> cbTipo = new JComboBox<>(tipos);

        form.add(new JLabel("Tipo:")); form.add(cbTipo);
        form.add(new JLabel("ID:")); form.add(fId);
        form.add(new JLabel("Título:")); form.add(fTitulo);
        form.add(new JLabel("ID Género:")); form.add(fGenre);
        form.add(new JLabel("Data (yyyy-MM-dd):")); form.add(fData);
        form.add(new JLabel("Duração (min):")); form.add(fDuracao);
        form.add(new JLabel("Região:")); form.add(fRegiao);

        JButton btnAdd = new JButton("Adicionar");
        JButton btnRemove = new JButton("Remover Selecionado");

        btnAdd.addActionListener(e -> {
            String id = fId.getText().trim();
            String titulo = fTitulo.getText().trim();
            String genreId = fGenre.getText().trim();
            Genre genre = db.genres().get(genreId);
            if (genre == null) {
                JOptionPane.showMessageDialog(this, "Género não encontrado: " + genreId, "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                LocalDate data = LocalDate.parse(fData.getText().trim());
                int dur = Integer.parseInt(fDuracao.getText().trim());
                String reg = fRegiao.getText().trim();
                Content c = switch ((String) cbTipo.getSelectedItem()) {
                    case "Série" -> new Series(id, titulo, genre, data, dur, reg, 1);
                    case "Documentário" -> new Documentary(id, titulo, genre, data, dur, reg, "", "");
                    default -> new Movie(id, titulo, genre, data, dur, reg, null);
                };
                if (db.addContent(c)) {
                    refreshContentTable(model);
                } else {
                    JOptionPane.showMessageDialog(this, "ID já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dados inválidos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRemove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um conteúdo."); return; }
            String id = (String) model.getValueAt(row, 0);
            db.removeContent(id);
            refreshContentTable(model);
        });

        JPanel btns = new JPanel(new FlowLayout());
        btns.add(btnAdd); btns.add(btnRemove);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(btns, BorderLayout.SOUTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshContentTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Content c : db.contents().listAll()) {
            String tipo = c instanceof Movie ? "Filme" : c instanceof Series ? "Série" : "Documentário";
            model.addRow(new Object[]{c.getId(), tipo, c.getTitle(), c.getGenre().getName(),
                    c.getReleaseDate(), c.getDuration(), c.getRating()});
        }
    }

    // -----------------------------------------------------------------------
    // Painel Artistas
    // -----------------------------------------------------------------------

    private JPanel buildArtistasPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"ID", "Nome", "Nacionalidade", "Género", "Data Nasc.", "Papel"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        refreshArtistTable(model);

        // Formulário
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(new TitledBorder("Inserir Artista"));

        JTextField fId     = new JTextField();
        JTextField fNome   = new JTextField();
        JTextField fNac    = new JTextField("PT");
        JTextField fGen    = new JTextField("M");
        JTextField fData   = new JTextField("1980-01-01");
        JComboBox<ArtistRole> cbRole = new JComboBox<>(ArtistRole.values());

        form.add(new JLabel("ID:")); form.add(fId);
        form.add(new JLabel("Nome:")); form.add(fNome);
        form.add(new JLabel("Nacionalidade:")); form.add(fNac);
        form.add(new JLabel("Género (M/F):")); form.add(fGen);
        form.add(new JLabel("Data Nasc. (yyyy-MM-dd):")); form.add(fData);
        form.add(new JLabel("Papel:")); form.add(cbRole);

        JButton btnAdd = new JButton("Adicionar");
        JButton btnRemove = new JButton("Remover Selecionado");

        btnAdd.addActionListener(e -> {
            try {
                Artist a = new Artist(fId.getText().trim(), fNome.getText().trim(),
                        fNac.getText().trim(), fGen.getText().trim(),
                        LocalDate.parse(fData.getText().trim()),
                        (ArtistRole) cbRole.getSelectedItem());
                if (db.addArtist(a)) refreshArtistTable(model);
                else JOptionPane.showMessageDialog(this, "ID já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dados inválidos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRemove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um artista."); return; }
            db.removeArtist((String) model.getValueAt(row, 0));
            refreshArtistTable(model);
        });

        JPanel btns = new JPanel(new FlowLayout());
        btns.add(btnAdd); btns.add(btnRemove);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(btns, BorderLayout.SOUTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshArtistTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Artist a : db.artists().listAll())
            model.addRow(new Object[]{a.getId(), a.getName(), a.getNationality(),
                    a.getGender(), a.getBirthDate(), a.getRole()});
    }

    // -----------------------------------------------------------------------
    // Painel Grafo / R8
    // -----------------------------------------------------------------------

    private JPanel buildGrafoPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        // --- R8a: Caminho mais curto ---
        JPanel r8a = new JPanel(new FlowLayout(FlowLayout.LEFT));
        r8a.setBorder(new TitledBorder("R8a — Caminho mais curto entre utilizadores"));
        JTextField fOrigem  = new JTextField(6);
        JTextField fDestino = new JTextField(6);
        JButton btnCaminho  = new JButton("Calcular");
        btnCaminho.addActionListener(e -> {
            String orig = fOrigem.getText().trim();
            String dest = fDestino.getText().trim();
            List<String> caminho = db.getGraph().caminhoMaisCurtoBetweenUsers(orig, dest);
            double peso = db.getGraph().pesoCaminhoMaisCurto(orig, dest);
            if (caminho.isEmpty())
                output.setText("[R8a] Sem caminho de " + orig + " para " + dest);
            else
                output.setText("[R8a] Caminho " + orig + "→" + dest + ": " + caminho
                        + "\n      Peso total: " + String.format("%.2f", peso));
        });
        r8a.add(new JLabel("Origem:")); r8a.add(fOrigem);
        r8a.add(new JLabel("Destino:")); r8a.add(fDestino);
        r8a.add(btnCaminho);

        // --- R8c: Grafo conexo ---
        JPanel r8c = new JPanel(new FlowLayout(FlowLayout.LEFT));
        r8c.setBorder(new TitledBorder("R8c — Grafo de utilizadores é fortemente conexo?"));
        JButton btnConexo = new JButton("Verificar");
        btnConexo.addActionListener(e -> {
            boolean conexo = db.getGraph().isGrafoUtilizadoresConexo();
            output.setText("[R8c] O grafo de utilizadores é fortemente conexo: " + (conexo ? "SIM ✓" : "NÃO ✗"));
        });
        r8c.add(btnConexo);

        // --- R8d: Recomendações ---
        JPanel r8d = new JPanel(new FlowLayout(FlowLayout.LEFT));
        r8d.setBorder(new TitledBorder("R8d — Recomendações por proximidade"));
        JTextField fUserRec = new JTextField(6);
        JButton btnRec = new JButton("Recomendar");
        btnRec.addActionListener(e -> {
            String uid = fUserRec.getText().trim();
            List<Content> recom = db.getGraph().recomendarConteudosPorProximidade(uid, db.follows(), db.users());
            if (recom.isEmpty())
                output.setText("[R8d] Sem recomendações para " + uid);
            else {
                StringBuilder sb = new StringBuilder("[R8d] Recomendações para " + uid + ":\n");
                for (Content c : recom) sb.append("  • ").append(c.getTitle()).append(" (").append(c.getGenre().getName()).append(")\n");
                output.setText(sb.toString());
            }
        });
        r8d.add(new JLabel("User ID:")); r8d.add(fUserRec); r8d.add(btnRec);

        // --- R8e: Estatísticas ---
        JPanel r8e = new JPanel(new FlowLayout(FlowLayout.LEFT));
        r8e.setBorder(new TitledBorder("R8e — Estatísticas de visualização (2024)"));
        JTextField fContentStat = new JTextField(6);
        JButton btnStat = new JButton("Ver estatísticas");
        btnStat.addActionListener(e -> {
            String cid = fContentStat.getText().trim();
            Map<String, Double> stats = db.getGraph().estatisticasVisualizacao(
                    cid,
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 12, 31, 23, 59),
                    db.users());
            output.setText(String.format("[R8e] Estatísticas de %s em 2024:%n" +
                            "  Visualizações : %.0f%n" +
                            "  Progresso médio: %.2f%n" +
                            "  Rating médio  : %.2f",
                    cid, stats.get("visualizacoes"), stats.get("progressoMedio"), stats.get("ratingMedio")));
        });
        r8e.add(new JLabel("Content ID:")); r8e.add(fContentStat); r8e.add(btnStat);

        // --- R8f: Utilizadores que viram séries de um género ---
        JPanel r8f = new JPanel(new FlowLayout(FlowLayout.LEFT));
        r8f.setBorder(new TitledBorder("R8f — Utilizadores que viram séries de um género (2024)"));
        JTextField fGenreId = new JTextField(6);
        JButton btnGenre = new JButton("Pesquisar");
        btnGenre.addActionListener(e -> {
            String gid = fGenreId.getText().trim();
            List<User> users = db.getGraph().utilizadoresQueViramSeriesDeGenero(
                    gid,
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 12, 31, 23, 59),
                    db.users(), db.contents());
            if (users.isEmpty())
                output.setText("[R8f] Nenhum utilizador viu séries do género " + gid + " em 2024.");
            else {
                StringBuilder sb = new StringBuilder("[R8f] Utilizadores que viram séries de género " + gid + " em 2024:\n");
                for (User u : users) sb.append("  • ").append(u.getName()).append(" (").append(u.getRegion()).append(")\n");
                output.setText(sb.toString());
            }
        });
        r8f.add(new JLabel("Genre ID:")); r8f.add(fGenreId); r8f.add(btnGenre);

        // --- R8g: Seguidores que viram conteúdo ---
        JPanel r8g = new JPanel(new FlowLayout(FlowLayout.LEFT));
        r8g.setBorder(new TitledBorder("R8g — Seguidores que viram um conteúdo (2024)"));
        JTextField fUserG = new JTextField(6);
        JTextField fContG = new JTextField(6);
        JButton btnG = new JButton("Pesquisar");
        btnG.addActionListener(e -> {
            String uid = fUserG.getText().trim();
            String cid = fContG.getText().trim();
            List<User> result = db.getGraph().seguidoresQueViramConteudo(
                    uid, cid,
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    LocalDateTime.of(2024, 12, 31, 23, 59),
                    db.follows(), db.users());
            if (result.isEmpty())
                output.setText("[R8g] Nenhum seguidor de " + uid + " viu " + cid + " em 2024.");
            else {
                StringBuilder sb = new StringBuilder("[R8g] Seguidores de " + uid + " que viram " + cid + ":\n");
                for (User u : result) sb.append("  • ").append(u.getName()).append("\n");
                output.setText(sb.toString());
            }
        });
        r8g.add(new JLabel("User ID:")); r8g.add(fUserG);
        r8g.add(new JLabel("Content ID:")); r8g.add(fContG);
        r8g.add(btnG);

        // Layout dos painéis de controlo
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(r8a); controls.add(r8c); controls.add(r8d);
        controls.add(r8e); controls.add(r8f); controls.add(r8g);

        // Info grafo
        JButton btnInfo = new JButton("ℹ️ Info do Grafo");
        btnInfo.addActionListener(e -> output.setText(
                "[GRAFO] Vértices: " + db.getGraph().totalVertices()
                        + " | Arestas: " + db.getGraph().totalArestas()));
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(btnInfo);
        controls.add(infoPanel);

        panel.add(new JScrollPane(controls), BorderLayout.WEST);
        panel.add(new JScrollPane(output),   BorderLayout.CENTER);
        return panel;
    }

    // -----------------------------------------------------------------------
    // Status bar
    // -----------------------------------------------------------------------

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.add(new JLabel("Plataforma de Streaming | LP2/AED2 2025/26 | Utilizadores: "
                + db.users().size() + " | Conteúdos: " + db.contents().size()));
        return bar;
    }

    // -----------------------------------------------------------------------
    // Base de dados de exemplo (dados pré-carregados)
    // -----------------------------------------------------------------------

    private StreamingDatabase buildSampleDB() {
        StreamingDatabase sdb = new StreamingDatabase();

        // Géneros
        Genre gAcao  = new Genre("g1", "Acao");
        Genre gDrama = new Genre("g2", "Drama");
        Genre gSci   = new Genre("g3", "Sci-Fi");
        sdb.addGenre(gAcao); sdb.addGenre(gDrama); sdb.addGenre(gSci);

        // Utilizadores
        User u1 = new User("u1", "Alice Silva",  "alice@mail.com",  "PT", LocalDate.of(2020, 1, 10));
        User u2 = new User("u2", "Bruno Costa",  "bruno@mail.com",  "PT", LocalDate.of(2020, 3, 15));
        User u3 = new User("u3", "Carla Pereira","carla@mail.com",  "BR", LocalDate.of(2021, 6, 20));
        User u4 = new User("u4", "David Alves",  "david@mail.com",  "US", LocalDate.of(2022, 9,  5));
        sdb.addUser(u1); sdb.addUser(u2); sdb.addUser(u3); sdb.addUser(u4);

        // Conteúdos
        Movie  m1 = new Movie ("c1", "Inception",    gAcao,  LocalDate.of(2010, 7, 16), 148, "PT", null);
        Movie  m2 = new Movie ("c2", "Interstellar", gSci,   LocalDate.of(2014,11,  7), 169, "PT", null);
        Series s1 = new Series("c3", "Breaking Bad", gDrama, LocalDate.of(2008, 1, 20),  45, "PT", 5);
        Series s2 = new Series("c4", "Dark",         gAcao,  LocalDate.of(2017,12,  1),  60, "BR", 3);
        sdb.addContent(m1); sdb.addContent(m2); sdb.addContent(s1); sdb.addContent(s2);

        // Artistas
        Artist a1 = new Artist("a1", "Christopher Nolan", "GB", "M", LocalDate.of(1970, 7, 30), ArtistRole.DIRECTOR);
        Artist a2 = new Artist("a2", "Leonardo DiCaprio", "US", "M", LocalDate.of(1974,11, 11), ArtistRole.ACTOR);
        sdb.addArtist(a1); sdb.addArtist(a2);
        sdb.addParticipation("a1", "c1", ArtistRole.DIRECTOR, LocalDate.of(2010, 7, 16));
        sdb.addParticipation("a2", "c1", ArtistRole.ACTOR,    LocalDate.of(2010, 7, 16));

        // Follows
        sdb.addFollow("u1", "u2");
        sdb.addFollow("u2", "u3");
        sdb.addFollow("u3", "u4");

        // Interações
        sdb.addInteraction(new Interation(u1, m1, LocalDateTime.of(2024,1,10,20,0), 0,   0.9, InterationType.WATCH, "i1"));
        sdb.addInteraction(new Interation(u2, m1, LocalDateTime.of(2024,1,15,21,0), 0,   1.0, InterationType.WATCH, "i2"));
        sdb.addInteraction(new Interation(u2, m1, LocalDateTime.of(2024,1,16,10,0), 4.5, 1.0, InterationType.RATE,  "i3"));
        sdb.addInteraction(new Interation(u2, s1, LocalDateTime.of(2024,2, 1,19,0), 0,   0.5, InterationType.WATCH, "i4"));
        sdb.addInteraction(new Interation(u3, s2, LocalDateTime.of(2024,3, 5,20,0), 0,   0.8, InterationType.WATCH, "i5"));
        sdb.addInteraction(new Interation(u4, s1, LocalDateTime.of(2024,3,10,22,0), 0,   0.6, InterationType.WATCH, "i6"));

        return sdb;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StreamingGUI::new);
    }
}
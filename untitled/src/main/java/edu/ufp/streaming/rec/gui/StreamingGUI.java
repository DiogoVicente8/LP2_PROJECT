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

/**
 * Interface gráfica da plataforma de streaming (R9).
 * Permite gerir utilizadores, artistas, conteúdos e explorar o grafo.
 *
 * @author Diogo Vicente & Pedro
 */
public class StreamingGUI extends JFrame {

    private final StreamingDatabase db;
    private JTabbedPane tabbedPane;

    public StreamingGUI() {
        super("Plataforma de Streaming — LP2/AED2");
        this.db = buildSampleDB();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar()); // R10 / R11

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
    // Menu Bar (R10 / R11)
    // -----------------------------------------------------------------------

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFicheiro = new JMenu("Ficheiro");

        JMenuItem exportTxt = new JMenuItem("Exportar Conteúdos TXT (R10)");
        exportTxt.addActionListener(e -> {
            ContentFileManager.exportGenres(db.genres(), "genres.txt");
            ContentFileManager.exportContents(db.contents(), "contents.txt");
            JOptionPane.showMessageDialog(this, "Dados exportados para genres.txt e contents.txt");
        });

        JMenuItem importTxt = new JMenuItem("Importar Conteúdos TXT (R10)");
        importTxt.addActionListener(e -> {
            ContentFileManager.importGenres(db.genres(), "genres.txt");
            ContentFileManager.importContents(db.contents(), db.genres(), "contents.txt");
            JOptionPane.showMessageDialog(this, "Dados importados de genres.txt e contents.txt");
        });

        JMenuItem exportBin = new JMenuItem("Exportar Binário (R11)");
        exportBin.addActionListener(e -> {
            ContentSerializer.exportGenres(db.genres(), "genres.bin");
            ContentSerializer.exportContents(db.contents(), "contents.bin");
            JOptionPane.showMessageDialog(this, "Dados serializados para genres.bin e contents.bin");
        });

        JMenuItem importBin = new JMenuItem("Importar Binário (R11)");
        importBin.addActionListener(e -> {
            ContentSerializer.importGenres(db.genres(), "genres.bin");
            ContentSerializer.importContents(db.contents(), "contents.bin");
            JOptionPane.showMessageDialog(this, "Dados deserializados de genres.bin e contents.bin");
        });

        menuFicheiro.add(exportTxt);
        menuFicheiro.add(importTxt);
        menuFicheiro.addSeparator();
        menuFicheiro.add(exportBin);
        menuFicheiro.add(importBin);

        menuBar.add(menuFicheiro);
        return menuBar;
    }

    // -----------------------------------------------------------------------
    // Painel Utilizadores
    // -----------------------------------------------------------------------

    private JPanel buildUtilizadoresPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = {"ID", "Nome", "Email", "Região", "Data Registo"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        refreshUserTable(model);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(new TitledBorder("Inserir Utilizador"));

        JTextField fId     = new JTextField();
        JTextField fNome   = new JTextField();
        JTextField fEmail  = new JTextField();
        JTextField fRegiao = new JTextField("PT");

        form.add(new JLabel("ID:"));     form.add(fId);
        form.add(new JLabel("Nome:"));   form.add(fNome);
        form.add(new JLabel("Email:"));  form.add(fEmail);
        form.add(new JLabel("Região:")); form.add(fRegiao);

        JButton btnAdd    = new JButton("Adicionar");
        JButton btnRemove = new JButton("Remover Selecionado");

        btnAdd.addActionListener(e -> {
            String id     = fId.getText().trim();
            String nome   = fNome.getText().trim();
            String email  = fEmail.getText().trim();
            String regiao = fRegiao.getText().trim();

            if (id.isEmpty() || nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID e Nome são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email.isEmpty()) {
                for (User u : db.users().listAll()) {
                    if (u.getEmail().equalsIgnoreCase(email)) {
                        JOptionPane.showMessageDialog(this,
                                "O email '" + email + "' já está registado pelo utilizador '" + u.getName() + "'.",
                                "Email Duplicado", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
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

        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        editPanel.setBorder(new TitledBorder("Editar Utilizador Selecionado"));

        JButton btnEditEmail  = new JButton("Editar Email");
        JButton btnEditRegiao = new JButton("Editar Região");

        btnEditEmail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um utilizador."); return; }
            String id = (String) model.getValueAt(row, 0);
            String atual = (String) model.getValueAt(row, 2);
            String novoEmail = JOptionPane.showInputDialog(this, "Novo Email:", atual);
            if (novoEmail == null || novoEmail.trim().isEmpty()) return;
            for (User u : db.users().listAll()) {
                if (!u.getId().equals(id) && u.getEmail().equalsIgnoreCase(novoEmail.trim())) {
                    JOptionPane.showMessageDialog(this,
                            "Email já registado por '" + u.getName() + "'.",
                            "Email Duplicado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (db.users().editEmail(id, novoEmail.trim())) refreshUserTable(model);
        });

        btnEditRegiao.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um utilizador."); return; }
            String id = (String) model.getValueAt(row, 0);
            String atual = (String) model.getValueAt(row, 3);
            String novaRegiao = JOptionPane.showInputDialog(this, "Nova Região:", atual);
            if (novaRegiao == null || novaRegiao.trim().isEmpty()) return;
            if (db.users().editRegion(id, novaRegiao.trim())) refreshUserTable(model);
        });

        editPanel.add(btnEditEmail);
        editPanel.add(btnEditRegiao);

        JPanel followPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        followPanel.setBorder(new TitledBorder("Gestão de Follows"));

        JTextField fFollowId   = new JTextField(6);
        JButton btnFollow      = new JButton("Seguir");
        JButton btnUnfollow    = new JButton("Deixar de Seguir");
        JButton btnVerSeg      = new JButton("Ver Seguidores");
        JButton btnVerSeguindo = new JButton("Ver a Seguir");

        btnFollow.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona o utilizador que vai seguir."); return; }
            String followerId = (String) model.getValueAt(row, 0);
            String followedId = fFollowId.getText().trim();
            if (followedId.isEmpty()) { JOptionPane.showMessageDialog(this, "Introduz o ID do utilizador a seguir."); return; }
            UserFollow uf = db.addFollow(followerId, followedId);
            if (uf != null) JOptionPane.showMessageDialog(this, followerId + " passou a seguir " + followedId + " ✓");
            else JOptionPane.showMessageDialog(this, "Não foi possível criar o follow.", "Erro", JOptionPane.ERROR_MESSAGE);
        });

        btnUnfollow.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona o utilizador que vai deixar de seguir."); return; }
            String followerId = (String) model.getValueAt(row, 0);
            String followedId = fFollowId.getText().trim();
            if (followedId.isEmpty()) { JOptionPane.showMessageDialog(this, "Introduz o ID do utilizador."); return; }
            UserFollow uf = db.follows().unfollow(followerId, followedId);
            if (uf != null) JOptionPane.showMessageDialog(this, followerId + " deixou de seguir " + followedId + " ✓");
            else JOptionPane.showMessageDialog(this, "Relação de follow não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
        });

        btnVerSeg.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um utilizador."); return; }
            String id = (String) model.getValueAt(row, 0);
            List<User> seguidores = db.follows().getFollowers(id);
            if (seguidores.isEmpty()) {
                JOptionPane.showMessageDialog(this, id + " não tem seguidores.");
            } else {
                StringBuilder sb = new StringBuilder("Seguidores de " + id + ":\n");
                for (User u : seguidores) sb.append("  • ").append(u.getName()).append(" (").append(u.getId()).append(")\n");
                JOptionPane.showMessageDialog(this, sb.toString());
            }
        });

        btnVerSeguindo.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um utilizador."); return; }
            String id = (String) model.getValueAt(row, 0);
            List<User> seguindo = db.follows().getFollowing(id);
            if (seguindo.isEmpty()) {
                JOptionPane.showMessageDialog(this, id + " não segue ninguém.");
            } else {
                StringBuilder sb = new StringBuilder(id + " segue:\n");
                for (User u : seguindo) sb.append("  • ").append(u.getName()).append(" (").append(u.getId()).append(")\n");
                JOptionPane.showMessageDialog(this, sb.toString());
            }
        });

        followPanel.add(new JLabel("ID a seguir/deixar:"));
        followPanel.add(fFollowId);
        followPanel.add(btnFollow);
        followPanel.add(btnUnfollow);
        followPanel.add(btnVerSeg);
        followPanel.add(btnVerSeguindo);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        search.setBorder(new TitledBorder("Pesquisar por Nome"));
        JTextField fSearch = new JTextField(15);
        JButton btnSearch  = new JButton("Pesquisar");
        JButton btnAll     = new JButton("Todos");
        btnSearch.addActionListener(e -> {
            String q = fSearch.getText().trim();
            model.setRowCount(0);
            for (User u : db.users().searchByNameSubstring(q))
                model.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(), u.getRegion(), u.getRegisterDate()});
        });
        btnAll.addActionListener(e -> refreshUserTable(model));
        search.add(fSearch); search.add(btnSearch); search.add(btnAll);

        JPanel southFinal = new JPanel(new BorderLayout());
        southFinal.add(search, BorderLayout.NORTH);
        southFinal.add(form,   BorderLayout.CENTER);

        JPanel allActions = new JPanel(new BorderLayout());
        JPanel insertBtns = new JPanel(new FlowLayout());
        insertBtns.add(btnAdd); insertBtns.add(btnRemove);
        allActions.add(insertBtns,  BorderLayout.NORTH);
        allActions.add(editPanel,   BorderLayout.CENTER);
        allActions.add(followPanel, BorderLayout.SOUTH);

        southFinal.add(allActions, BorderLayout.SOUTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(southFinal, BorderLayout.SOUTH);
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

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(new TitledBorder("Inserir Conteúdo"));

        JTextField fId      = new JTextField();
        JTextField fTitulo  = new JTextField();
        JTextField fGenre   = new JTextField();
        JTextField fData    = new JTextField("2024-01-01");
        JTextField fDuracao = new JTextField("120");
        JTextField fRegiao  = new JTextField("PT");
        String[] tipos = {"Filme", "Série", "Documentário"};
        JComboBox<String> cbTipo = new JComboBox<>(tipos);

        form.add(new JLabel("Tipo:"));              form.add(cbTipo);
        form.add(new JLabel("ID:"));                form.add(fId);
        form.add(new JLabel("Título:"));            form.add(fTitulo);
        form.add(new JLabel("ID Género:"));         form.add(fGenre);
        form.add(new JLabel("Data (yyyy-MM-dd):")); form.add(fData);
        form.add(new JLabel("Duração (min):"));     form.add(fDuracao);
        form.add(new JLabel("Região:"));            form.add(fRegiao);

        JButton btnAdd    = new JButton("Adicionar");
        JButton btnRemove = new JButton("Remover Selecionado");

        btnAdd.addActionListener(e -> {
            String id     = fId.getText().trim();
            String titulo = fTitulo.getText().trim();
            Genre genre   = db.genres().get(fGenre.getText().trim());
            if (genre == null) {
                JOptionPane.showMessageDialog(this, "Género não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                LocalDate data = LocalDate.parse(fData.getText().trim());
                int dur = Integer.parseInt(fDuracao.getText().trim());
                String reg = fRegiao.getText().trim();
                Content c = switch ((String) cbTipo.getSelectedItem()) {
                    case "Série"        -> new Series(id, titulo, genre, data, dur, reg, 1);
                    case "Documentário" -> new Documentary(id, titulo, genre, data, dur, reg, "", "");
                    default             -> new Movie(id, titulo, genre, data, dur, reg, null);
                };
                if (db.addContent(c)) refreshContentTable(model);
                else JOptionPane.showMessageDialog(this, "ID já existe.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dados inválidos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRemove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um conteúdo."); return; }
            db.removeContent((String) model.getValueAt(row, 0));
            refreshContentTable(model);
        });

        JPanel searchContainer = new JPanel();
        searchContainer.setLayout(new BoxLayout(searchContainer, BoxLayout.Y_AXIS));
        searchContainer.setBorder(new TitledBorder("Pesquisar Conteúdos"));

        JPanel searchRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField fSearchTitulo = new JTextField(15);
        JButton btnSearchTitulo  = new JButton("Por Título");
        btnSearchTitulo.addActionListener(e -> {
            String q = fSearchTitulo.getText().trim();
            model.setRowCount(0);
            for (Content c : db.contents().searchByTitleSubstring(q)) {
                String tipo = c instanceof Movie ? "Filme" : c instanceof Series ? "Série" : "Documentário";
                model.addRow(new Object[]{c.getId(), tipo, c.getTitle(), c.getGenre().getName(),
                        c.getReleaseDate(), c.getDuration(), c.getRating()});
            }
        });
        searchRow1.add(new JLabel("Título:")); searchRow1.add(fSearchTitulo); searchRow1.add(btnSearchTitulo);

        JPanel searchRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cbFilterTipo = new JComboBox<>(new String[]{"-- Todos --", "Filme", "Série", "Documentário"});
        JButton btnSearchTipo   = new JButton("Por Tipo");
        JButton btnAllConteudos = new JButton("Ver Todos");
        btnSearchTipo.addActionListener(e -> {
            String sel = (String) cbFilterTipo.getSelectedItem();
            if (sel == null || sel.startsWith("--")) { refreshContentTable(model); return; }
            model.setRowCount(0);
            for (Content c : db.contents().listAll()) {
                boolean match = (sel.equals("Filme") && c instanceof Movie)
                        || (sel.equals("Série") && c instanceof Series)
                        || (sel.equals("Documentário") && c instanceof Documentary);
                if (match) model.addRow(new Object[]{c.getId(), sel, c.getTitle(), c.getGenre().getName(),
                        c.getReleaseDate(), c.getDuration(), c.getRating()});
            }
        });
        btnAllConteudos.addActionListener(e -> refreshContentTable(model));
        searchRow2.add(new JLabel("Tipo:")); searchRow2.add(cbFilterTipo);
        searchRow2.add(btnSearchTipo); searchRow2.add(btnAllConteudos);

        searchContainer.add(searchRow1);
        searchContainer.add(searchRow2);

        JPanel btns = new JPanel(new FlowLayout());
        btns.add(btnAdd); btns.add(btnRemove);

        JPanel south = new JPanel(new BorderLayout());
        south.add(searchContainer, BorderLayout.NORTH);
        south.add(form,   BorderLayout.CENTER);
        south.add(btns,   BorderLayout.SOUTH);

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

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(new TitledBorder("Inserir Artista"));

        JTextField fId   = new JTextField();
        JTextField fNome = new JTextField();
        JTextField fNac  = new JTextField("PT");
        JTextField fGen  = new JTextField("M");
        JTextField fData = new JTextField("1980-01-01");
        JComboBox<ArtistRole> cbRole = new JComboBox<>(ArtistRole.values());

        form.add(new JLabel("ID:"));                      form.add(fId);
        form.add(new JLabel("Nome:"));                    form.add(fNome);
        form.add(new JLabel("Nacionalidade:"));           form.add(fNac);
        form.add(new JLabel("Género (M/F):"));            form.add(fGen);
        form.add(new JLabel("Data Nasc. (yyyy-MM-dd):")); form.add(fData);
        form.add(new JLabel("Papel:"));                   form.add(cbRole);

        JButton btnAdd    = new JButton("Adicionar");
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

        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        editPanel.setBorder(new TitledBorder("Editar Artista Selecionado"));

        JButton btnEditNac = new JButton("Editar Nacionalidade");
        btnEditNac.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Seleciona um artista."); return; }
            String id    = (String) model.getValueAt(row, 0);
            String atual = (String) model.getValueAt(row, 2);
            String nova  = JOptionPane.showInputDialog(this, "Nova Nacionalidade:", atual);
            if (nova == null || nova.trim().isEmpty()) return;
            if (db.artists().editNationality(id, nova.trim())) refreshArtistTable(model);
        });
        editPanel.add(btnEditNac);

        JPanel searchContainer = new JPanel();
        searchContainer.setLayout(new BoxLayout(searchContainer, BoxLayout.Y_AXIS));
        searchContainer.setBorder(new TitledBorder("Pesquisar Artistas"));

        JPanel searchRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField fSearchNome = new JTextField(12);
        JButton btnSearchNome  = new JButton("Por Nome");
        JComboBox<String> cbRoleFilter = new JComboBox<>();
        cbRoleFilter.addItem("-- Todos os papéis --");
        for (ArtistRole r : ArtistRole.values()) cbRoleFilter.addItem(r.name());
        JButton btnSearchRole = new JButton("Por Papel");

        btnSearchNome.addActionListener(e -> {
            String q = fSearchNome.getText().trim();
            if (q.isEmpty()) { refreshArtistTable(model); return; }
            model.setRowCount(0);
            for (Artist a : db.artists().searchByNameSubstring(q))
                model.addRow(new Object[]{a.getId(), a.getName(), a.getNationality(),
                        a.getGender(), a.getBirthDate(), a.getRole()});
        });

        btnSearchRole.addActionListener(e -> {
            String sel = (String) cbRoleFilter.getSelectedItem();
            if (sel == null || sel.startsWith("--")) { refreshArtistTable(model); return; }
            ArtistRole role = ArtistRole.valueOf(sel);
            model.setRowCount(0);
            for (Artist a : db.artists().searchByRole(role))
                model.addRow(new Object[]{a.getId(), a.getName(), a.getNationality(),
                        a.getGender(), a.getBirthDate(), a.getRole()});
        });

        searchRow1.add(new JLabel("Nome:"));  searchRow1.add(fSearchNome); searchRow1.add(btnSearchNome);
        searchRow1.add(new JLabel("Papel:")); searchRow1.add(cbRoleFilter); searchRow1.add(btnSearchRole);

        JPanel searchRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField fSearchData = new JTextField(10);
        JButton btnSearchData  = new JButton("Por Data Nasc.");
        JButton btnAllArtistas = new JButton("Ver Todos");

        btnSearchData.addActionListener(e -> {
            String q = fSearchData.getText().trim();
            if (q.isEmpty()) { refreshArtistTable(model); return; }
            try {
                LocalDate dataExata = LocalDate.parse(q);
                model.setRowCount(0);
                for (Artist a : db.artists().searchByBirthDate(dataExata))
                    model.addRow(new Object[]{a.getId(), a.getName(), a.getNationality(),
                            a.getGender(), a.getBirthDate(), a.getRole()});
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Data inválida. Usa o formato yyyy-MM-dd.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnAllArtistas.addActionListener(e -> refreshArtistTable(model));

        searchRow2.add(new JLabel("Data Nasc. (yyyy-MM-dd):")); searchRow2.add(fSearchData);
        searchRow2.add(btnSearchData); searchRow2.add(btnAllArtistas);

        searchContainer.add(searchRow1);
        searchContainer.add(searchRow2);

        JPanel btns = new JPanel(new FlowLayout());
        btns.add(btnAdd); btns.add(btnEditNac); btns.add(btnRemove);

        JPanel south = new JPanel(new BorderLayout());
        south.add(searchContainer, BorderLayout.NORTH);
        south.add(form,   BorderLayout.CENTER);
        south.add(btns,   BorderLayout.SOUTH);

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

        JPanel r8c = new JPanel(new FlowLayout(FlowLayout.LEFT));
        r8c.setBorder(new TitledBorder("R8c — Grafo de utilizadores é fortemente conexo?"));
        JButton btnConexo = new JButton("Verificar");
        btnConexo.addActionListener(e -> {
            boolean conexo = db.getGraph().isGrafoUtilizadoresConexo();
            output.setText("[R8c] O grafo de utilizadores é fortemente conexo: " + (conexo ? "SIM ✓" : "NÃO ✗"));
        });
        r8c.add(btnConexo);

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

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnInfo = new JButton("ℹ️ Info do Grafo");
        btnInfo.addActionListener(e -> output.setText(
                "[GRAFO] Vértices: " + db.getGraph().totalVertices()
                        + " | Arestas: " + db.getGraph().totalArestas()));
        infoPanel.add(btnInfo);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(r8a); controls.add(r8c); controls.add(r8g); controls.add(infoPanel);

        panel.add(new JScrollPane(controls), BorderLayout.WEST);
        panel.add(new JScrollPane(output),   BorderLayout.CENTER);
        return panel;
    }

    // -----------------------------------------------------------------------
    // Barra de estado
    // -----------------------------------------------------------------------

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.add(new JLabel("Plataforma de Streaming | LP2/AED2 2025/26 | Utilizadores: "
                + db.users().size() + " | Conteúdos: " + db.contents().size()));
        return bar;
    }

    // -----------------------------------------------------------------------
    // Base de dados de exemplo
    // -----------------------------------------------------------------------

    private StreamingDatabase buildSampleDB() {
        StreamingDatabase sdb = new StreamingDatabase();

        Genre gAcao  = new Genre("g1", "Acao");
        Genre gDrama = new Genre("g2", "Drama");
        Genre gSci   = new Genre("g3", "Sci-Fi");
        sdb.addGenre(gAcao); sdb.addGenre(gDrama); sdb.addGenre(gSci);

        User u1 = new User("u1", "Alice Silva",   "alice@mail.com",  "PT", LocalDate.of(2020, 1, 10));
        User u2 = new User("u2", "Bruno Costa",   "bruno@mail.com",  "PT", LocalDate.of(2020, 3, 15));
        User u3 = new User("u3", "Carla Pereira", "carla@mail.com",  "BR", LocalDate.of(2021, 6, 20));
        User u4 = new User("u4", "David Alves",   "david@mail.com",  "US", LocalDate.of(2022, 9,  5));
        sdb.addUser(u1); sdb.addUser(u2); sdb.addUser(u3); sdb.addUser(u4);

        Movie  m1 = new Movie ("c1", "Inception",    gAcao,  LocalDate.of(2010, 7, 16), 148, "PT", null);
        Movie  m2 = new Movie ("c2", "Interstellar", gSci,   LocalDate.of(2014,11,  7), 169, "PT", null);
        Series s1 = new Series("c3", "Breaking Bad", gDrama, LocalDate.of(2008, 1, 20),  45, "PT", 5);
        Series s2 = new Series("c4", "Dark",         gAcao,  LocalDate.of(2017,12,  1),  60, "BR", 3);
        sdb.addContent(m1); sdb.addContent(m2); sdb.addContent(s1); sdb.addContent(s2);

        Artist a1 = new Artist("a1", "Christopher Nolan", "GB", "M", LocalDate.of(1970, 7, 30), ArtistRole.DIRECTOR);
        Artist a2 = new Artist("a2", "Leonardo DiCaprio", "US", "M", LocalDate.of(1974,11, 11), ArtistRole.ACTOR);
        sdb.addArtist(a1); sdb.addArtist(a2);
        sdb.addParticipation("a1", "c1", ArtistRole.DIRECTOR, LocalDate.of(2010, 7, 16));
        sdb.addParticipation("a2", "c1", ArtistRole.ACTOR,    LocalDate.of(2010, 7, 16));

        sdb.addFollow("u1", "u2");
        sdb.addFollow("u2", "u3");
        sdb.addFollow("u3", "u4");

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
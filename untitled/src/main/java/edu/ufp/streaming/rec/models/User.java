package edu.ufp.streaming.rec.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um utilizador/cliente da plataforma de streaming.
 *
 * <p>Um utilizador possui um ID único, detalhes pessoais, uma região preferencial,
 * uma lista de géneros preferidos, um histórico de visualização e interações
 * registadas com o conteúdo da plataforma.
 *
 * @author  Diogo Vicente
 */
public class User implements Serializable {

  /** Identificador de versão para serialização. */
  private static final long serialVersionUID = 1L;

  /** Identificador único para este utilizador. */
  private String id;

  /** Nome de exibição do utilizador. */
  private String name;

  /** Endereço de e-mail de contacto. */
  private String email;

  /** Região geográfica (ex: "PT", "US", "BR"). */
  private String region;

  /** Data em que o utilizador se registou na plataforma. */
  private LocalDate registerDate;

  /** Lista de géneros que o utilizador marcou como preferidos. */
  private List<Genre> preferences;

  /** Lista ordenada de conteúdo que o utilizador assistiu. */
  private List<Content> watchHistory;

  /**
   * Todas as interações registadas (ver, avaliar, marcar, saltar).
   * Marcada como transient para evitar referências circulares durante a serialização
   * (Interation → User → Interation...).
   * Após desserialização, esta lista é reconstruída pelo sistema.
   */
  private transient List<Interation> interactions;

  /**
   * Constrói um novo Utilizador com os detalhes fornecidos.
   *
   * @param id           ID único do utilizador
   * @param name         nome de exibição
   * @param email        endereço de e-mail
   * @param region       região geográfica
   * @param registerDate data de registo na plataforma
   */
  public User(String id, String name, String email, String region, LocalDate registerDate) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.region = region;
    this.registerDate = registerDate;
    this.preferences = new ArrayList<>();
    this.watchHistory = new ArrayList<>();
    this.interactions = new ArrayList<>();
  }

  /**
   * Retorna o ID único do utilizador.
   *
   * @return string do ID do utilizador
   */
  public String getId() { return id; }

  /**
   * Retorna o nome de exibição do utilizador.
   *
   * @return string do nome
   */
  public String getName() { return name; }

  /**
   * Retorna o endereço de e-mail do utilizador.
   *
   * @return string do e-mail
   */
  public String getEmail() { return email; }

  /**
   * Retorna a região geográfica do utilizador.
   *
   * @return string da região
   */
  public String getRegion() { return region; }

  /**
   * Retorna a data em que o utilizador se registou na plataforma.
   *
   * @return data de registo
   */
  public LocalDate getRegisterDate() { return registerDate; }

  /**
   * Retorna a lista de géneros preferidos do utilizador.
   *
   * @return lista mutável de preferências de {@link Genre}
   */
  public List<Genre> getPreferences() { return preferences; }

  /**
   * Retorna o histórico de visualização do utilizador.
   *
   * @return lista mutável de {@link Content} assistidos
   */
  public List<Content> getWatchHistory() { return watchHistory; }

  /**
   * Retorna todas as interações registadas para este utilizador.
   * Se a lista for null (após desserialização), inicializa-a automaticamente.
   *
   * @return lista mutável de objetos {@link Interation}
   */
  public List<Interation> getInteractions() {
    if (interactions == null) interactions = new ArrayList<>();
    return interactions;
  }

  /**
   * Define o ID único do utilizador.
   *
   * @param id nova string de ID
   */
  public void setId(String id) { this.id = id; }

  /**
   * Atualiza o nome de exibição do utilizador.
   *
   * @param name novo nome
   */
  public void setName(String name) { this.name = name; }

  /**
   * Atualiza o endereço de e-mail do utilizador.
   *
   * @param email novo endereço de e-mail
   */
  public void setEmail(String email) { this.email = email; }

  /**
   * Atualiza a região geográfica do utilizador.
   *
   * @param region nova string de região
   */
  public void setRegion(String region) { this.region = region; }

  /**
   * Atualiza a data de registo do utilizador.
   *
   * @param registerDate nova data de registo
   */
  public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }

  /**
   * Substitui a lista de preferências de género do utilizador.
   *
   * @param preferences nova lista de preferências de {@link Genre}
   */
  public void setPreferences(List<Genre> preferences) { this.preferences = preferences; }

  /**
   * Substitui a lista do histórico de visualização do utilizador.
   *
   * @param watchHistory nova lista de {@link Content} assistidos
   */
  public void setWatchHistory(List<Content> watchHistory) { this.watchHistory = watchHistory; }

  /**
   * Substitui a lista de interações do utilizador.
   *
   * @param interactions nova lista de objetos {@link Interation}
   */
  public void setInteractions(List<Interation> interactions) { this.interactions = interactions; }

  /**
   * Adiciona um género às preferências do utilizador, se ainda não estiver presente.
   *
   * @param genre o {@link Genre} a adicionar; ignorado se for {@code null} ou se já existir
   */
  public void addPreference(Genre genre) {
    if (this.preferences == null) this.preferences = new ArrayList<>();
    if (genre != null && !this.preferences.contains(genre)) this.preferences.add(genre);
  }

  /**
   * Regista uma interação para este utilizador.
   *
   * @param interation a {@link Interation} a adicionar
   */
  public void addInteration(Interation interation) {
    if (this.interactions == null) this.interactions = new ArrayList<>();
    this.interactions.add(interation);
  }

  /**
   * Espaço reservado para a ação de seguir.
   * A lógica real de seguimento é gerida pelo {@link edu.ufp.streaming.rec.managers.FollowManager}.
   *
   * @param u o utilizador a seguir
   */
  public void follow(User u) {
    // A lógica de follow é delegada ao FollowManager
  }

  /**
   * Retorna a lista de seguidores (vazia por defeito, gerida externamente).
   *
   * @return lista de utilizadores seguidores
   */
  public List<User> getFollowers() { return new ArrayList<>(); }

  @Override
  public String toString() {
    return "User{id='" + id + "', name='" + name + "', email='" + email
            + "', region='" + region + "', registerDate=" + registerDate + "}";
  }
}
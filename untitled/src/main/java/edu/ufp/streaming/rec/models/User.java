package edu.ufp.streaming.rec.models;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.time.LocalDate;

/**
 * Representa um utilizador/cliente da plataforma de streaming.
 *
 * <p>A password é armazenada como hash SHA-256 com salt aleatório (PBKDF-like),
 * no formato {@code base64(salt):hex(SHA-256(salt + password))}.
 * O campo {@code passwordHash} é {@code null} enquanto o utilizador ainda não
 * tiver definido a sua password — o sistema bloqueia o login nesse estado e
 * redireciona para a definição de password.
 *
 * @author Diogo Vicente
 */
public class User implements Serializable {

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

  /**
   * Hash da password no formato {@code base64(salt):hex(SHA-256(salt+password))}.
   * {@code null} significa que a password ainda não foi definida.
   */
  private String passwordHash;

  /** Lista de géneros que o utilizador marcou como preferidos. */
  private List<Genre> preferences;

  /** Lista ordenada de conteúdo que o utilizador assistiu. */
  private List<Content> watchHistory;

  /**
   * Todas as interações registadas (ver, avaliar, marcar, saltar).
   * Marcada como transient para evitar referências circulares na serialização.
   * Após desserialização, esta lista é reconstruída pelo sistema.
   */
  private transient List<Interation> interations;

  // -------------------------------------------------------------------------
  // Construtores
  // -------------------------------------------------------------------------

  /**
   * Constrói um novo utilizador com password definida desde o início.
   *
   * @param id           ID único do utilizador
   * @param name         nome de exibição
   * @param email        endereço de e-mail
   * @param region       região geográfica
   * @param registerDate data de registo na plataforma
   * @param rawPassword  password em texto simples (guardada como hash SHA-256 + salt)
   */
  public User(String id, String name, String email, String region,
              LocalDate registerDate, String rawPassword) {
    this.id           = id;
    this.name         = name;
    this.email        = email;
    this.region       = region;
    this.registerDate = registerDate;
    this.passwordHash = (rawPassword != null && !rawPassword.isEmpty())
            ? hashPassword(rawPassword)
            : null;   // password ainda não definida
    this.preferences  = new ArrayList<>();
    this.watchHistory = new ArrayList<>();
    this.interations = new ArrayList<>();
  }

  /**
   * Constrói um novo utilizador sem password definida.
   * A password deve ser definida com {@link #setInitialPassword(String)}
   * antes do primeiro login.
   *
   * @param id           ID único do utilizador
   * @param name         nome de exibição
   * @param email        endereço de e-mail
   * @param region       região geográfica
   * @param registerDate data de registo na plataforma
   */
  public User(String id, String name, String email, String region, LocalDate registerDate) {
    this(id, name, email, region, registerDate, null);
  }

  // -------------------------------------------------------------------------
  // Autenticação
  // -------------------------------------------------------------------------

  /**
   * Indica se a password já foi definida para este utilizador.
   *
   * @return {@code true} se a password estiver definida
   */
  public boolean hasPassword() {
    return passwordHash != null;
  }

  /**
   * Define a password inicial de um utilizador que ainda não tem password.
   * Só funciona se {@link #hasPassword()} for {@code false} — use
   * {@link #changePassword(String)} para alterar uma password já existente.
   *
   * @param rawPassword nova password em texto simples
   * @return {@code true} se definida com sucesso; {@code false} se já existia uma password
   */
  public boolean setInitialPassword(String rawPassword) {
    if (hasPassword()) return false;
    if (rawPassword == null || rawPassword.isEmpty()) return false;
    this.passwordHash = hashPassword(rawPassword);
    return true;
  }

  /**
   * Verifica se a password fornecida corresponde à password guardada.
   * Devolve {@code false} se a password ainda não tiver sido definida.
   *
   * @param rawPassword password em texto simples a verificar
   * @return {@code true} se a password estiver correta
   */
  public boolean checkPassword(String rawPassword) {
    if (!hasPassword() || rawPassword == null) return false;
    return verifyPassword(rawPassword, this.passwordHash);
  }

  /**
   * Altera a password do utilizador.
   * A nova password é armazenada como hash SHA-256 com salt novo.
   *
   * @param newRawPassword nova password em texto simples
   */
  public void changePassword(String newRawPassword) {
    if (newRawPassword == null || newRawPassword.isEmpty()) return;
    this.passwordHash = hashPassword(newRawPassword);
  }

  // -------------------------------------------------------------------------
  // Hashing com salt (SHA-256 + SecureRandom)
  // -------------------------------------------------------------------------

  /**
   * Gera um hash seguro de uma password com salt aleatório.
   * Formato do resultado: {@code base64(salt):hex(SHA-256(salt+password))}.
   *
   * @param raw password em texto simples
   * @return string no formato salt:hash
   */
  private static String hashPassword(String raw) {
    try {
      // Gerar salt aleatório de 16 bytes
      SecureRandom rng = new SecureRandom();
      byte[] salt = new byte[16];
      rng.nextBytes(salt);
      String saltB64 = Base64.getEncoder().encodeToString(salt);

      // Calcular SHA-256(salt + password)
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(salt);
      md.update(raw.getBytes(StandardCharsets.UTF_8));
      byte[] hashBytes = md.digest();

      // Codificar hash em hex
      StringBuilder hex = new StringBuilder();
      for (byte b : hashBytes) hex.append(String.format("%02x", b));

      return saltB64 + ":" + hex;
    } catch (NoSuchAlgorithmException e) {
      // SHA-256 está sempre disponível na JVM padrão
      throw new RuntimeException("SHA-256 não disponível", e);
    }
  }

  /**
   * Verifica uma password contra um hash no formato {@code base64(salt):hex(hash)}.
   *
   * @param raw       password em texto simples a verificar
   * @param saltedHash hash armazenado no formato salt:hash
   * @return {@code true} se a password corresponder
   */
  private static boolean verifyPassword(String raw, String saltedHash) {
    try {
      String[] parts = saltedHash.split(":", 2);
      if (parts.length != 2) return false;

      byte[] salt = Base64.getDecoder().decode(parts[0]);

      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(salt);
      md.update(raw.getBytes(StandardCharsets.UTF_8));
      byte[] hashBytes = md.digest();

      StringBuilder hex = new StringBuilder();
      for (byte b : hashBytes) hex.append(String.format("%02x", b));

      // Comparação em tempo constante para evitar timing attacks
      return constantTimeEquals(hex.toString(), parts[1]);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Compara duas strings em tempo constante para evitar timing attacks.
   *
   * @param a primeira string
   * @param b segunda string
   * @return {@code true} se as strings forem iguais
   */
  private static boolean constantTimeEquals(String a, String b) {
    if (a.length() != b.length()) return false;
    int diff = 0;
    for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
    return diff == 0;
  }

  // -------------------------------------------------------------------------
  // Getters
  // -------------------------------------------------------------------------

  /** @return ID único do utilizador */
  public String getId() { return id; }

  /** @return nome de exibição do utilizador */
  public String getName() { return name; }

  /** @return endereço de e-mail do utilizador */
  public String getEmail() { return email; }

  /** @return região geográfica do utilizador */
  public String getRegion() { return region; }

  /** @return data em que o utilizador se registou */
  public LocalDate getRegisterDate() { return registerDate; }

  /** @return lista de géneros preferidos do utilizador */
  public List<Genre> getPreferences() { return preferences; }

  /** @return histórico de visualização do utilizador */
  public List<Content> getWatchHistory() { return watchHistory; }

  /**
   * Devolve todas as interações registadas para este utilizador.
   * Se a lista for {@code null} (após desserialização), inicializa-a automaticamente.
   *
   * @return lista mutável de objetos {@link Interation}
   */
  public List<Interation> getInteractions() {
    if (interations == null) interations = new ArrayList<>();
    return interations;
  }

  // -------------------------------------------------------------------------
  // Setters
  // -------------------------------------------------------------------------

  /** @param id nova string de ID */
  public void setId(String id) { this.id = id; }

  /** @param name novo nome de exibição */
  public void setName(String name) { this.name = name; }

  /** @param email novo endereço de e-mail */
  public void setEmail(String email) { this.email = email; }

  /** @param region nova string de região */
  public void setRegion(String region) { this.region = region; }

  /** @param registerDate nova data de registo */
  public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }

  /** @param preferences nova lista de preferências de {@link Genre} */
  public void setPreferences(List<Genre> preferences) { this.preferences = preferences; }

  /** @param watchHistory nova lista de {@link Content} assistidos */
  public void setWatchHistory(List<Content> watchHistory) { this.watchHistory = watchHistory; }

  /** @param interactions nova lista de objetos {@link Interation} */
  public void setInteractions(List<Interation> interactions) { this.interations = interations; }

  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  // -------------------------------------------------------------------------
  // Métodos de negócio
  // -------------------------------------------------------------------------

  /**
   * Adiciona um género às preferências do utilizador, se ainda não estiver presente.
   *
   * @param genre o {@link Genre} a adicionar; ignorado se {@code null} ou duplicado
   */
  public void addPreference(Genre genre) {
    if (preferences == null) preferences = new ArrayList<>();
    if (genre != null && !preferences.contains(genre)) preferences.add(genre);
  }

  /**
   * Regista uma interação para este utilizador.
   *
   * @param interaction a {@link Interation} a adicionar
   */
  public void addInteraction(Interation interaction) {
    if (interations == null) interations = new ArrayList<>();
    interations.add(interaction);
  }
  /** @return hash da password (formato salt:hash), ou {@code null} se não definida */
  public String getPasswordHash() { return passwordHash; }


  @Override
  public String toString() {
    return "User{id='" + id + "', name='" + name + "', email='" + email
            + "', region='" + region + "', registerDate=" + registerDate
            + ", passwordDefined=" + hasPassword() + "}";
  }
} 
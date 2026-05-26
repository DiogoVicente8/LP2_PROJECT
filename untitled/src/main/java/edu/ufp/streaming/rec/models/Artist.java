package edu.ufp.streaming.rec.models;

import edu.ufp.streaming.rec.enums.ArtistRole;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um artista (ator, realizador, produtor ou argumentista) na plataforma de streaming.
 *
 * <p>Os artistas possuem um 'ID' único, detalhes pessoais, uma função principal ({@link ArtistRole}),
 * e uma filmografia — uma lista de associações {@link ArtistContent} que ligam
 * este artista ao conteúdo em que participou.
 *
 * @author  Diogo Vicente
 */
public class Artist implements Serializable {

  /** Identificador único para este artista. */
  private String id;

  /** Nome completo do artista. */
  private String name;

  /** País de origem/nacionalidade. */
  private String nationality;

  /** String de género (ex: "M", "F"). */
  private final String gender;

  /** Data de nascimento. */
  private final LocalDate birthDate;

  /** Função principal na plataforma. */
  private final ArtistRole role;

  /**
   * Lista de participações em conteúdos (filmografia).
   * Marcada como transient para evitar referências circulares durante a serialização
   * (ArtistContent → Content → Artist → ArtistContent...).
   * Após desserialização, esta lista é reconstruída pelo ArtistContentManager.
   */
  private transient List<ArtistContent> participates;

  /**
   * Constrói um novo Artista com os detalhes fornecidos.
   *
   * @param id          ID único do artista
   * @param name        nome completo
   * @param nationality país de origem
   * @param gender      string de género
   * @param birthDate   data de nascimento
   * @param role        função principal ({@link ArtistRole})
   */
  public Artist(String id, String name, String nationality, String gender,
                LocalDate birthDate, ArtistRole role) {
    this.id = id;
    this.name = name;
    this.nationality = nationality;
    this.gender = gender;
    this.birthDate = birthDate;
    this.role = role;
    this.participates = new ArrayList<>();
  }

  /**
   * Retorna o ID único do artista.
   *
   * @return string do ID
   */
  public String getId() { return id; }

  /**
   * Retorna o nome completo do artista.
   *
   * @return 'string' do nome
   */
  public String getName() { return name; }

  /**
   * Retorna a nacionalidade do artista.
   *
   * @return string da nacionalidade
   */
  public String getNationality() { return nationality; }

  /**
   * Retorna o género do artista.
   *
   * @return string do género
   */
  public String getGender() { return gender; }

  /**
   * Retorna a data de nascimento do artista.
   *
   * @return data de nascimento
   */
  public LocalDate getBirthDate() { return birthDate; }

  /**
   * Retorna a função principal do artista.
   *
   * @return {@link ArtistRole}
   */
  public ArtistRole getRole() { return role; }

  /**
   * Retorna a filmografia do artista (lista de participações em conteúdos).
   * @return lista de {@link ArtistContent} (vazia se não tiver nenhuma).
   */
  public List<ArtistContent> getFilmography() {

    return participates == null ? new ArrayList<>() : participates;
  }

  /**
   * Define o ID único do artista.
   *
   * @param id nova string de ID
   */
  public void setId(String id) { this.id = id; }

  /**
   * Atualiza o nome de exibição do artista.
   *
   * @param name novo nome
   */
  public void setName(String name) { this.name = name; }

  /**
   * Atualiza a nacionalidade do artista.
   *
   * @param nationality nova string de nacionalidade
   */
  public void setNationality(String nationality) { this.nationality = nationality; }

  /**
   * Adiciona uma participação em conteúdo à filmografia deste artista.
   * Também é chamado automaticamente pelo {@link edu.ufp.streaming.rec.managers.ArtistContentManager}
   * quando uma nova participação é registada.
   *
   * @param ac a associação {@link ArtistContent} a adicionar
   */
  public void addParticipation(ArtistContent ac) {
    if (ac == null) return;
    if (participates == null) participates = new ArrayList<>();
    if (!participates.contains(ac)) participates.add(ac);
  }

  /**
   * Remove uma participação em conteúdo da filmografia deste artista.
   * Chamado pelo {@link edu.ufp.streaming.rec.managers.ArtistContentManager}
   * quando uma participação é removida.
   *
   * @param ac a associação {@link ArtistContent} a remover
   */
  public void removeParticipation(ArtistContent ac) {
    if (ac == null) return;
    if (participates != null) participates.remove(ac);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Artist)) return false;
    return id.equals(((Artist) o).id);
  }

  @Override
  public int hashCode() { return id.hashCode(); }

  @Override
  public String toString() {
    return "Artist{id='" + id + "', name='" + name + "', nationality='" + nationality
            + "', gender='" + gender + "', birthDate=" + birthDate + ", role=" + role + "}";
  }
}
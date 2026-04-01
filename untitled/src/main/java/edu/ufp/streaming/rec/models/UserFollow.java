package edu.ufp.streaming.rec.models;

import java.time.LocalDateTime;
/**
 * Representa uma relação de seguimento (follow) entre duas entidades {@link User}.
 *
 * <p>Armazena o seguidor, o utilizador seguido e o carimbo de data/hora (timestamp)
 * de quando a relação de seguimento foi criada.
 *
 * <p>Na Fase 2, cada {@code UserFollow} mapeia diretamente para uma aresta pesada
 * direcionada {@code seguidor → seguido} no grafo da plataforma.
 ** @author  Diogo Vicente
 **/
public class UserFollow {

  /** O utilizador que iniciou o seguimento (seguidor). */
  private final User follower;

  /** O utilizador que está a ser seguido. */
  private final User followed;

  /** A data e hora em que a relação de seguimento foi criada. */
  private final LocalDateTime followDate;

  /**
   * Constrói uma nova relação UserFollow entre dois utilizadores.
   * A data de seguimento é definida automaticamente para a data e hora atuais.
   *
   * @param follower o {@link User} que está a seguir
   * @param followed o {@link User} que está a ser seguido
   */
  public UserFollow(User follower, User followed) {
    this.follower = follower;
    this.followed = followed;
    this.followDate = LocalDateTime.now();
  }

  /**
   * Retorna o utilizador que iniciou o seguimento.
   *
   * @return o {@link User} seguidor
   */
  public User getFollower() { return follower; }

  /**
   * Retorna o utilizador que está a ser seguido.
   *
   * @return o {@link User} seguido
   */
  public User getFollowed() { return followed; }

  /**
   * Retorna a data e hora em que esta relação de seguimento foi criada.
   *
   * @return carimbo de data/hora do follow
   */
  public LocalDateTime getDate() { return followDate; }

  @Override
  public String toString() {
    return "UserFollow{follower=" + follower.getId()
            + ", followed=" + followed.getId()
            + ", date=" + followDate + "}";
  }
}
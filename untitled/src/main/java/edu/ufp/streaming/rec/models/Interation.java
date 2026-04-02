package edu.ufp.streaming.rec.models;

import edu.ufp.streaming.rec.enums.InterationType;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * Representa uma interação de um utilizador com um conteúdo na plataforma de streaming.
 *
 * @author Pedro
 * @version 1.0
 */
public class Interation implements Serializable {

  /** Utilizador que realizou a interação. */
  private User user;

  /** Conteúdo alvo da interação. */
  private Content content;

  /** Data e hora em que ocorreu a interação. */
  private LocalDateTime watchDate;

  /** Avaliação atribuída ao conteúdo (se aplicável). */
  private double rating;

  /** Progresso da visualização do conteúdo (se aplicável). */
  private double progress;

  /** Tipo da interação realizada. */
  private InterationType type;

  /** Identificador único da interação. */
  private String id;

  /**
   * Constrói uma nova interação.
   *
   * @param user      utilizador que realizou a interação
   * @param content   conteúdo alvo da interação
   * @param watchDate data e hora da interação
   * @param rating    avaliação atribuída ao conteúdo
   * @param progress  progresso da visualização do conteúdo
   * @param type      tipo da interação
   * @param id        identificador único da interação
   */
  public Interation(User user, Content content, LocalDateTime watchDate, double rating, double progress, InterationType type, String id) {
    this.user = user;
    this.content = content;
    this.watchDate = watchDate;
    this.rating = rating;
    this.progress = progress;
    this.type = type;
    this.id = id;
  }

  /**
   * Devolve o utilizador que realizou a interação.
   *
   * @return utilizador da interação
   */
  public User getUser() {
    return user;
  }

  /**
   * Devolve o conteúdo alvo da interação.
   *
   * @return conteúdo da interação
   */
  public Content getContent() {
    return content;
  }

  /**
   * Devolve a data e hora da interação.
   *
   * @return data da interação
   */
  public LocalDateTime getWatchDate() {
    return watchDate;
  }

  /**
   * Devolve a avaliação atribuída ao conteúdo.
   *
   * @return avaliação da interação
   */
  public double getRating() {
    return rating;
  }

  /**
   * Devolve o progresso de visualização do conteúdo.
   *
   * @return progresso da interação
   */
  public double getProgress() {
    return progress;
  }

  /**
   * Devolve o tipo da interação realizada.
   *
   * @return tipo de interação
   */
  public InterationType getType() {
    return type;
  }

  /**
   * Devolve o identificador único da interação.
   *
   * @return id da interação
   */
  public String getId() {
    return id;
  }

  /**
   * Define a data e hora da interação.
   *
   * @param watchDate nova data e hora
   */
  public void setWatchDate(LocalDateTime watchDate) {
    this.watchDate = watchDate;
  }

  /**
   * Define a avaliação atribuída ao conteúdo.
   *
   * @param rating nova avaliação
   */
  public void setRating(double rating) {
    this.rating = rating;
  }

  /**
   * Define o progresso de visualização do conteúdo.
   *
   * @param progress novo progresso
   */
  public void setProgress(double progress) {
    this.progress = progress;
  }

  /**
   * Define o tipo da interação realizada.
   *
   * @param type novo tipo de interação
   */
  public void setType(InterationType type) {
    this.type = type;
  }

  /**
   * Devolve uma representação textual da interação.
   *
   * @return string com os dados da interação
   */
  @Override
  public String toString() {
    return "edu.pt.lp2.edu.ufp.streaming.rec.models.Interation{" +
            "user=" + user +
            ", content=" + content +
            ", watchDate=" + watchDate +
            ", rating=" + rating +
            ", progress=" + progress +
            ", type=" + type +
            ", id='" + id + '\'' +
            '}';
  }
}
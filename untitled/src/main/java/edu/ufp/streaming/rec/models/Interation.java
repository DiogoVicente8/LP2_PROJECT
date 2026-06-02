package edu.ufp.streaming.rec.models;

import edu.ufp.streaming.rec.enums.InterationType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;



/**
 * Representa uma interação de um utilizador com um conteúdo na plataforma de streaming.
 * Guarda o estado da visualização (progresso) e a avaliação atribuída.
 * * Utiliza a estrutura 'record' para imutabilidade e concisão.
 *
 * @param user      Utilizador que realizou a interação.
 * @param content   Conteúdo alvo da interação.
 * @param watchDate Data e hora em que ocorreu a interação.
 * @param rating    Avaliação atribuída ao conteúdo (0,0 a 5,0).
 * @param progress  Progresso da visualização do conteúdo (0,0 a 1,0).
 * @param type      Tipo da interação realizada (WATCH, RATE, etc.).
 * @param id        Identificador único da interação.
 * @author Pedro
 */
public record Interation(User user, Content content, LocalDateTime watchDate, double rating, double progress,
                         InterationType type, String id) implements Serializable {

  /**
   * Constrói uma nova interação com validações rigorosas de domínio.
   * O bloco compacto garante a integridade dos dados na criação.
   */
  public Interation {
    Objects.requireNonNull(user, "O utilizador não pode ser nulo");
    Objects.requireNonNull(content, "O conteúdo não pode ser nulo");
    Objects.requireNonNull(watchDate, "A data de visualização não pode ser nula");

    if (progress < 0.0 || progress > 1.0) {
      throw new IllegalArgumentException("O progresso deve estar entre 0.0 e 1.0. Recebido: " + progress);
    }
    if (rating < 0.0 || rating > 5.0) {
      throw new IllegalArgumentException("O rating deve estar entre 0.0 e 5.0. Recebido: " + rating);
    }
  }

  // -------------------------------------------------------------------------
  // Métodos Utilitários (Equals e HashCode baseados apenas no ‘ID’)
  // -------------------------------------------------------------------------

  /**
   * Duas interações são consideradas iguais se partilharem o mesmo identificador único.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Interation that = (Interation) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Interation{" +
            "id='" + id + '\'' +
            ", type=" + type +
            ", userId='" + (user != null ? user.getId() : "null") + '\'' +
            ", contentId='" + (content != null ? content.getId() : "null") + '\'' +
            ", rating=" + rating +
            ", progress=" + progress +
            '}';
  }
}
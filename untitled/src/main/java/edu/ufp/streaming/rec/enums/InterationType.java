package edu.ufp.streaming.rec.enums;

/**
 * Representa os tipos de interação possíveis de um utilizador com um conteúdo na plataforma de streaming.
 *
 * @author Pedro
 */
public enum InterationType {

  /** Interação de visualização do conteúdo. */
  WATCH,

  /** Interação de avaliação (dar nota) ao conteúdo. */
  RATE,

  /** Interação de guardar o conteúdo para ver mais tarde (marcador/favorito). */
  BOOKMARK,

  /** Interação de saltar ou ignorar o conteúdo. */
  SKIP
}
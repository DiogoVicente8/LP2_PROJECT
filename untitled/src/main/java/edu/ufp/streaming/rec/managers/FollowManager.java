package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.User;
import edu.ufp.streaming.rec.models.UserFollow;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
/**
 * Gere as relações de seguimento (follow) entre entidades {@link User} na plataforma.
 * @author  Diogo Vicente
 */

public class FollowManager {

    /** ST Primária: "followerId:followedId" → UserFollow. */
    private final ST<String, UserFollow> followST;

    /** Índice: userId → lista de UserFollow onde este utilizador é quem segue (outgoing). */
    private final ST<String, List<UserFollow>> followingIndex;

    /** Índice: userId → lista de UserFollow onde este utilizador é seguido (incoming). */
    private final ST<String, List<UserFollow>> followerIndex;

    /** BST Ordenada: data em segundos (Long) → lista de UserFollow. */
    private final RedBlackBST<Long, List<UserFollow>> byDateBST;

    /**
     * Constrói um FollowManager vazio.
     */
    public FollowManager() {
        this.followST       = new ST<>();
        this.followingIndex = new ST<>();
        this.followerIndex  = new ST<>();
        this.byDateBST      = new RedBlackBST<>();
    }

    /**
     * Regista que o {@code follower} agora segue o {@code followed}.
     * Não faz nada se a relação já existir.
     *
     * @param follower o utilizador que inicia o seguimento
     * @param followed o utilizador que passa a ser seguido
     * @return o novo objeto {@link UserFollow}, ou {@code null} se já o seguia ou se for inválido
     */
    public UserFollow follow(User follower, User followed) {
        if (follower == null || followed == null) return null;
        String key = compositeKey(follower.getId(), followed.getId());
        if (followST.contains(key)) return null;

        UserFollow uf = new UserFollow(follower, followed);
        followST.put(key, uf);
        indexByFollower(uf);
        indexByFollowed(uf);
        indexByDate(uf);
        return uf;
    }

    /**
     * Remove a relação de seguimento de {@code followerId} para {@code followedId}.
     *
     * @param followerId o ID do utilizador que segue
     * @param followedId o ID do utilizador seguido
     * @return o {@link UserFollow} removido, ou {@code null} se não for encontrado
     */
    public UserFollow unfollow(String followerId, String followedId) {
        String key = compositeKey(followerId, followedId);
        if (!followST.contains(key)) return null;

        UserFollow uf = followST.get(key);
        followST.delete(key);
        removeFromFollowerIndex(uf);
        removeFromFollowedIndex(uf);
        removeFromDateIndex(uf);
        return uf;
    }

    /**
     * Retorna {@code true} se o {@code followerId} segue atualmente o {@code followedId}.
     *
     * @param followerId ID do potencial seguidor
     * @param followedId ID do utilizador potencialmente seguido
     * @return {@code true} se a relação de seguimento existir
     */
    public boolean isFollowing(String followerId, String followedId) {
        return followST.contains(compositeKey(followerId, followedId));
    }

    /**
     * Retorna a lista de utilizadores que um determinado utilizador está a seguir.
     *
     * @param userId o ID do utilizador (seguidor)
     * @return lista de objetos {@link User} seguidos; vazia se nenhum
     */
    public List<User> getFollowing(String userId) {
        List<User> result = new ArrayList<>();
        List<UserFollow> list = followingIndex.get(userId);
        if (list != null) {
            for (UserFollow uf : list) result.add(uf.getFollowed());
        }
        return result;
    }

    /**
     * Retorna a lista de utilizadores que seguem um determinado utilizador.
     *
     * @param userId o ID do utilizador seguido
     * @return lista de seguidores ({@link User}); vazia se nenhum
     */
    public List<User> getFollowers(String userId) {
        List<User> result = new ArrayList<>();
        List<UserFollow> list = followerIndex.get(userId);
        if (list != null) {
            for (UserFollow uf : list) result.add(uf.getFollower());
        }
        return result;
    }

    /**
     * Retorna o número de seguidores de um utilizador.
     *
     * @param userId o ID do utilizador seguido
     * @return contagem de seguidores
     */
    public int followerCount(String userId) {
        List<UserFollow> list = followerIndex.get(userId);
        return list != null ? list.size() : 0;
    }

    /**
     * Retorna o número de utilizadores que um determinado utilizador segue.
     *
     * @param userId o ID do utilizador (seguidor)
     * @return contagem de quem ele segue
     */
    public int followingCount(String userId) {
        List<UserFollow> list = followingIndex.get(userId);
        return list != null ? list.size() : 0;
    }

    /**
     * Retorna todas as relações de seguimento criadas num intervalo de data/hora [de, até].
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     * @return lista de objetos {@link UserFollow} no intervalo
     */
    public List<UserFollow> searchByDateRange(LocalDateTime from, LocalDateTime to) {
        List<UserFollow> result = new ArrayList<>();
        Long fromEpoch = from.toEpochSecond(ZoneOffset.UTC);
        Long toEpoch = to.toEpochSecond(ZoneOffset.UTC);

        for (Long dt : byDateBST.keys(fromEpoch, toEpoch)) {
            List<UserFollow> bucket = byDateBST.get(dt);
            if (bucket != null) result.addAll(bucket);
        }
        return result;
    }

    /**
     * Remove todas as relações de seguimento que envolvam um determinado utilizador.
     * Deve ser chamado quando um utilizador é apagado do sistema (consistência R4).
     *
     * @param userId o ID do utilizador removido
     */
    public void removeAllRelationships(String userId) {
        // Remove all outgoing follows (userId → someone)
        List<UserFollow> following = followingIndex.get(userId);
        if (following != null) {
            for (UserFollow uf : new ArrayList<>(following)) {
                String key = compositeKey(userId, uf.getFollowed().getId());
                followST.delete(key);
                removeFromFollowedIndex(uf);
                removeFromDateIndex(uf);
            }
            followingIndex.delete(userId);
        }

        List<UserFollow> followers = followerIndex.get(userId);
        if (followers != null) {
            for (UserFollow uf : new ArrayList<>(followers)) {
                String key = compositeKey(uf.getFollower().getId(), userId);
                followST.delete(key);
                removeFromFollowerIndex(uf);
                removeFromDateIndex(uf);
            }
            followerIndex.delete(userId);
        }
    }

    /**
     * Retorna o número total de relações de seguimento armazenadas.
     *
     * @return contagem total de follows
     */

    public int size() {
        return followST.size();
    }

    // --- Métodos Auxiliares Privados ---

    private String compositeKey(String followerId, String followedId) {
        return followerId + ":" + followedId;
    }

    private void indexByFollower(UserFollow uf) {
        String key = uf.getFollower().getId();
        List<UserFollow> list = followingIndex.get(key);
        if (list == null) { list = new ArrayList<>(); followingIndex.put(key, list); }
        list.add(uf);
    }

    private void indexByFollowed(UserFollow uf) {
        String key = uf.getFollowed().getId();
        List<UserFollow> list = followerIndex.get(key);
        if (list == null) { list = new ArrayList<>(); followerIndex.put(key, list); }
        list.add(uf);
    }

    private void indexByDate(UserFollow uf) {
        Long dt = uf.getDate().toEpochSecond(ZoneOffset.UTC);
        List<UserFollow> bucket = byDateBST.get(dt);
        if (bucket == null) { bucket = new ArrayList<>(); byDateBST.put(dt, bucket); }
        bucket.add(uf);
    }

    private void removeFromFollowerIndex(UserFollow uf) {
        String key = uf.getFollower().getId();
        List<UserFollow> list = followingIndex.get(key);
        if (list != null) { list.remove(uf); if (list.isEmpty()) followingIndex.delete(key); }
    }

    private void removeFromFollowedIndex(UserFollow uf) {
        String key = uf.getFollowed().getId();
        List<UserFollow> list = followerIndex.get(key);
        if (list != null) { list.remove(uf); if (list.isEmpty()) followerIndex.delete(key); }
    }

    private void removeFromDateIndex(UserFollow uf) {
        Long dt = uf.getDate().toEpochSecond(ZoneOffset.UTC);
        List<UserFollow> bucket = byDateBST.get(dt);
        if (bucket != null) { bucket.remove(uf); if (bucket.isEmpty()) byDateBST.delete(dt); }
    }
}
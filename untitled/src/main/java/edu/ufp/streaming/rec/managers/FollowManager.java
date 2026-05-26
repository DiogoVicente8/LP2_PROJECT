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
 * @author Diogo Vicente
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

    public FollowManager() {
        this.followST       = new ST<>();
        this.followingIndex = new ST<>();
        this.followerIndex  = new ST<>();
        this.byDateBST      = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // Ações de Seguimento
    // -------------------------------------------------------------------------

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

    public UserFollow followWithDate(User follower, User followed, LocalDateTime followDate) {
        if (follower == null || followed == null) return null;

        String key = compositeKey(follower.getId(), followed.getId());
        if (followST.contains(key)) return null;

        UserFollow uf = new UserFollow(follower, followed, followDate);
        followST.put(key, uf);
        indexByFollower(uf);
        indexByFollowed(uf);
        indexByDate(uf);

        return uf;
    }

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

    // -------------------------------------------------------------------------
    // Consultas Rápidas
    // -------------------------------------------------------------------------

    public boolean isFollowing(String followerId, String followedId) {
        return followST.contains(compositeKey(followerId, followedId));
    }

    public List<User> getFollowing(String userId) {
        List<User> result = new ArrayList<>();
        List<UserFollow> relacoesDeQuemEuSigo = followingIndex.get(userId);

        if (relacoesDeQuemEuSigo != null) {
            for (UserFollow uf : relacoesDeQuemEuSigo) {
                result.add(uf.getFollowed());
            }
        }
        return result;
    }

    public List<User> getFollowers(String userId) {
        List<User> result = new ArrayList<>();
        List<UserFollow> relacoesDeQuemMeSegue = followerIndex.get(userId);

        if (relacoesDeQuemMeSegue != null) {
            for (UserFollow uf : relacoesDeQuemMeSegue) {
                result.add(uf.getFollower());
            }
        }
        return result;
    }

    public int followingCount(String userId) {
        List<UserFollow> list = followingIndex.get(userId);
        return list != null ? list.size() : 0;
    }

    public int followerCount(String userId) {
        List<UserFollow> list = followerIndex.get(userId);
        return list != null ? list.size() : 0;
    }

    public List<UserFollow> listAll() {
        List<UserFollow> result = new ArrayList<>();
        for (String key : followST.keys()) {
            result.add(followST.get(key));
        }
        return result;
    }

    public int size() {
        return followST.size();
    }

    // -------------------------------------------------------------------------
    // Consistência R4
    // -------------------------------------------------------------------------

    /**
     * Remove todas as relações de seguimento que envolvam um determinado utilizador.
     * Deve ser chamado quando um utilizador é apagado do sistema (consistência R4).
     */
    public void removeAllRelationships(String userId) {

        // Limpar todas as pessoas que este utilizador estava a seguir
        List<UserFollow> quemEleSegue = followingIndex.get(userId);
        if (quemEleSegue != null) {
            for (UserFollow uf : new ArrayList<>(quemEleSegue)) {
                String key = compositeKey(userId, uf.getFollowed().getId());
                followST.delete(key);
                removeFromFollowedIndex(uf);
                removeFromDateIndex(uf);
            }
            followingIndex.delete(userId);
        }

        // Limpar todas as pessoas que seguiam este utilizador
        List<UserFollow> osSeusSeguidores = followerIndex.get(userId);
        if (osSeusSeguidores != null) {
            for (UserFollow uf : new ArrayList<>(osSeusSeguidores)) {
                String key = compositeKey(uf.getFollower().getId(), userId);
                followST.delete(key);
                removeFromFollowerIndex(uf);
                removeFromDateIndex(uf);
            }
            followerIndex.delete(userId);
        }
    }

    // -------------------------------------------------------------------------
    // Métodos Auxiliares de Indexação
    // -------------------------------------------------------------------------

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
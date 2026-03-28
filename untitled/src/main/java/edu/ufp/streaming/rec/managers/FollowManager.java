package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.User;
import edu.ufp.streaming.rec.models.UserFollow;

import java.time.LocalDateTime;
import java.time.ZoneOffset; // <-- Novo import necessário para a conversão
import java.util.ArrayList;
import java.util.List;

public class FollowManager {

    /** Primary ST: "followerId:followedId" → edu.pt.lp2.edu.ufp.streaming.rec.models.UserFollow. */
    private final ST<String, UserFollow> followST;

    /** Per-user index: userId → list of edu.pt.lp2.edu.ufp.streaming.rec.models.UserFollow where this user is the follower. */
    private final ST<String, List<UserFollow>> followingIndex;

    /** Per-user index: userId → list of edu.pt.lp2.edu.ufp.streaming.rec.models.UserFollow where this user is being followed. */
    private final ST<String, List<UserFollow>> followerIndex;

    /** BST ordered by follow date for temporal range queries.
     * CORREÇÃO: Alterado de LocalDateTime para Long
     */
    private final RedBlackBST<Long, List<UserFollow>> byDateBST;

    /**
     * Constructs an empty edu.pt.lp2.edu.ufp.streaming.rec.managers.FollowManager.
     */
    public FollowManager() {
        this.followST       = new ST<>();
        this.followingIndex = new ST<>();
        this.followerIndex  = new ST<>();
        this.byDateBST      = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // Follow / Unfollow
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

    public boolean isFollowing(String followerId, String followedId) {
        return followST.contains(compositeKey(followerId, followedId));
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------


    public List<User> getFollowing(String userId) {
        List<User> result = new ArrayList<>();
        List<UserFollow> list = followingIndex.get(userId);
        if (list != null) {
            for (UserFollow uf : list) result.add(uf.getFollowed());
        }
        return result;
    }

    public List<User> getFollowers(String userId) {
        List<User> result = new ArrayList<>();
        List<UserFollow> list = followerIndex.get(userId);
        if (list != null) {
            for (UserFollow uf : list) result.add(uf.getFollower());
        }
        return result;
    }

    public int followerCount(String userId) {
        List<UserFollow> list = followerIndex.get(userId);
        return list != null ? list.size() : 0;
    }

    public int followingCount(String userId) {
        List<UserFollow> list = followingIndex.get(userId);
        return list != null ? list.size() : 0;
    }

    public List<UserFollow> searchByDateRange(LocalDateTime from, LocalDateTime to) {
        List<UserFollow> result = new ArrayList<>();
        // CORREÇÃO: Transformar from e to em Long (segundos)
        Long fromEpoch = from.toEpochSecond(ZoneOffset.UTC);
        Long toEpoch = to.toEpochSecond(ZoneOffset.UTC);

        for (Long dt : byDateBST.keys(fromEpoch, toEpoch)) {
            List<UserFollow> bucket = byDateBST.get(dt);
            if (bucket != null) result.addAll(bucket);
        }
        return result;
    }

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

        // Remove all incoming follows (someone → userId)
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

    public int size() {
        return followST.size();
    }

    // -------------------------------------------------------------------------
    // Private helpers
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
        // CORREÇÃO: Transformar LocalDateTime em Long (segundos)
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
        // CORREÇÃO: Transformar LocalDateTime em Long (segundos)
        Long dt = uf.getDate().toEpochSecond(ZoneOffset.UTC);
        List<UserFollow> bucket = byDateBST.get(dt);
        if (bucket != null) { bucket.remove(uf); if (bucket.isEmpty()) byDateBST.delete(dt); }
    }
}
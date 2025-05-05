package org.example.shootergame.model;

import jakarta.persistence.*;
import org.example.shootergame.db.SessionFactoryBuilder;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "players")
public class PlayerInfo {
    private static final Map<String, PlayerInfo> allPlayers = new HashMap<>();

    @Id
    @Column(name = "nickname")
    public String nickname;

    @Column(name = "wins")
    public int wins = 0;

    @Transient
    public String color;

    @Transient
    public ArrowInfo arrow = new ArrowInfo();

    @Transient
    public int shots = 0;

    @Transient
    public int score = 0;

    @Transient
    public boolean wantToPause = false;

    @Transient
    public boolean wantToStart = false;

    @Transient
    public boolean shooting = false;

    public PlayerInfo() {
    }

    public PlayerInfo(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public int getWins() {
        return wins;
    }

    public static PlayerInfo loadOrCreateByName(String nickname) {
        PlayerInfo player = allPlayers.get(nickname);
        if (player == null) {
            // Try to load from database first
            try (Session session = SessionFactoryBuilder.getSessionFactory().openSession()) {
                Query<PlayerInfo> query = session.createQuery("FROM PlayerInfo WHERE nickname = :nickname", PlayerInfo.class);
                query.setParameter("nickname", nickname);
                player = query.uniqueResult();

                if (player == null) {
                    // Create new player if not found in DB
                    player = new PlayerInfo(nickname);
                    Transaction transaction = session.beginTransaction();
                    session.persist(player);
                    transaction.commit();
                }
            } catch (Exception e) {
                // Fallback to in-memory if database access fails
                player = new PlayerInfo(nickname);
                e.printStackTrace();
            }

            // Add to in-memory cache
            allPlayers.put(nickname, player);
        }
        return player;
    }

    public void increaseWins() {
        ++wins;
        // Save to database
        try (Session session = SessionFactoryBuilder.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(this);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<PlayerInfo> getAllPlayers() {
        List<PlayerInfo> players = new ArrayList<>();

        try (Session session = SessionFactoryBuilder.getSessionFactory().openSession()) {
            Query<PlayerInfo> query = session.createQuery("FROM PlayerInfo", PlayerInfo.class);
            players = query.list();
        } catch (Exception e) {
            // Fallback to in-memory if database access fails
            players = new ArrayList<>(allPlayers.values());
            e.printStackTrace();
        }

        return players;
    }
}
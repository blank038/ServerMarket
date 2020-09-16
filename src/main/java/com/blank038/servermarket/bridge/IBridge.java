package com.blank038.servermarket.bridge;

import org.bukkit.entity.Player;

public interface IBridge {

    double balance(Player player);

    void give(Player player, double amount);

    boolean take(Player player, double amount);
}

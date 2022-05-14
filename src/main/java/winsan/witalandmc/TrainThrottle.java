package winsan.witalandmc;

import com.bergerkiller.bukkit.tc.properties.CartProperties;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Objects;

public class TrainThrottle implements Listener, CommandExecutor {

    HashMap<Player, Byte> modeHashMap = new HashMap<>();
    HashMap<Player, ItemStack[]> invHashMap = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String s, String[] args) {
        if (command.getName().equalsIgnoreCase("traindrive")) {
            if (sender instanceof Player && sender.hasPermission("train.throttle")) {

                if (args.length == 1 && args[0].equalsIgnoreCase("enable")) {
                    Player player = (Player) sender;
                    if (modeHashMap.get(player) != null) {
                        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + "Sorry, but your throttle is already activated.");
                    }
                    else {
                        modeHashMap.put(player, (byte) 0);
                        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Throttle has been activated.");
                        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.AQUA + "Please run "+ChatColor.YELLOW+"/train claim"+ChatColor.AQUA+" in order if you not yet claimed this train to start.");
                        ItemStack[] playerHB = new ItemStack[9];
                        for (int i = 0; i < 9; i++) {
                            playerHB[i] = player.getInventory().getItem(i);
                        }
                        invHashMap.put(player, playerHB);
                    }

                    inventoryhotbar(player);
                    return true;
                }

                else if (args.length == 1 && args[0].equalsIgnoreCase("disable")) {
                    Player player = (Player) sender;
                    if (modeHashMap.get(player) == null) {
                        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + "Throttle wasn't activated. Or already deactivated.");
                    } else {
                        modeHashMap.remove(player);


                        ItemStack[] playerHB = invHashMap.get(player);
                        for (int i = 0; i < 9; i++) {
                            player.getInventory().setItem(i, playerHB[i]);
                        }

                        sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.RED + "Throttle has been deactivated.");
                        CartProperties cartProperties = CartProperties.getEditing(player);
                        TrainProperties properties = cartProperties != null ? cartProperties.getTrainProperties() : null;
                        assert properties != null;
                        if (properties.matchTag("auto")) {
                            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.AQUA + "The train is still in Auto Mode. So it wasn't emergency brake.");
                        }
                        else {
                            properties.setSpeedLimit(0);
                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public void inventoryhotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, new ItemStack(Material.AIR, 0));
        }

        ItemStack item = new ItemStack(Material.RED_WOOL, 1);
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName("§cBrake 3");
        item.setItemMeta(itemMeta);
        player.getInventory().setItem(1, item);

        item.setType(Material.ORANGE_WOOL);
        itemMeta.setDisplayName("§6Brake 2");
        item.setItemMeta(itemMeta);
        player.getInventory().setItem(2, item);

        item.setType(Material.YELLOW_WOOL);
        itemMeta.setDisplayName("§eBrake 1");
        item.setItemMeta(itemMeta);
        player.getInventory().setItem(3, item);

        item.setType(Material.LIME_WOOL);
        itemMeta.setDisplayName("§aNeutral");
        item.setItemMeta(itemMeta);
        player.getInventory().setItem(4, item);

        item.setType(Material.LIGHT_BLUE_WOOL);
        itemMeta.setDisplayName("§bPower 1");
        item.setItemMeta(itemMeta);
        player.getInventory().setItem(5, item);

        item.setType(Material.BLUE_WOOL);
        itemMeta.setDisplayName("§1Power 2");
        item.setItemMeta(itemMeta);
        player.getInventory().setItem(6, item);

        item.setType(Material.PURPLE_WOOL);
        itemMeta.setDisplayName("§5Power 3");
        item.setItemMeta(itemMeta);
        player.getInventory().setItem(7, item);

    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        if (modeHashMap.containsKey(event.getPlayer())) {
            emergencyBrake(event.getPlayer());
        }
    }

    public void emergencyBrake(Player player) {
        CartProperties cartProperties = CartProperties.getEditing(player);
        TrainProperties properties = cartProperties != null ? cartProperties.getTrainProperties() : null;
        assert properties != null;
        if (properties.matchTag("auto")) {
            modeHashMap.remove(player);
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.RED + "Throttle has been deactivated.");
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.AQUA + "The train is still in Auto Mode. So it wasn't apply an emergency brake.");
            ItemStack[] playerHB = invHashMap.get(player);
            for (int i = 0; i < 9; i++) {
                player.getInventory().setItem(i, playerHB[i]);
            }
        }
        else {
            properties.setSpeedLimit(0);
            modeHashMap.remove(player);
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "TrainThrottle" + ChatColor.GRAY + "] " + ChatColor.RED + "Throttle has been deactivated.");
            ItemStack[] playerHB = invHashMap.get(player);
            for (int i = 0; i < 9; i++) {
                player.getInventory().setItem(i, playerHB[i]);
            }
        }
        
    }


    @EventHandler
    public void dismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {

            if (modeHashMap.containsKey(player)) {
                emergencyBrake(player);
            }
        }
    }

    @EventHandler
    public void cancelDrop(PlayerDropItemEvent event) {
        if (modeHashMap.containsKey(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void die(PlayerDeathEvent event) {
        if (modeHashMap.containsKey(event.getEntity()))
            emergencyBrake(event.getEntity());
    }

    @EventHandler
    public void shutdown(PlayerDeathEvent event) {
        if (modeHashMap.containsKey(event.getEntity()))
            emergencyBrake(event.getEntity());
    }

    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();
        if ((action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) && modeHashMap.containsKey(player)) {
            event.setCancelled(player.getInventory().getItemInMainHand().getItemMeta() != null);
        } else if ((action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) && modeHashMap.containsKey(player)) {
            event.setCancelled(true);
        }
    }

    
    public void throttleloop() {

        for (Player player : modeHashMap.keySet()) {
            CartProperties cartProperties = CartProperties.getEditing(player);
            TrainProperties properties = cartProperties != null ? cartProperties.getTrainProperties() : null;
            if (properties != null && properties.hasOwners() && properties.getOwners().contains(player.getName().toLowerCase())) {
                if (modeHashMap.containsKey(player)) {
                    switch (player.getInventory().getHeldItemSlot()) {
                        case 1 -> {
                            if (properties.matchTag("neutral")) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED+"ELECTRIC FAULT"+ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick"));
                            }
                            else if (properties.matchTag("ats")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "ATS" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Brake"));
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.020, 0));
                            }
                            else if (properties.matchTag("auto")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + "ATO" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Control"));
                            }
                            else {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "B3" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "-5"));
                            }
                        }
                        case 2 -> {
                            if (properties.matchTag("neutral")) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.002, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED+"ELECTRIC FAULT"+ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE+ " blocks/tick"));
                            }
                            else if (properties.matchTag("ats")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "ATS" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Brake"));
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.020, 0));
                            }
                            else if (properties.matchTag("auto")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + "ATO" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Control"));
                            }
                            else {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.002, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GOLD + "B2" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "-2"));
                            }
                        }
                        case 3 -> {
                            if (properties.matchTag("neutral")) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.001, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED+"ELECTRIC FAULT"+ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick"));
                            }
                            else if (properties.matchTag("ats")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "ATS" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Brake"));
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.020, 0));
                            }
                            else if (properties.matchTag("auto")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + "ATO" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Control"));
                            }
                            else {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.001, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + "B1" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "-1"));
                            }
                        }
                        case 4 -> {
                            if (properties.matchTag("neutral")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED+"ELECTRIC FAULT"+ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick"));
                                if (Objects.requireNonNull(properties).getSpeedLimit() < 0.0125) {
                                    properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.00005, 0));}
                                else
                                    properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.00005, 0));
                            }
                            else if (properties.matchTag("ats")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "ATS" +ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Brake"));
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.020, 0));
                            }
                            else if (properties.matchTag("auto")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + "ATO" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Control"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.0125) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.00005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "N" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "0"));
                            }
                            else {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.00005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "N" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "0"));
                            }
                        }
                        case 5 -> {
                            if (properties.matchTag("neutral")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED+"ELECTRIC FAULT"+ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick"));
                            }
                            else if (properties.matchTag("ats")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "ATS" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Brake"));
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.020, 0));
                            }
                            else if (properties.matchTag("auto")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + "ATO" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Control"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.05) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.AQUA + "P1" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "5"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.15) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.004, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.AQUA + "P1" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "4"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.30) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.003, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.AQUA + "P1" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "3"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.45) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.002, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.AQUA + "P1" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "2"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.60) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.001, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.AQUA + "P1" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "1"));
                            }
                            else {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.00005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.AQUA + "P1" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "0"));
                            }
                        }
                        case 6 -> {
                            if (properties.matchTag("neutral")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED+"ELECTRIC FAULT"+ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick"));
                            }
                            else if (properties.matchTag("ats")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "ATS" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Brake"));
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.020, 0));
                            }
                            else if (properties.matchTag("auto")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + "ATO" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Control"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.15) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_BLUE + "P2" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "5"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.20) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.004, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_BLUE + "P2" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "4"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.40) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.003, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_BLUE + "P2" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "3"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.60) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.002, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_BLUE + "P2" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "2"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.80) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.001, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_BLUE + "P2" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "1"));
                            }
                            else {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.00005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_BLUE + "P2" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "0"));
                            }
                        }
                        case 7 -> {
                            if (properties.matchTag("neutral")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED+"ELECTRIC FAULT"+ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick"));
                            }
                            else if (properties.matchTag("ats")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_RED + "ATS" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Brake"));
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.020, 0));
                            }
                            else if (properties.matchTag("auto")) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + "ATO" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "Auto Control"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.15) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "P3" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "5"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 0.80) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.004, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "P3" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "4"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 1.00) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.003, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "P3" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "3"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 1.20) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.002, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "P3" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "2"));
                            }
                            else if (Objects.requireNonNull(properties).getSpeedLimit() < 1.60) {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() + 0.001, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "P3" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "1"));
                            }
                            else {
                                properties.setSpeedLimit(Math.max(properties.getSpeedLimit() - 0.00005, 0));
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_PURPLE + "P3" + ChatColor.WHITE + " | Speed: " + ChatColor.LIGHT_PURPLE + String.format("%.2f", properties.getSpeedLimit()) + ChatColor.WHITE + " blocks/tick | Acceleration: "+ChatColor.LIGHT_PURPLE+ "0"));
                            }
                        }
                    }
                }


            }
        }
    }
}

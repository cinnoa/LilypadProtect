package com.nateandfriends.lilypadboatguard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class LilyPadBoatGuardPlugin extends JavaPlugin implements Listener {

    private boolean enabled;
    private boolean preventBoatBreakingLilyPads;
    private boolean restoreAfterCollision;
    private boolean debug;
    private long restoreDelayTicks;

    private final Set<String> protectedBoatTypes = new HashSet<>();
    private final Set<Material> protectedBlockTypes = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("LilyPadBoatGuard enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("LilyPadBoatGuard disabled.");
    }

    private void loadSettings() {
        reloadConfig();
        FileConfiguration config = getConfig();

        enabled = config.getBoolean("enabled", true);
        preventBoatBreakingLilyPads = config.getBoolean("prevent-boat-breaking-lily-pads", true);
        restoreAfterCollision = config.getBoolean("restore-lily-pad-after-collision", true);
        debug = config.getBoolean("debug", false);
        restoreDelayTicks = Math.max(1L, config.getLong("restore-delay-ticks", 1L));

        protectedBoatTypes.clear();
        for (String typeName : config.getStringList("protected-boat-types")) {
            if (typeName != null && !typeName.isBlank()) {
                protectedBoatTypes.add(typeName.trim().toUpperCase(Locale.ROOT));
            }
        }
        if (protectedBoatTypes.isEmpty()) {
            protectedBoatTypes.add("BOAT");
            protectedBoatTypes.add("CHEST_BOAT");
        }

        protectedBlockTypes.clear();
        for (String materialName : config.getStringList("protected-block-types")) {
            Material material = Material.matchMaterial(materialName == null ? "" : materialName.trim());
            if (material == null) {
                getLogger().warning("Unknown material in protected-block-types: " + materialName);
                continue;
            }
            protectedBlockTypes.add(material);
        }
        if (protectedBlockTypes.isEmpty()) {
            protectedBlockTypes.add(Material.LILY_PAD);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!enabled || !preventBoatBreakingLilyPads) return;
        if (!isProtectedBoat(event.getEntity())) return;
        if (!isProtectedBlock(event.getBlock())) return;

        event.setCancelled(true);
        debug("Cancelled boat block-change/break event at " + formatLocation(event.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
        if (!enabled || !restoreAfterCollision) return;
        if (!isProtectedBoat(event.getVehicle())) return;

        Block block = event.getBlock();
        if (block == null || !isProtectedBlock(block)) return;

        Material originalType = block.getType();
        Location location = block.getLocation();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            Block currentBlock = location.getBlock();
            if (currentBlock.getType() == Material.AIR || currentBlock.isEmpty()) {
                currentBlock.setType(originalType, false);
                debug("Restored protected block at " + formatLocation(location));
            }
        }, restoreDelayTicks);
    }

    private boolean isProtectedBoat(Entity entity) {
        return entity != null && protectedBoatTypes.contains(entity.getType().name());
    }

    private boolean isProtectedBlock(Block block) {
        return block != null && protectedBlockTypes.contains(block.getType());
    }

    private void debug(String message) {
        if (debug) {
            getLogger().info("[Debug] " + message);
        }
    }

    private String formatLocation(Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            loadSettings();
            sender.sendMessage("§aLilyPadBoatGuard config reloaded.");
            return true;
        }

        sender.sendMessage("§dLilyPadBoatGuard §7commands:");
        sender.sendMessage("§f/" + label + " reload §7- Reload the config");
        return true;
    }
}

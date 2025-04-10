package me.rhydium.rKitPvP.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.rhydium.rKitPvP.rKitPvP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitSelectorGUI implements Listener {

    private final rKitPvP plugin;
    private static final Map<String, FileConfiguration> kits = new HashMap<>();

    public KitSelectorGUI(rKitPvP plugin) {
        this.plugin = plugin;
        loadKits(plugin);
    }

    public void loadKits(rKitPvP plugin) {
        File kitsFolder = new File(plugin.getDataFolder(), "kits");
        if (!kitsFolder.exists()) {
            if (kitsFolder.mkdirs()) {
                createDefaultKit(kitsFolder);
            } else {
                plugin.getLogger().severe("Failed to create kits directory.");
                return;
            }
        }

        File[] kitFiles = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (kitFiles != null) {
            for (File kitFile : kitFiles) {
                FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);
                String kitId = kitConfig.getString("kitId", kitFile.getName().replace(".yml", "").toLowerCase());
                kits.put(kitId, kitConfig);
            }
        }
    }

    private void createDefaultKit(File kitsFolder) {
        File defaultKitFile = new File(kitsFolder, "default_kit.yml");
        if (!defaultKitFile.exists()) {
            FileConfiguration defaultKit = YamlConfiguration.loadConfiguration(defaultKitFile);
            defaultKit.set("kitId", "default_kit"); // Unique ID for the kit
            defaultKit.set("name", "§bDefault Kit");
            defaultKit.set("type", "free");
            defaultKit.set("icon", "IRON_SWORD");
            defaultKit.set("lore", List.of("§7A basic starter kit", "§7Contains essential items."));

            // Items
            Map<String, Object> sword = new HashMap<>();
            sword.put("material", "IRON_SWORD");
            sword.put("amount", 1);
            sword.put("name", "§bStarter Sword");
            sword.put("lore", List.of("§7A trusty iron sword."));
            sword.put("enchantments", Map.of("SHARPNESS", 1));

            Map<String, Object> apple = new HashMap<>();
            apple.put("material", "GOLDEN_APPLE");
            apple.put("amount", 3);

            defaultKit.set("items", List.of(sword, apple));

            // Potions
            Map<String, Object> speedPotion = new HashMap<>();
            speedPotion.put("type", "SWIFTNESS");
            speedPotion.put("duration", 600);
            speedPotion.put("amplifier", 1);
            speedPotion.put("ambient", false);
            speedPotion.put("particles", true);

            defaultKit.set("potions", List.of(speedPotion));

            try {
                defaultKit.save(defaultKitFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create default kit file: " + e.getMessage());
            }
        }
    }

    public void openKitSelector(Player player) {
        Inventory kitSelector = Bukkit.createInventory(null, 54, Component.text("Select your Kit!", NamedTextColor.GOLD));

        // Add gray stained-glass panes as border
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = grayPane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.customName(Component.text(" "));
            grayPane.setItemMeta(paneMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                kitSelector.setItem(i, grayPane);
            }
        }

        // Add barrier item to close the menu
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.customName(Component.text("Close", NamedTextColor.RED));
            closeItem.setItemMeta(closeMeta);
        }
        kitSelector.setItem(49, closeItem);

        // Add kits to the inventory in the specified order
        int freeSlot = 20;
        int coinSlot = 22;
        int rankSlot = 24;

        for (Map.Entry<String, FileConfiguration> entry : kits.entrySet()) {
            FileConfiguration kitConfig = entry.getValue();
            String kitName = kitConfig.getString("name", "Unknown Kit");
            String type = kitConfig.getString("type", "free");
            String kitId = kitConfig.getString("kitId", entry.getKey());

            ItemStack kitItem = new ItemStack(Material.valueOf(kitConfig.getString("icon", "IRON_SWORD")));
            ItemMeta kitMeta = kitItem.getItemMeta();
            if (kitMeta != null) {
                kitMeta.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(kitName));

                List<String> rawLore = kitConfig.getStringList("lore");
                List<TextComponent> loreComponents = new java.util.ArrayList<>(rawLore.stream()
                        .map(line -> LegacyComponentSerializer.legacySection().deserialize(line))
                        .toList());
                kitMeta.lore(loreComponents);

                NamespacedKey key = new NamespacedKey(rKitPvP.getPlugin(rKitPvP.class), "kitId");
                kitMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, kitId);

                kitItem.setItemMeta(kitMeta);
            }

            switch (type.toLowerCase()) {
                case "free":
                    kitSelector.setItem(freeSlot++, kitItem);
                    break;
                case "coin":
                    kitSelector.setItem(coinSlot++, kitItem);
                    break;
                case "rank":
                    kitSelector.setItem(rankSlot++, kitItem);
                    break;
            }
        }

        player.openInventory(kitSelector);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || !(event.getAction().name().contains("RIGHT_CLICK_AIR") || event.getAction().name().contains("RIGHT_CLICK_BLOCK"))) {
            return;
        }
        if (!event.hasItem() || !event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

//        if (item != null && item.getType() == Material.CHEST && item.getItemMeta() != null && ChatColor.translateAlternateColorCodes('&', "&6&lKit Selector").equals(item.getItemMeta().getDisplayName())) {
        if (item != null && item.getType() == Material.CHEST && item.getItemMeta() != null && LegacyComponentSerializer.legacyAmpersand().deserialize("&6&lKit Selector").equals(item.getItemMeta().displayName())) {
            openKitSelector(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(LegacyComponentSerializer.legacyAmpersand().deserialize("&6Select your Kit!"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                ItemMeta meta = clickedItem.getItemMeta();
                NamespacedKey key = new NamespacedKey(rKitPvP.getPlugin(rKitPvP.class), "kitId");
                String kitId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                if (kitId != null) {
                    FileConfiguration selectedKit = kits.get(kitId);

                    if (selectedKit == null) {
                        player.sendMessage("§cKit configuration for '" + kitId + "' not found.");
                        return;
                    }

                    // Clear inventory of other items before giving kit items
                    player.getInventory().clear();

                    List<Map<?, ?>> items = selectedKit.getMapList("items");
                    for (Map<?, ?> itemData : items) {
                        try {
                            Material material = Material.getMaterial((String) itemData.get("material"));
                            if (material == null) {
                                player.sendMessage("§cInvalid material in kit: " + itemData.get("material"));
                                continue;
                            }
                            int amount = itemData.containsKey("amount") ? ((Number) itemData.get("amount")).intValue() : 1;
                            ItemStack item = new ItemStack(material, amount);

                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null) {
                                if (itemData.containsKey("name")) {
//                                    itemMeta.setDisplayName((String) itemData.get("name"));
                                    itemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize((String) itemData.get("name")));
                                }
                                if (itemData.containsKey("lore")) {
                                    Object loreObj = itemData.get("lore");
                                    List<String> legacyLore = new ArrayList<>();

                                    if (loreObj instanceof List<?> rawList) {
                                        for (Object line : rawList) {
                                            if (line instanceof String str) {
                                                legacyLore.add(str);
                                            }
                                        }
                                    }
                                    List<TextComponent> newLore = legacyLore.stream()
                                            .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
                                            .toList();
                                    itemMeta.lore(newLore);
                                }
                                if (itemData.containsKey("enchantments")) {
                                    Object enchObj = itemData.get("enchantments");

                                    if (enchObj instanceof Map<?, ?> rawMap) {
                                        Registry<@NotNull Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

                                        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                                            if (entry.getKey() instanceof String enchantmentKey && entry.getValue() instanceof Number levelNumber) {
                                                NamespacedKey enchantmentNameSpaceKey = NamespacedKey.minecraft(enchantmentKey.toLowerCase());
                                                Enchantment enchantment = enchantmentRegistry.get(enchantmentNameSpaceKey);

                                                if (enchantment != null) {
                                                    itemMeta.addEnchant(enchantment, levelNumber.intValue(), true);
                                                } else {
                                                    plugin.getLogger().warning("Invalid enchantment: " + enchantmentKey);
                                                }
                                            }
                                        }
                                    }
                                }
                                item.setItemMeta(itemMeta);
                            }

                            // Debug: Confirm item before adding
                            player.sendMessage("§aAdding item: " + item.getType() + " x" + amount);
                            player.getInventory().addItem(item);

                        } catch (Exception e) {
                            player.sendMessage("§cError adding item to inventory.");
                            plugin.getLogger().warning("Error adding item to inventory: " + e.getMessage());
                        }
                    }

                    // Apply potion effects if any
                    List<Map<?, ?>> potions = selectedKit.getMapList("potions");

                    Registry<@NotNull PotionType> potionRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.POTION);
                    for (Map<?, ?> potionData : potions) {
                        Object typeObj = potionData.get("type");
                        if (!(typeObj instanceof String typeStr)) {
                            player.sendMessage("§cInvalid potion effect type (not a string).");
                            continue;
                        }

                        NamespacedKey potionEffectNamespacedKey = NamespacedKey.minecraft(typeStr.toLowerCase());
                        PotionType potionType = potionRegistry.get(potionEffectNamespacedKey);

                        if (potionType == null) {
                            player.sendMessage("§cInvalid potion type in kit: " + typeStr);
                            continue;
                        }

                        for (PotionEffect effect : potionType.getPotionEffects()) {
                            PotionEffectType type = effect.getType();

                            int duration = potionData.containsKey("duration")
                                    ? ((Number) potionData.get("duration")).intValue()
                                    : effect.getDuration(); // fallback naar originele waarde

                            int amplifier = potionData.containsKey("amplifier")
                                    ? ((Number) potionData.get("amplifier")).intValue()
                                    : effect.getAmplifier(); // fallback naar originele waarde

                            boolean ambient = potionData.containsKey("ambient")
                                    ? (Boolean) potionData.get("ambient")
                                    : effect.isAmbient();

                            boolean particles = potionData.containsKey("particles")
                                    ? (Boolean) potionData.get("particles")
                                    : effect.hasParticles();

                            PotionEffect customEffect = new PotionEffect(type, duration, amplifier, ambient, particles);
                            player.addPotionEffect(customEffect);
                            player.sendMessage("§aAdded potion effect: " + type.translationKey());
                        }
                    }

                    player.sendMessage("§aKit selected successfully!");
                    player.closeInventory();
                }
            }
        }
    }

    public static void givePlayerKitSelectorItem(Player player) {
        ItemStack kitSelector = new ItemStack(Material.CHEST);
        ItemMeta meta = kitSelector.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&6&lKit Selector"));
            kitSelector.setItemMeta(meta);
        }

        player.getInventory().setItem(0, kitSelector);
    }
}
package sh.tmb.EpicSpleef.objects;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.EpicSpleef;

import java.util.UUID;

public class Powerup {

    private EpicSpleef plugin;
    private String name;
    private Material type;

    private int secondsTillAppear;
    private boolean activated = false;
    private boolean assigned = false;
    private boolean countdownFinished = false;
    private Countdown countdown;
    private int id;
    private Location spawnLocation;
    private ChatColor nameColor;
    private UUID holder;
    private Item entity;
    private ItemStack item;
    private FloatingDisplay displayName;
    private FloatingDisplay displayCountdown;

    public Powerup(EpicSpleef plugin, int id, String name, Material type, ChatColor nameColor, Location spawnLocation, int countdown) {
        this.plugin = plugin;
        this.name = name.toUpperCase();
        this.type = type;
        this.nameColor = nameColor;
        this.spawnLocation = spawnLocation;
        this.secondsTillAppear = countdown;
        this.id = id;
    }

    public void spawn() {
        // make entity appear
        displayName = new FloatingDisplay(plugin, ChatColor.GREEN + "Powerup: " + nameColor + ChatColor.BOLD + name, spawnLocation.clone().add(0, .5, 0));
        displayCountdown = new FloatingDisplay(plugin, "Spawning in " + ChatColor.AQUA + ChatColor.BOLD + secondsTillAppear + ChatColor.RESET + " seconds", spawnLocation.clone().add(0, .25, 0));

        countdown = new Countdown(plugin, secondsTillAppear, (x) -> displayCountdown.setText("Spawning in " + ChatColor.AQUA + ChatColor.BOLD + x + ChatColor.RESET + " seconds"), () -> {
            countdownFinished = true;
            displayCountdown.remove();
            afterCountdown();
        });
    }

    private void afterCountdown() {
        // drop actual item
        initializeItem();
        Location itemSpawn = spawnLocation.clone().add(0,1,0);
        entity = spawnLocation.getWorld().dropItem(itemSpawn, item);
        entity.setGravity(false);
        entity.setVelocity(new Vector());
        entity.setInvulnerable(true);

        // with fireworks
        Firework fw = (Firework) spawnLocation.getWorld().spawnEntity(itemSpawn, EntityType.FIREWORK);
        FireworkMeta fwMeta = fw.getFireworkMeta();
        fwMeta.setPower(2);
        fwMeta.addEffect(FireworkEffect.builder().withColor(Color.RED).build());
        fw.setFireworkMeta(fwMeta);
        fw.detonate();
    }

    private void initializeItem() {
        item = new ItemStack(type, 1);
        ItemMeta powerupMeta = item.getItemMeta();
        powerupMeta.setDisplayName(nameColor + ""+ ChatColor.BOLD + name + ChatColor.RESET +" — " + ChatColor.GREEN + "Powerup");
        powerupMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        powerupMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "powerupId"), PersistentDataType.INTEGER, id);
        item.setItemMeta(powerupMeta);
    }

    public void assignToPlayer(Player p) throws Exception {
        if (this.holder != null) {
            throw new Exception("Powerups can only belong to one player!");
        }
        this.holder = p.getUniqueId();
        displayName.remove();
        entity.remove();
        p.getInventory().setItem(1, item);
        p.playSound(p.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1.5F, 1.5F);
        p.sendMessage(ChatColor.GREEN + "You just picked up a " + nameColor + ChatColor.BOLD + name + ChatColor.RESET + ChatColor.GREEN + " Powerup! " + ChatColor.AQUA + ChatColor.BOLD +"Use it by switching to your second item slot.");
        assigned = true;
    }

    public void activate() {
        plugin.getServer().getPlayer(holder).getInventory().remove(item);
        activated = true;
    }

    public void cleanup() {
        if (!countdownFinished && !assigned) {
            countdown.cancel();
            displayCountdown.remove();
            displayName.remove();
        }

        if (!assigned && countdownFinished) {
            displayName.remove();
            entity.remove();
        }

        if (assigned && !activated) {
            plugin.getServer().getPlayer(holder).getInventory().remove(item);
            this.holder = null;
        }
    }

    public UUID getPlayer() {
        return holder;
    }

    public ItemStack getItem() {
        return item;
    }

    public Location getSpawnLocation() { return spawnLocation.clone(); }

    public EpicSpleef getPlugin() {
        return plugin;
    }


}

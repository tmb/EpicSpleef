package sh.tmb.EpicSpleef.managers;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.listeners.EndingListener;
import sh.tmb.EpicSpleef.listeners.PlayingListener;
import sh.tmb.EpicSpleef.listeners.StartingListener;
import sh.tmb.EpicSpleef.listeners.WaitingListener;
import sh.tmb.EpicSpleef.objects.Countdown;
import sh.tmb.EpicSpleef.objects.SpleefPlayer;
import sh.tmb.EpicSpleef.scoreboard.ScoreboardProvider;
import sh.tmb.EpicSpleef.scoreboard.providers.GameProvider;
import sh.tmb.EpicSpleef.scoreboard.providers.LobbyProvider;
import sh.tmb.EpicSpleef.utilities.CircleGenerator;

import java.util.*;
import java.util.stream.Collectors;


public class GameManager {

    public EpicSpleef plugin;
    public PowerupManager pm;

    private int playersRequired;
    private GameStatus status;

    private HashMap<UUID, SpleefPlayer> users;

    private WaitingListener waitingListener;
    private StartingListener startingListener;
    private PlayingListener playingListener;
    private EndingListener endingListener;

    private ScoreboardProvider gameProvider = new GameProvider(this);
    private ScoreboardProvider lobbyProvider = new LobbyProvider(this);

    private MapManager mm;
    private boolean loadingMap;

    // location and who broke it + when it was broken
    private HashMap<Location, AbstractMap.SimpleEntry<UUID, Long>> breakMap;

    private final String bossBarString = ChatColor.BOLD + "" + ChatColor.RED + "E" + ChatColor.GREEN + "P" + ChatColor.BLUE + "I" + ChatColor.GOLD + "C" + ChatColor.AQUA + " SPLEEF" + ChatColor.WHITE + " — ";
    private Integer powerupGeneratorId = null;
    private Countdown tillStart;
    private BossBar gameBar;
    private UUID winner;
    private Random r = new Random();

    public GameManager(EpicSpleef plugin) {
        status = GameStatus.WAITING;
        this.plugin = plugin;
        this.playersRequired = plugin.getServer().getOnlinePlayers().size() >= 5 ? plugin.getServer().getOnlinePlayers().size() + 2 : 5;
        this.users = new HashMap<>();
        this.waitingListener = new WaitingListener(this);
        this.startingListener = new StartingListener(this);
        this.playingListener = new PlayingListener(this);
        this.endingListener = new EndingListener(this);
        this.mm = new MapManager(this);
        this.breakMap = new HashMap<>();
        this.pm = new PowerupManager(plugin);
        this.gameBar = plugin.getServer().createBossBar(bossBarString + "0/" + playersRequired + " ready", BarColor.PINK, BarStyle.SOLID);
    }

    private List<SpleefPlayer> players() {
        return users.isEmpty() ? new ArrayList<>() : users.values().stream().filter((p) -> p.getMode() == SpleefPlayer.SpleefPlayerMode.PLAYER).collect(Collectors.toList());
    }

    private List<SpleefPlayer> spectators() {
        return users.isEmpty() ? new ArrayList<>() : users.values().stream().filter((p) -> p.getMode() != SpleefPlayer.SpleefPlayerMode.PLAYER).collect(Collectors.toList());
    }

    // set up
    public void setup() {
        mm.intialize();
        Team team = plugin.getServer().getScoreboardManager().getMainScoreboard().getTeam("team");

        if (team == null) {
            team = plugin.getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("team");
        }
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        setStatus(GameStatus.WAITING);
        plugin.getServer().getOnlinePlayers().forEach(this::joinPlayer);
    }

    // waiting
    public void waitForPlayers() {
        // change all players and dead players to normal players & clear inventories
        for (SpleefPlayer u : users.values()) {
            u.getSpigotPlayer().teleport(plugin.getServer().getWorld("world").getSpawnLocation());
            u.resetPlayer();
            if (u.getMode() != SpleefPlayer.SpleefPlayerMode.SPECTATOR) {
                u.setMode(SpleefPlayer.SpleefPlayerMode.PLAYER);
            }
            u.getSpigotPlayer().getInventory().clear();
            u.getScoreboard().setProvider(getScoreboardProvider());
        }

        moveSpectatorsToPlayers();
        updateBossBar();


        Runnable after = () -> {
            this.loadingMap = false;
            plugin.getLogger().info("finished loading map!");
            tryStartGameCountdown();
        };

        this.loadingMap = true;
        asyncUnloadThenLoadNewMap(after);
    }

    // starting the game
    public void startGame() {
        getUsers().forEach((p) -> p.getSpigotPlayer().teleport(mm.getSpawn().add(.5, 0, .5)));

        updateBossBar();
        // declare shovels
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL, 1);
        ItemMeta shovelMeta = shovel.getItemMeta();

        shovelMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Spleef Shovel");
        shovelMeta.setUnbreakable(true);

        shovel.setItemMeta(shovelMeta);

        players().forEach((sp) -> {
            sp.getScoreboard().setProvider(getScoreboardProvider());
            Player p = sp.getSpigotPlayer();
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().setItem(0, shovel);
            p.getInventory().setHeldItemSlot(0);
        });

        new Countdown(plugin, 3, (x) -> users.values().forEach((sp) -> {
            Player p = sp.getSpigotPlayer();
            p.sendTitle("Spleefing starts in " + ChatColor.YELLOW + x, "Start moving around!", 0, 25, 0);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1L, 1L);
        }), () -> setStatus(GameStatus.PLAYING), 1);
    }

    public void runGame() {
        users.values().forEach((sp) ->
        {
            sp.getSpigotPlayer().sendTitle("GO!", ChatColor.YELLOW + "Start spleefing!", 0, 20 * 3, 10);
            sp.getSpigotPlayer().playSound(sp.getSpigotPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        });
        ArrayList<Location> possibleSpawnLocs = new ArrayList<>();

        for (int i = 1; i <= mm.getMapRadius(); i++) {
            possibleSpawnLocs.addAll(CircleGenerator.generateCircle(mm.getSpawn(), i, CircleGenerator.Plane.XZ, false, false));
        }

        powerupGeneratorId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Location loc = possibleSpawnLocs.get(r.nextInt(possibleSpawnLocs.size()));
            while (pm.powerupAtLocation(loc) || !mm.getSpleefBlocks().contains(loc.getBlock().getRelative(BlockFace.DOWN).getType())) {
                loc = possibleSpawnLocs.get(r.nextInt(possibleSpawnLocs.size()));
            }
            pm.createRandomPowerup(loc);
        }, 20L, 20L * 6);


    }

    // end the game
    public void endGame() {
        // CLEAN UP MAP

        // stop powerup generation
        if (powerupGeneratorId != null) {
            Bukkit.getScheduler().cancelTask(powerupGeneratorId);
            powerupGeneratorId = null;
        }

        // remove all existing powerups
        pm.removeAll();
        breakMap.clear();

        mm.getMapPlatformCenter().getBlock().setType(mm.getSpleefBlocks().stream().findFirst().get());

        // STOP WINNER FROM FALLING
        getPlayers().forEach((sp) -> {
            Player p = sp.getSpigotPlayer();
            if (p.getLocation().getY() < mm.getMapPlatformCenter().getY()) {
                p.setFallDistance(0);
                p.teleport(mm.getSpawn());
            }
        });

        // NOTIFY PLAYERS OF GAME END

        getUsers().forEach((sp) -> {
            Player p = sp.getSpigotPlayer();
            p.sendTitle(ChatColor.GREEN + (winner != null ? users.get(winner).getSpigotPlayer().getName() : "No one"), "won the game!", 10, 20 * 5, 10);
        });

        updateBossBar();

        // ADD BLOCKS BROKEN TO SPLEEF PLAYER
        HashMap<UUID, Integer> brokenPerPlayer = new HashMap<>();
        breakMap.forEach((l, s) -> brokenPerPlayer.compute(s.getKey(), (k, v) -> (v == null) ? 1 : v + 1));
        brokenPerPlayer.forEach((u, i) -> getSpleefPlayerFromUUID(u).addBlocksBroken(i));

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            getUsers().forEach((sp) -> sp.getSpigotPlayer().teleport(plugin.getServer().getWorld("world").getSpawnLocation()));
            setStatus(GameStatus.WAITING);
            winner = null;
        }, 20L * 5);
    }

    public void tryStartGameCountdown() {
        if (players().size() == playersRequired && tillStart == null && getStatus() == GameStatus.WAITING && !loadingMap) {
            tillStart = new Countdown(plugin, 5, (x) -> users.values().forEach((sp) -> {
                Player pl = sp.getSpigotPlayer();
                pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "Game starting in " + ChatColor.YELLOW + x + ChatColor.GREEN + " seconds"));
                pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1L, 1L);
            }), () -> {
                tillStart = null;
                setStatus(GameStatus.STARTING);

            });
        }
    }

    // goes through each spectator and changes them to a player if there's space.
    public void moveSpectatorsToPlayers() {
        while (players().size() < playersRequired && spectators().size() > 0) {
            spectators().get(0).setMode(SpleefPlayer.SpleefPlayerMode.PLAYER);
        }
    }

    public void joinPlayer(Player p) {
        plugin.getServer().getScoreboardManager().getMainScoreboard().getTeam("team").addPlayer(p);

        // if we're full on players OR not waiting, add player to spectator mode.
        if (tillStart != null && tillStart.getActive() || getStatus() != GameStatus.WAITING) {
            users.put(p.getUniqueId(), new SpleefPlayer(p, SpleefPlayer.SpleefPlayerMode.SPECTATOR, this));
            if (getStatus() != GameStatus.WAITING) {
                p.teleport(mm.getSpawn());
            }
        } else {
            // they're a normal player
            users.put(p.getUniqueId(), new SpleefPlayer(p, SpleefPlayer.SpleefPlayerMode.PLAYER, this));
            p.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
        }

        gameBar.addPlayer(p);
        updateBossBar();
        tryStartGameCountdown();
    }

    public void exitPlayer(Player p) {
        users.remove(p.getUniqueId());
        // if someone leaves while the game is still in waiting
        if (getStatus() == GameStatus.WAITING) {
            // if we have less players than we need and the countdown has started, cancel the countdown.
            if (players().size() < playersRequired && tillStart != null && tillStart.getActive()) {
                tillStart.cancel();
                tillStart = null;
                users.values().forEach((sp) -> sp.getSpigotPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("")));
                moveSpectatorsToPlayers();
                tryStartGameCountdown();

            }
            updateBossBar();
        }
    }

    private void updateBossBar() {
        switch (status) {
            case WAITING:
                gameBar.setTitle(bossBarString + players().size() + "/" + playersRequired + " ready");
                gameBar.setProgress((float) players().size() / (float) playersRequired);
                break;
            case STARTING:
                gameBar.setTitle(bossBarString + "IN PROGRESS");
                gameBar.setProgress(1.0);
                break;
            case PLAYING:
                gameBar.setTitle(bossBarString + "IN PROGRESS");
                gameBar.setProgress(1.0);
                break;
            case ENDING:
                gameBar.setTitle(bossBarString + (winner != null ? users.get(winner).getSpigotPlayer().getName() : "No one") + " wins!");
                gameBar.setProgress(1.0);
                break;
            default:
                break;
        }
    }

    private void asyncUnloadThenLoadNewMap(Runnable after) {
        if (mm.hasMap()) {
            String prevMap = mm.getMapName();
            mm.unloadMap(() -> mm.loadRandomMap(prevMap, after));
        } else {
            mm.loadRandomMap("", after);
        }
    }


    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        HandlerList.unregisterAll(plugin);
        switch (status) {
            case WAITING:
                this.status = GameStatus.WAITING;
                plugin.getServer().getPluginManager().registerEvents(waitingListener, plugin);
                waitForPlayers();
                break;
            case STARTING:
                this.status = GameStatus.STARTING;
                plugin.getServer().getPluginManager().registerEvents(startingListener, plugin);
                startGame();
                break;
            case PLAYING:
                this.status = GameStatus.PLAYING;
                plugin.getServer().getPluginManager().registerEvents(playingListener, plugin);
                runGame();
                break;
            case ENDING:
                this.status = GameStatus.ENDING;
                plugin.getServer().getPluginManager().registerEvents(endingListener, plugin);
                endGame();
            default:
                break;
        }
    }

    public ScoreboardProvider getScoreboardProvider() {
        switch(getStatus()) {
            case WAITING:
                return lobbyProvider;
            case STARTING:
                return gameProvider;
            case PLAYING:
                return gameProvider;
            case ENDING:
                return gameProvider;
            default:
                return lobbyProvider;
        }
    }

    public MapManager getMm() {
        return mm;
    }

    public void setPlayersRequired(int numberOfPlayers) {
        playersRequired = numberOfPlayers;
        if (tillStart != null && tillStart.getActive()) {
            tillStart.cancel();
            tillStart = null;
        }
        moveSpectatorsToPlayers();
        updateBossBar();
        tryStartGameCountdown();
    }

    public void setBlockBroken(Location l, UUID brokenBy) {
        breakMap.put(l, new AbstractMap.SimpleEntry<>(brokenBy, System.currentTimeMillis()));
    }

    public UUID getBlockBrokenBy(Location l) {
        // will only return if it was broken by someone in the last second
        if (breakMap.containsKey(l)) {
            if (System.currentTimeMillis() - breakMap.get(l).getValue() <= 1000) {
                return breakMap.get(l).getKey();
            }
        }
        return null;
    }

    public UUID getWinner() {
        return winner;
    }

    public void setWinner(UUID winner) {
        this.winner = winner;
    }

    public SpleefPlayer getSpleefPlayerFromPlayer(Player p) {
        return users.get(p.getUniqueId());
    }

    public SpleefPlayer getSpleefPlayerFromUUID(UUID uuid) {
        return users.get(uuid);
    }

    public List<SpleefPlayer> getUsers() {
        return users.values().stream().collect(Collectors.toList());
    }

    public List<SpleefPlayer> getPlayers() {
        return players();
    }

    public List<SpleefPlayer> getSpectators() {
        return spectators();
    }

    public void disable() {
        HandlerList.unregisterAll();
        pm.removeAll();
        gameBar.removeAll();
        users.clear();
        plugin.getServer().getOnlinePlayers().forEach((p) -> p.getInventory().clear());
    }

    public enum GameStatus {
        WAITING,
        STARTING,
        PLAYING,
        ENDING
    }

}

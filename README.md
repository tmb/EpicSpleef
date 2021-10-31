# EpicSpleef

My twist on the classic Minecraft minigame Spleef. EpicSpleef adds four powerups to Spleef: teleport, jump, throwable bomb, and knockback snowballs. They can be triggered in game by picking them up on spawn, and switching to the second item slot in your inventory.

To set this up yourself, drop it into a Spigot server (only compatiable with 1.16.5 for now), put all of your spleef maps (named `spleef_[map name]`) under `./spleef_worlds`, and then modify config.yml:

```yaml
maps:
  natural: # name of your map, (spleef_natural, for this one)
    x: 8 # center of the map (also where players will spawn)
    y: 4
    z: 8
    radius: 23 # the plugin assumes your map is circular. this is the radius in which powerups will spawn around the center declared above.
    spleef-blocks: # blocks (org.Bukkit.Material types) that are allowed to be broken during Spleef
      - COBBLESTONE
      - STONE_BRICK
```

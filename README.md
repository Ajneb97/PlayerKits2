# PlayerKits2

Minecraft Plugin that allows you to create a variety of survival kits and put them on a GUI Inventory. Using a command, players will be able to claim these kits if they have the correct permission or even some requirements. Kits can also have a cooldown, so users can't claim them everytime.

What makes PlayerKits 2 special is its easy way to create kits, just having the items on your inventory and executing a command. You can edit kits properties by using /kit edit command, or directly through the config.

**Spigot / Paper 1.8.9+**

## Download Links
- [SpigotMC](https://www.spigotmc.org/resources/playerkits-2-fully-configurable-kits-1-8-26-2.112616/)
- [Modrinth](https://modrinth.com/plugin/playerkits-2)


## Other Links
- [Wiki](https://ajneb97.gitbook.io/playerkits-2)
- [bStats](https://bstats.org/plugin/bukkit/PlayerKits%202/19795)
- [Support me on Ko-fi](https://ko-fi.com/ajneb97)
## Resource pack look (this fork, `1.24.1-projects`)

Every inventory in `inventory.yml` may carry an optional `pack` section. Players who have the server
resource pack (Paper `Player#hasResourcePack()`, never Bedrock players) then get that look; everyone else
keeps the inventory exactly as configured.

```yaml
main_inventory:
  slots: 54
  title: '&8Kits'
  pack:
    title: '&f%img_offset_-48%%img_my_frame%%img_offset_-167%&f[title]'  # PlaceholderAPI, legacy & codes
    max_title_length: 26          # [title] = normal title without colours, cut to this length
  0-53:
    item: {id: BLACK_STAINED_GLASS_PANE, name: ' '}
    pack_hidden: true             # left out for pack players (the texture is the frame)
  49:
    item: {id: BARRIER, name: '&cClose'}
    pack_icon: {id: PAPER, custom_model_data: 84001}   # icon only pack players see (also: model)
  50:
    item: {...}
    pack_only: true               # shown only to pack players
```

Titles of `preview_inventory` and `buy_requirements_inventory` also accept `%kit_display%` (the kit's
default display name without colours) next to `%kit%`. Code: `model/inventory/PackStyle`, `PackIcon`.

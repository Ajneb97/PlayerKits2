# BetterPlayerKits2

A fork of **PlayerKits2 by Ajneb97** — https://www.spigotmc.org/resources/playerkits-2-fully-configurable-kits-1-8-1-20.112616/

Modernised for Paper / Folia / Purpur / CanvasMC, hardened against duplication and GUI exploits, and
extended with a per-player kit layout editor. All original functionality, configuration formats and
multi-version support (1.8 → 26.2) are preserved.

**Credits:** original plugin and all of its design by **Ajneb97**. Fork maintained by **Jules(forked it)**.

## What this fork changes

### Platform support

- Runs on **Bukkit, Spigot, Paper, Purpur, Folia and CanvasMC**.
- A `PlatformScheduler` abstraction resolves the Folia regionised scheduler API by reflection at
  startup, so a single jar keeps loading on servers as old as 1.8 that have no such API.
- Global/region/entity/async work is routed to the correct scheduler:
  - GUI refreshes and GUI transitions run on the viewing player's **entity scheduler**.
  - Player data file and database I/O runs on the **async scheduler**.
  - Console commands from kit actions are dispatched on the **global region** on regionised servers.
- `folia-supported: true` is declared in `plugin.yml`.

### Security fixes

- **Kit duplication via failing actions** — kit state (cooldown, one-time, bought) is now written
  *before* items are handed out, and every action is executed inside a guard, so a malformed action
  can no longer abort the claim after items were given.
- **Cooldown reset on join** — claiming used to be possible while player data was still loading
  asynchronously; the load then overwrote the freshly written cooldown. Claims are now gated on
  loaded data, and a late load merges conservatively (highest cooldown, sticky one-time/bought).
- **Free kits on failed payment** — the Vault withdrawal is now performed before items are given and
  its `EconomyResponse` is checked.
- **Concurrent claims** — a per-player reentrancy guard prevents a second claim from starting while
  one is in progress.
- **Drag exploits** — `InventoryDragEvent` was never handled; dragging into a kit GUI is now denied.
- **GUI spoofing / cross-player clicks** — every GUI carries a `KitGuiHolder` bound to its owner's
  UUID, and clicks are validated against it instead of a player-list lookup.
- **Requirement bypass** — placeholder conditions that failed to parse used to return `true`
  (fail-open); they now fail closed, and malformed conditions are rejected.
- **Silent item loss** — leftovers from `addItem` were discarded; they are now dropped instead.
- **Item loss in the position editor** — the admin's inventory is cleared and was only restored a tick
  later, losing items on quit or crash. Restoration is now immediate and also runs on plugin disable.
- **Thread safety** — player data, GUI sessions and kit/inventory registries moved to concurrent
  collections; the async save task no longer iterates maps being mutated by the main thread.
- **Path traversal** — kit names are validated before being used as file names.
- **Inventory desync** — GUI transitions and closes triggered from click handlers are deferred by a
  tick instead of running inside the event.
- The database connection and all tasks are now shut down on plugin disable.

### Performance

- The global "scan every open GUI every second" task is gone; each viewer gets one refresh task that
  is cancelled on close.
- Kit, inventory and player lookups moved from `O(n)` list scans to hash maps.
- `InventoryView#getTopInventory` reflection is cached per view class instead of resolved per call.
- Fewer allocations in lore, NBT and action handling.

### New feature: personal kit layout editor

Every player can decide where each kit's items land in their own inventory.

- **Edit Layout** button in the kit GUI (shipped in `preview_inventory`, and auto-added once to
  existing `inventory.yml` files), plus `SHIFT + LEFT click` on any kit item and `/kit layout <kit>`.
- The editor is a 54-slot GUI whose top 36 slots mirror the player's inventory. Click an item to pick
  it up, click another slot to place or swap it. **All clicks are cancelled** — no real item ever
  moves, so the editor cannot be used to duplicate or lose items.
- Layouts are **per player and per kit**, are never written back to the kit, and players who never
  open the editor keep the default behaviour.
- Persisted with the existing player data (`players/<uuid>.yml` or the new
  `playerkits_players_layouts` MySQL table) and saved by the existing dirty-flag save task, so the
  feature adds no extra disk or database traffic.
- On claim, an item goes to its saved slot when that slot is free, and otherwise falls back to the
  normal behaviour. Auto-equipped armor and offhand items are unaffected.

### Configuration additions

```yaml
metrics: false
layout_editor:
  enabled: true
  shift_click: true
  title: "&8Edit Layout: &9%kit%"
```

Existing `config.yml`, `messages.yml` and `inventory.yml` files are migrated automatically on
startup; nothing needs to be regenerated.

> `metrics` defaults to **false** because bStats id 19795 belongs to the upstream PlayerKits2
> project. Set it to `true` only if you intend to report to that project, or replace the id in
> `PlayerKits2.java` with your own.

## Building

```bash
mvn clean package
```

Produces `target/BetterPlayerKits2-<version>.jar`. Requires JDK 21+.

## License

See [LICENSE](LICENSE). The original PlayerKits2 copyright and credit are retained.

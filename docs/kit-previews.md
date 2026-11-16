# Kit previews

Right-click a kit in the list or run `/kit preview <kit>`. The default
preview uses 54 slots with a Go Back arrow at slot 45 and a Claim Kit chest
at slot 49. The buttons use vanilla items and text colors.

Click the chest to claim the kit with its normal permissions, cooldown, space
and requirements checks. If a claim opens a requirements screen, cancelling
returns to the menu that opened the preview.

## Use a different layout for a kit

Add `preview_inventory` to the kit's YAML file and set it to the name of an
inventory under `inventories` in `inventory.yml`:

```yaml
# kits/iron.yml
preview_inventory: iron_preview
```

```yaml
# Add under inventories in inventory.yml
iron_preview:
  slots: 45
  title: "&4&lKit Preview: %kit%"
  36:
    item:
      id: ARROW
      name: "&7Go Back"
    open_inventory: previous
  40:
    item:
      id: CHEST
      name: "&aClaim Kit"
      lore:
        - "&7Click to claim this kit."
    type: preview_claim
```

Use a size of 9, 18, 27, 36, 45 or 54. `%kit%` in the title is replaced
with the kit ID. The standard `preview_inventory` remains the fallback if
a kit leaves the setting unset or refers to a missing layout. Keep the
built-in `buy_requirements_inventory` for the purchase flow.

The preview displays the kit's items and its claim action display items.
An explicit `preview_slot` takes priority over an automatic position.
Items with unavailable positions move to free slots. The preview skips
items that exceed its capacity. Navigation and action slots stay reserved
for their controls.

## Add a claim button to an existing preview

New installations get the chest in the default inventory file. To enable it
in an existing layout, add a slot with `type: preview_claim` and an `item`
as shown above. Use a free slot in that layout. A left or right click claims
the kit currently being previewed. The claim button is shown only in previews.

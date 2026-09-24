# Lv Foods SxV - Config

After the mod starts once, it creates:

`config/lv_foods_sxv.json`

Default values:

```json
{
  "xp_feast_1": 1000,
  "xp_feast_2": 5000,
  "xp_feast_3": 10000,
  "xp_feast_4": 15000,
  "xp_feast_5": 30000,
  "legendary_feast_levels": 105,
  "divine_feast_levels": 10000
}
```

- `xp_feast_1` through `xp_feast_5`: XP granted by foods 1-5.
- `legendary_feast_levels`: levels granted by food 6.
- `divine_feast_levels`: levels granted by food 7.

Edit the numbers, save the file, then restart Minecraft for the new values to load.
Values below 0 are clamped to 0.

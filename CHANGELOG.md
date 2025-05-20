# 0.0.5

- data-driven features refactor, items are now defined as itemStacks in the known format
- added an optional additional dimension for public locations
- added several new blocks
  - Player Detector Block, triggers another block when a player enters an area
  - Lootable Vault Block, a variant of the Vault Block that has integration for the 'Lootable' mod
  - Triggering Trial Spawner, a variant of the Trial Spawner, that can trigger blocks when changing the Trial Spawner State
  - Triggered Villager Spawner Block, a variant of the Triggered Spawner specifically for all entities that have VillagerData (has automatic support for mods that add villager professions or types)
  - Triggered Entity Remover Block, discards all non-player entities in an area when triggered
  - Triggered Dispenser Block, variant of the vanilla dispenser, but needs to be triggered, doesn't drop it's inventory and can't be interacted with in survival/adventure mode
  - Aesthetic Nether Portal, looks and sounds like a nether portal block, but is just aesthetic
  - Aesthetic Decorated Pot, looks like a decorated pot, but has no inventory and can't be smashed
  - Triggered Redstone Block, emits a redstone signal when triggered, has several states (signal strength, toggle/pulse) that change on (shift) right-click
- added 'Fake Villager' entity type, looks and sounds like a villager, but has no AI and no default right-click interaction

# 0.0.4

- added Teleporter(Trap)Doors, can be broken in survival and keep their settings
- several small fixes

# 0.0.3

- updated to 1.21.1
- added (optional) compatibility with Lootables
- now depends on Slot Customization API
- now depends on Fzzy Config
- removed dependency on AzureLib
- removed dependency on Cardinal Components API
- removed dependency on Cloth Config
- lots of internal refactors, bug fixes and improvements, some highlights:

- added "BossControllerBlock" and data-driven boss fights
- added a series of "data blocks", that can save data in world and influence other script blocks
- removed all custom mobs, TriggeredSpawner and BossController now work with every MobEntity

- check the GitHub commits if you are interested in more details

# 0.0.2

- added missing localization strings
- added background gradient to all creative screens
- added missing biome definitions

# 0.0.1

First alpha release.

Bugs and crashes are very possible.

Make backups of your worlds or better, don't use this mod with existing worlds.

Updates may make breaking changes!

Feedback and bug reports are very appreciated!

#
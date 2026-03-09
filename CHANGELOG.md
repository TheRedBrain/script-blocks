# 0.0.8

## Additions

- added "scriptblocks:removed_on_teleport" data component, which holds a list of strings
- Teleporter Blocks can now remove status effects in a configurable tag on teleport (in addition to the existing "reduce level by 1" status effect tag)
- Teleporter Blocks can now remove items from the player inventory on teleport
  - item stacks with the new "scriptblocks:removed_on_teleport" component are removed if the components string list contains a string specified by the Teleporter Block
- added option to set the manual reset for Location Controller Blocks in their config screen
  - can be used to ensure that locations are reset when first visited, without the need for always resetting/player initialized reset before entering/complicated script block structures
- added option to hide the "Cancel Teleport" button in the teleport screen
- added "scriptblocks:adventure" status effect, players with this effect are under the same restrictions as if they were in adventure mode

## Changes

- deprecated Lootable Vault Block and Interactive Loot Block
  - these block were extracted into a standalone mod
  - this includes all integration with the Lootables mod
  - existing blocks are still present, but several warning log messages were added and these blocks will be removed eventually
- no longer depends on DimLib, the dynamic dimensions are now implemented using Fantasy (https://github.com/NucleoidMC/fantasy)
- changed several mixin implementations to be more compatible

# 0.0.7

## Additions

- "Boss Controller Block" can now trigger a block when no players are present in its defined area
- added optional "triggersBlockWhenNoPlayersAround" boolean field to boss data files (false by default)
- added "scriptblocks:interactive_key" data component, used to restrict several block interactions
- added "Interactive Trigger Blocks" in door and trap door form

## Changes

- "Boss Controller Block" no longer removes non-player entities in its defined area when reset. Use the "Triggered Entity Remover Block" for this instead.
- Entrance Delegation Block now relays Triggered Spawner / Boss Controller Block spawn position
- overhauled Use Relay Chest blocks, now use the new "interactive_key" feature and can trigger a block when opened

## Fixes

- fixed "rollable" setting of Jigsaw Placer Block
- fixed manual location reset in teleporter screen
- fixed an issue where block entity data would not be updated when the block was rotated/mirrored
- fixed several issues with the Boss Controller Block

# 0.0.6

## Additions

- added Use Relay Lectern Block, another variant of the Use Relay Block
- added Area Filler Block, when triggered fills a configurable area with a configurable block
- added option to Teleporter Block to not teleport on tick, but when triggered
- added option to Triggered Beacon Block to set applied effect duration
- added Team Controller Block, creates configured team on trigger and adds entities in configured area to that team
- added PVP Controller Block, controls player respawning during pvp battles, designed to work with RPG Inventory's "PVP Death" system
- added data-driven 'PVP Arena Settings', used by the PVP Controller Block
- added Triggered Damage Dealing Block, dealing configurable damage in a configurable area when triggered
- added server config option to set world spawn to location entrance (note that the sky access requirement for the world spawn is still active)

## Changes

- several data-driven features were restructured, see the test data for examples
  - the display order of location (entrance) names is now configurable
  - in addition to advancements, data (using Data Blocks) can now be used to (un)lock locations and dialogue answers
- the force-loaded chunk area on location reset is now configurable in the LocationControllerBlock
- overhauled the dialogue screen, allowing for greater customization by dialogues
- the custom data location was moved from the "namespace/scriptblocks" to the "namespace" directory
- visible, locked dialogue answer buttons are now inactive
- "direct" teleport with Teleporter Block now works with Entrance Delegation Block
- removed the "shouldJigSawGenerationBeDeterministic" and "shouldJigSawStructuresBeRandomlyRotated" server config options, as they are no longer needed

## Fixes

- Jigsaw Placer Block no longer crashes the game when the specified template pool is not found
- updating script blocks now properly resets them
- structures placed by the JigsawPlacer block are now properly randomized
- horizontal facing JigsawPlacer blocks now place the structure correctly (having proper test structures helps a lot)
- fixed WorldLoadingScreen not showing when teleporting with Teleporter

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
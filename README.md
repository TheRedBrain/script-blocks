# Script Blocks

This mod adds multiple blocks designed to help building adventure maps and dungeons.

## Important
This mod is currently in its alpha stage, which means:
- Not all features are implemented.
- Existing features may not be in their final form.
- Breaking changes can happen.
- Existing features may be removed.
- Expect bugs and crashes.
- Documentation is very minimal.
- Back up your worlds when using this mod.

## Features

This mod implements a lot of different features, I will give a short and incomplete overview here.\
All details will be explained in the wiki (not yet written).

### Script Blocks
The basic idea is to build a "script" outside the visible parts of the dungeon.

There are blocks that can detect a player, interactable blocks, etc., which all "trigger" a configurable block position.\
Then there are blocks which have functionality when they are "triggered", like mob spawners, blocks which change state, message sender, jigsaw placer, advancement giver, etc.
There are also utility blocks like counter, delay and relay blocks.

The blocks are designed to be used in jigsaw structures.
They are configurable via an in game UI, like the existing jigsaw blocks.

### Dialogues
Dialogues are data driven and can be referenced via the 'dialogue block'.\
Each dialogue is a list of strings and a list of answers. When the player chooses an answer, several things can happen:
- another dialogue opens
- an advancement is granted
- a loot table is generated and the items given to the player
- a message is send to the player
- a block at a configurable position is triggered (refer to Script Blocks)
- a block at a configurable position is interacted with (similar to a right-click)
An answer can be (un)locked via an advancement and can have an item cost.

### Shops
Shops are data driven and can be referenced via special blocks.\
A shop can have multiple deals, which can be (un)locked via advancements.
A deal has an item stack as offer and a list of item stacks as price.

### Use Relay Blocks
When interacting with these blocks, they relay the interaction to another block.\
For now they come in the form of all existing doors and trapdoors.

### Teleporter blocks and locations
Teleporter blocks can teleport the player and optionally the players party to a position in a dimension.
The position can be configured:
- the players spawn point or the world spawn.
- a specific position in a specific dimension set by another teleporter block.
- a 'location'.

Locations are data driven. They reference a structure and a 'control block position'.
The structure must be configured, so that the block at the 'control block position' is a 'location control block'.
The 'location control block' tells the teleporter the teleport target position.

Locations can be (un)locked by advancements and can have an item cost.
They can either be public or player specific.\
Public locations are located in the overworld. Private locations are located in a 'player_locations_dimension'. Each player has one, they are dynamically generated for each player entering the world.

### Housing
A housing block has an owner and an area of influence (AOI).\
In the AOI only the owner can place, break or interact with blocks. Even players in adventure mode can break or place blocks here.\
The owner can add other players to different lists, which grants them various rights in the AOI, like placing, breaking or interacting with blocks.

Housing blocks have two different modi to determine their owner. 
- Interaction, the owner can be set by interacting with the housing block. The owner can revoke ownership of the housing block.
- Dimension, when the housing block is placed in a 'player_locations_dimension', the player owning the dimension also owns the housing block
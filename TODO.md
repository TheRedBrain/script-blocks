## Testing required

## TODO for next update

## TODO long term

- refactor various features to use a loot context predicate
    - should reference a data driven predicate id
    - how to implement a "data check" (data on a "ProvidesData" block)
    - features to refactor
        - interactive trigger blocks
            - item predicate for items that should prevent interaction
        - locations
            - unlock + cost
        - dialogues
        - dialogue answers
            - unlock + cost
        - shop deals
        - player detector block
            - could be refactored into an entity detecting block
        - triggered advancement checker block
        - triggered damage dealing block
        - triggered beacon block
        - teleporter block
            - item predicate for items that should prevent interaction

- Triggered Entity Checker Block (new)
    - relays a trigger signal to one of two blocks, depending on whether any entities that match a predicate are present
      in an area
    - alternative mode: world owner (functionality of TriggeredAdvancementCheckerBlock)

- new script block category and interaction type "player list blocks"
    - these blocks work with a list of player UUIDs and a boolean
        - the boolean indicates if the list content is additive or subtractive
    - some blocks provide a list
        - ~~Teleporter Block - onTeleport~~
        - ~~Player Detector Block?~~
    - some blocks use a list
        - ~~manipulate status effects block (extract from Teleporter Block)~~
            - ~~remove, apply, decrement/increment level~~
        - ~~send message block?~~
        - ~~set/remove spawn point~~
        - add/remove items to/from equipment slots or the inventory
            - gated behind predicates
    - there are also "logic blocks"
        - a block that inverts the boolean
        - ~~a relay block~~
        - a player list holder block that acts as player counter
            - can trigger other blocks when list changes from empty to filled and vice versa
            - maybe has a variable threshold
            - use cases
                - check if an instance is empty and can be reset
                - used by encounters to determine difficulty level
            - when triggered, triggers another block
                - list of other blocks with a threshold, the last passed threshold is triggered -> similar to
                  TriggeredCounterBlock

- Location Control Block
  - find alternative solution to forceload area, commands are not best practice
  - automatic location resets
  - forceloads a defined area, similar to nether portals/ender pearl projectiles
  - starts the forceloading when teleported to
  - has a timer after which a UUID List Handler is checked, when players are present, reset the timer
    - if no players are present:
      - just unload the area
      - set a countdown and unload the area
      - reset the location and unload the area
        - just set the manual reset override

- data component predicates
    - for "prevent interactions"
    - for "removed on interactions"

- some sort of localised environment attributes provider
    - similar to Lithostitched structure environment attributes, but in an area defined by a block entity

- biome placer block
  - sets the biome in a specified area

- Dialogues
    - convert data block pos into List<MutablePair<String, BlockPos>>
    - data-driven dialogue answer results
        - existing types are built-in
        - would enable mods like RPG Class Selection to have specific results

What happens when a player logs out in a location in another player's personal dimension and the location gets reset.

- the logout position is now potentially obstructed
    - use Rogue Tweaks to teleport players to their spawn point on login
        - what if the respawn point was set inside the location?
            - if a spawn setting block (bed/respqwn anchor/etc) is present, it should be a safe spawn
            - if not, they spawn at world spawn anyway
        - maybe let Rogue Tweaks remove player spawn point and thus send players to world spawn
    - solution without Rogue Tweaks?

- update readme
- documentation/wiki

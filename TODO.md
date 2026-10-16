## Testing required

## TODO for next update

## TODO long term

- DelayTriggerBlock -> TriggeredDelayBlock
- RelayTriggerBlock -> TriggeredRelayBlock

- refactor block entity nbt data + json data fields to snake_case

- interactive torch block
    - when interacted with (optional item required), triggers block
    - interacting toggles lit state
    - interacting only possible once
    - can be reset
    - interacting in creative mode toggles lit state but not triggered state
- triggered candle/torch block
    - lit state is toggled when triggered
    - can be reset
    - interacting in creative mode toggles lit state but not triggered state
- copper (trap)door variants for the script blocks (trap)doors

- refactor various features to use a loot context predicate
    - should reference a data driven predicate id
    - how to implement a "data check" (data on a "ProvidesData" block)
    - features to refactor
        - interactive trigger blocks
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
            - item predicate for items that should be removed on teleport

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
        - send message block?
        - set/remove spawn point
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

~~- cooldown on having access to a location?
- how would that be tracked?
- player list blocks + data attachments
- player list block put received players + configurable location id + current world/server time into data attachment
- whenever player opens a teleporter screen, this data is checked
- when the saved time is longer than x amount ago, allow the teleport
- amount x is defined by entrances -> allows entrances with varying timers, but does not require manual setup per
Teleporter Block~~
- else cancel/show in GUI that the location is on a timer
~~- what about team teleports?
- if a team player has a cooldown, what should happen?
- the team can't teleport and a message about the player on cooldown is displayed
- not used options:
- cooldown is ignored
- only the players on cooldown are not teleported/removed from team
- how are player deaths in a location tracked?
- when player enters location -> no time is saved, but eg -1, which means the player is in the location
- when a player leaves a location, the time is saved instead
- when a player dies, the time is saved for all locations with a value of -1~~
- could also be used for any other methods of leaving a location, e.g. teleportation via items

- data-driven dialogue answer results
    - existing types are built-in
    - would enable mods like RPG Class Selection to have specific results

- item cost for resetting a location

- data component for preventing interaction (e.g. with Teleporter Block, Interactive Trigger Blocks, etc)
    - has list if strings
    - block can define a string that is used for the check
    - if player has at least one item with the component and the block given string is in the list, the player can't
      teleport/open the teleport menu/interact

- data component predicates
    - for "prevent interactions"
    - for "removed on interactions"

- some sort of localised environment attributes provider
    - similar to Lithostitched structure environment attributes, but in an area defined by a block entity


- Triggering Respawn Anchor Block (new)
    - takes a list of Player UUIDs, binds their respawn position to it (if remove boolean is true, remove the spawnpoint
      instead)
    - has a configurable respawn position
    - when reset, removes the respawn position from the players
    - when a player respawns at the block, it triggers another block

- Dialogue
    - convert data block pos into List<MutablePair<String, BlockPos>>

- Two Areas Player Detector Block
    - this block has two areas
    - another block is triggered when a player enters one of the areas, but only if that player is already in the other
      area
        - depending on which area was entered first, a distinct block is triggered
        - the UUID of the player is also send to distinct UUIDListHandlers

What happens when a player logs out in a location in another player's personal dimension and the location gets reset.

- the logout position is now potentially obstructed
    - use Rogue Tweaks to teleport players to their spawn point on login
        - what if the respawn point was set inside the location?
            - if a spawn setting block (bed/respqwn anchor/etc) is present, it should be a safe spawn
            - if not, they spawn at world spawn anyway
        - maybe let Rogue Tweaks remove player spawn point and thus send players to world spawn
    - solution without Rogue Tweaks?

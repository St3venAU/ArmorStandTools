# ArmorStandTools
A full suite of Armor Stand tools for CraftBukkit/Spigot

Spigot resource page with plugin download: http://www.spigotmc.org/resources/armor-stand-tools.2237/

Inspiration
-----------
I wanted to create an armor stand for each kit in my mini-game that acts out its special ability. I quickly became frustrated with trying to use commands and numeric values to position the legs, arms, body and head of each armor stand, so I created this plugin which allows you to do all of this with ease. Among other features you can create any pose you wish just by holding right click on the tools and moving your cursor up and down on the armor stand. The plugin can also generate a summon command that will re-create the armor stand at any time.

Compatibility
-------------
- Spigot/CraftBukkit 1.8.x, 1.9.x, 1.10.x, 1.11.x, 1.12.x

Features
--------
- Summon armor stands.
- Name armor stands.
- Toggle: Gravity, Visibility, Arms, Base, Size, Invulnerability, Equipment Lock.
- Manipulate the x/y/z rotations of the Head, Body, Arms and Legs. The value depends on how high up the armor stand's body you click with the tool (i.e. click near the feet is one extreme, near the top of the head is the other extreme).
- Full control over armor stand's inventory (armor & items in hands).
- Pick up and move armor stands.
- Armor stand cloning tool.
- Save tool: Automatically generate a command block with the command to summon that armor stand in its current state.
- Player head tool: Give an armor stand the head of a specific player.
- WorldGuard region support.
- Customizable language config file.

Commands
--------
- /astools : Give yourself all of the armor stand tools (Note: Clears your inventory)
- /astools reload : Reload the config file
- /ast : Alias for /astools

Permissions
-----------
- astools.command : Permission for the /astools command
- astools.reload : Permission to reload the plugin with /astools reload
- astools.use : Permission for using any of the tools
- astools.clone: Permission to use the clone tool
- astools.head: Permission to use the player head tool (Ability to specify a player head for an armor stand)
- astools.summon: Permission to use the summon tool (Summons an armor stand without requiring the materials)
- astools.cmdblock: Permission to use the save tool (Creates a command block)

Config
------
- config.yml - The main config file allows you to set the default starting settings for newly summoned armor stands. This is useful if you plan on creating a lot of armor stands with similar equipment.
- language.yml - Contains all of the strings of text that the player will see. Edit this file if you wish to change the text or translate it into a different language.

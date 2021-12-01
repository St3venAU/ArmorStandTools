# ArmorStandTools
A full suite of Armor Stand tools for CraftBukkit/Spigot

Spigot resource page with plugin download: http://www.spigotmc.org/resources/armor-stand-tools.2237/

About
-----
I wanted to create an armor stand for each kit in my mini-game, and I quickly became frustrated with trying to use commands and numeric values to position the legs, arms, body and head of each armor stand, so I created this plugin which allows you to do all of this with ease. Among other features you can create any pose you wish just by holding right click on the tools and moving your cursor up and down on the armor stand. The plugin can also generate a summon command that will re-create the armor stand at any time.

Compatibility
-------------
- Armor Stand Tools v4.x.x - Spigot/CraftBukkit 1.17, 1.18+
- Armor Stand Tools v3.x.x - Spigot/CraftBukkit 1.13 - 1.16
- Armor Stand Tools v2.4.3 - Spigot/CraftBukkit 1.8 - 1.12

Features
--------
- Summon armor stands.
- Name armor stands.
- Toggle: Gravity, Visibility, Arms, Base, Size, Invulnerability, Equipment Lock, Glow.
- Manipulate the x/y/z rotations of the Head, Body, Arms and Legs. The value depends on how high up the armor stand's body you click with the tool (i.e. click near the feet is one extreme, near the top of the head is the other extreme).
- Full control over armor stand's inventory (armor & items in hands).
- Pick up and move armor stands.
- Armor stand cloning tool.
- Save tool: Automatically generate a summon command to summon the armor stand in its current state. This can be saved to a command block or logged.
- Pick up as item: Convert an armor stand into an inventory item that when placed like a normal armor stand retains its inventory, pose and settings.
- Player head tool: Give an armor stand the head of a specific player.
- WorldGuard region support, including a custom WorldGuard flag. Setting the 'ast' flag for a region to deny turns off AST use in that region.
- Customizable language config file.
- Assign commands to armor stands that are run when a player right clicks that armor stand (see below).

Commands
--------
- /astools or /ast : Give yourself all the armor stand tools (Note: Saves & clears your inventory which is restored by running this command again)
- /astools reload : Reload the config file
- /ascmd add \<priority\> \<delay\> \<player/console/bungee\> \<command/bungee_server_name\> : Add an assigned command to the nearest armor stand. See the assigning commands section below for more info.
- /ascmd remove \<command_number\> : Remove a command from to the nearest armor stand (use /ascmd list to find the command number)
- /ascmd list : List the commands assigned to the nearest armor stand
- /ascmd cooldown <ticks> : Sets the cooldown (in ticks) for the command on nearest armor stand (Setting this overrides the default cooldown from config.yml)
- /ascmd cooldown remove : Removes the cooldown for the command on nearest armor stand (Default cooldown set in config.yml will be used)

Permissions
-----------
- astools.use : Permission for using any of the tools
- astools.command : Permission for the /astools command
- astools.reload : Permission to reload the plugin with /astools reload
- astools.clone: Permission to use the clone tool
- astools.head: Permission to use the player head tool (Ability to specify a player head for an armor stand)
- astools.summon: Permission to use the summon tool (Summons an armor stand without requiring the materials)
- astools.cmdblock: Permission to use the save tool (Create a summon command)
- astools.glow: Permission to use the glow tool (Glow effect on armor stands)
- astools.ascmd.add.console: Permission to add a console command to an armor stand (Previously astools.ascmd.assign.console)
- astools.ascmd.add.player: Permission to add a player command to an armor stand (Previously astools.ascmd.assign.player)
- astools.ascmd.add.bungee: Permission to add a bungee command to an armor stand (See below for details)
- astools.ascmd.remove: Permission to remove a command from an armor stand
- astools.ascmd.list: Permission to list the commands assigned to an armor stand (Previously astools.ascmd.view)
- astools.ascmd.cooldown: Permission to add/remove a cooldown to commands assigned to an armor stand
- astools.ascmd.execute: Permission to execute commands assigned to an armor stand by (on right click)
- astools.bypass-wg-flag: Permission to bypass the WorldGuard ast flag, allowing the player to use AST even in regions that ast flag is set to deny.

Assigning Commands to Armor Stands
----------------------------------
- Stand close to the armor stand (The nearest armor stand within 4 blocks is selected) and use the command: /ascmd add \<priority\> \<delay\> \<player/console/bungee\> \<command/bungee_server_name\>
- \<priority\>: When multiple commands are assigned, commands with the lowest priority number are executed first. Command with the same priority can be executed in any order.
- \<delay\>: Delay in ticks before the command is executed.
- \<player/console/bungee\>: Player commands are executed as if they were typed by the player. Console commands are executed by the console. Bungee commands are a special case (see below).
- \<command/bungee_server_name\>: The command to be executed. To use the executing player's name in the command, use the placeholder %player% - it will be replaced with the players name at time of execution.
- When a player with the astools.ascmd.execute permission right-clicks on an armor stand, commands assigned to that armor stand are executed.
- Warning: Make sure you are careful when assigning console commands. Any player with the astools.ascmd.execute permission will be able to execute the command.
- By default, any command assigned to an armor stand will use the default cooldown set in config.yml. This can be set on an individual basis using the /ascmd cooldown <ticks> command.
- Bungee commands: These are used to send the player to a different BungeeCord server. E.g. This will add a command to send the player to a server called server1: /ascmd add 0 0 bungee server1

WorldGuard Integration
----------------------
 - If a player does not have build permission for the region, that player will also be denied the use of AST in that region.
 - Additionally, there is a custom region flag named 'ast' which defaults to Allow, this can be changed to Deny if you wish to have a region in which players have build permission, but not permission to use AST.
 - The ast worldguard flag is ignored for any player with the permission node 'astools.bypass-wg-flag'. This means that a player with this permission can use AST in worldguard regions even if the ast flag for that region is set to Deny.
 - The ast worldguard flag is also ignored for players that have op.

Config
------
- config.yml - The main config file allows you to set the default starting settings for newly summoned armor stands. This is useful if you plan on creating a lot of armor stands with similar equipment.
- language.yml - Contains all the strings of text that the player will see. Edit this file if you wish to change the text or translate it into a different language.

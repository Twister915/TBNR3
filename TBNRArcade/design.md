Arcade Arena Design Spec
==========================

Arcade will be modeled in the following way.

Each arcade game has a folder in the plugin data folder named after it's key.

Each folder will contain
* game.yml
* arena.json
* arena.zip

The game.yml is the configuration file for this game. A default can be provided by adding a file named

**gamekey**.yml 

in the resources directory. This will be copied to **gamekey**/game.yml


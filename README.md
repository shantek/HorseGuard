[![License: GPL](https://img.shields.io/badge/license-GPL-blue.svg)](LICENSE)
[![Discord](https://img.shields.io/discord/628396916639793152.svg?color=%237289da&label=discord)](https://shantek.co/discord)

#### Looking for the latest dev builds? You can find them [here!](https://shantek.dev/job/HorseGuard/)

### Horse Guard offers you the ultimate protection for horses on your Minecraft SMP.

Show your support for the Plugin via [PayPal](https://www.paypal.com/donate/?hosted_button_id=7KM6BVLPHSGDC) or [Patreon](https://shantek.co/patreon).

The ultimate horse protection plugin, once you tame a horse it belongs to you and no other players are able to ride, lead or harm your horse.

However, this plugin gives you the ability to trust other players, giving them access to ride and lead your horse, but not harm it. You also have the ability to transfer ownership of the horse to other players.

## How does the plugin work?

- Upload the JAR to your Bukkit, Spigot, Paper or Purpur server and reboot
- Any horses already tamed will be added to the config once it has been interacted with
- Each new horse you tame will be added to the plugin config and automatically protected

## Commands
The below commands are available to all players on your server, providing you own the horse. You must be sitting on the horse for these commands to work.

### /horse trustlist
If you own the horse, this will print a list of trusted players for this specific horse.

### /horse trust player
Replace 'player' with the player name you wish to trust. This will grant them access to ride and lead your horse, but not harm it.

### /horse untrust player
Replace 'player' with the player name you wish to remove trust access for. This will remove any permissions they had to ride and lead your horse.

### /horse transfer player
This will transfer ownership of the horse to the player you specify in the command.

## Permissions
The below permissions are ideal for server mods or staff allowing them to manage player owned horses on your server.

### shantek.horseguard.ride
Players with this permission will be able to ride and lead any horse, ignoring any ownership or trust settings.

### shantek.horseguard.damage
Players with this permission will be able to harm any horse, ignoring any ownership or trust settings.

![Plugin Usage Stats](https://bstats.org/signatures/bukkit/Horse%20Guard.svg)

[![License: GPL](https://img.shields.io/badge/license-GPL-blue.svg)](LICENSE)
[![Discord](https://img.shields.io/discord/628396916639793152.svg?color=%237289da&label=discord)](https://shantek.co/discord)
[![CodeFactor](https://www.codefactor.io/repository/github/shantek/horseguard/badge)](https://www.codefactor.io/repository/github/shantek/horseguard)
![Plugin Usage Stats](https://bstats.org/signatures/bukkit/Horse%20Guard.svg)

# 🐴 Horse Guard

**The ultimate horse protection plugin for Minecraft SMP servers.**  
Protect your tamed horses from theft and griefing while still allowing trusted friends to ride and lead them.

> ✅ [Get the latest dev builds here →](https://shantek.dev/job/HorseGuard/)

---

## 🔐 Key Features

- Horses are automatically protected once tamed.
- Only the owner can ride, lead, or damage the horse.
- Owners can trust others to **ride/lead** but not **harm** their horse.
- Ownership can be transferred to another player.
- Fully compatible with **Bukkit**, **Spigot**, **Paper**, and **Purpur**.

> ❤️ [Support development via Patreon](https://shantek.co/patreon) or [PayPal](https://www.paypal.com/donate/?hosted_button_id=7KM6BVLPHSGDC)

---

## ⚙️ How It Works

1. Upload the HorseGuard JAR to your server’s `/plugins` folder.
2. Restart your server.
3. Existing horses will be added to the config once interacted with.
4. Newly tamed horses are **automatically protected** and logged.

---

## 🐎 Commands

| Command | Description |
|---------|-------------|
| `/horse` | Open the GUI to manage trust, untrust, and ownership transfer. |
| `/horse trustlist` | View a list of trusted players for your current horse. |
| `/horse trust <player>` | Trust another player to ride/lead your horse. |
| `/horse untrust <player>` | Revoke trust from a player. |
| `/horse transfer <player>` | Transfer horse ownership to another player. |

> You **must be riding the horse** to use these commands.

---

## 🔐 Permissions

| Node | Description |
|------|-------------|
| `shantek.horseguard.ride` | Allows riding/leading any horse, ignoring trust/ownership. |
| `shantek.horseguard.damage` | Allows damaging any horse, regardless of ownership. |
| `shantek.horseguard.reload` | Grants access to reload the plugin config. |

---

## 🌐 External Links

- 💬 [Join Discord](https://shantek.co/discord)
- 🛠️ [GitHub Repo](https://github.com/shantek/HorseGuard)
- ✍️ [Report Bugs / Suggest Features](https://github.com/shantek/HorseGuard/issues)
- ❤️ [Support on Patreon](https://shantek.co/patreon)

---

## 📄 License

Distributed under the **GNU General Public License v3.0**.  
See [`LICENSE`](LICENSE) for full details.
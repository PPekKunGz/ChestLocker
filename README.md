# 🧱 **ChestLocker Mod – Secure Your Chests Like a Pro!**
> 🔐 Protect your storage. Lock with a key. Share with friends.
<img src="https://img.shields.io/modrinth/dt/oJS1ZjRd"/>

[Modrinth](https://modrinth.com/mod/chestlocker)

---

## 💡 **Features**
- 🔒 **Lock any chest** with a password-protected **Key Item** (Tripwire Hook).  
- 🔑 **Each key** stores its own password and owner (UUID-based), so only the true owner can lock or unlock.  
- 🧍‍♂️ **Whitelist system** lets you share access with trusted players via `/chestaccess` commands.  
- 🧱 **Break protection:** Locked chests cannot be broken or moved until unlocked.  
- 💬 **HUD display:** Shows “Locked by: <player>” when looking at a locked chest.  
- 💾 **Data persistence:** All locks automatically save to `config/chestlocker.json`.  
- ⚙️ **Multiplayer safe:** All checks are server-authoritative — no client-side exploits.

---

## 🔧 **How to Use**

### 🪄 1. Create a Key  
Hold a **Tripwire Hook** → **Shift + Right-Click** → Enter your password.  
This will save your name and UUID onto the key.

### 🔐 2. Lock a Chest  
Hold your key → **Right-Click** on a chest.  
If you’re the owner, the chest will lock immediately and be stored securely.

### 🔓 3. Unlock a Chest  
Hold your key → **Shift + Right-Click** on a locked chest.  
The mod verifies your **UUID + password hash** — if they match, the chest unlocks!

---

## 🧍‍🤝‍🧍 **Whitelist Commands**
You can grant or revoke chest access to friends:
- /chestaccess add <player>
- /chestaccess remove <player>
- /chestaccess list
  
---

## 🧭 **Tips**
- Press **Shift** while hovering your key to reveal the real password.  
- The key’s tooltip shows:
  
```
🔑 Key of <owner>
Password: ••••••••
Hold SHIFT to reveal
```
- You can only use your own key; others’ keys are invalid.

---

## ⚡ **Security Summary**

| Action | Requirement | Result |
|---------|--------------|--------|
| Lock chest | Your key (owner UUID matches) | ✅ Success |
| Unlock chest | Sneak + Right-Click with your key | 🔓 Unlocks |
| Use other’s key | Different UUID or password | ❌ Rejected |
| Break locked chest | Any player | ❌ Blocked |

---

## 📸 **Screenshots & Demo**
🖼️ Set password screen, owner tooltip, and key lock animation coming soon!

---

## 🔗 **Links**
- 🧰 Source: [GitHub – PPekKunGz/ChestLocker](https://github.com/PPekKunGz/ChestLocker)
- 🌍 Developed by **PPekKunGz**  
- 💬 Compatible: Minecraft **1.20.1+** (Fabric)  
- 🪶 License: **MIT**

---

## ✨ **Tagline**
> **ChestLocker – Your chest, your rules.**  
> Protect, share, and unlock safely with your personalized key. 🗝️

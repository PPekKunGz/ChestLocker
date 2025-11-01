package net.ppekkungz.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * โครงสร้างข้อมูลสำหรับ chest ที่ถูกล็อค
 * เก็บว่าใครเป็นเจ้าของ, password hash, whitelist และตำแหน่ง
 */
public final class LockData {

    private final GlobalPos pos;          // ระบุตำแหน่ง chest พร้อมมิติ (world)
    private final UUID owner;             // UUID ของเจ้าของ
    private final String passwordHash;    // password ที่ hash แล้ว
    private final Set<UUID> whitelist;    // คนที่เปิดได้

    public LockData(GlobalPos pos, UUID owner, String passwordHash) {
        this.pos = pos;
        this.owner = owner;
        this.passwordHash = passwordHash;
        this.whitelist = new HashSet<>();
    }

    // ---------- Getters ----------
    public GlobalPos pos() { return pos; }
    public UUID owner() { return owner; }
    public String passwordHash() { return passwordHash; }
    public Set<UUID> whitelist() { return whitelist; }

    // ---------- JSON Serialization ----------
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("dim", pos.dimension().location().toString());
        BlockPos p = pos.pos();
        obj.addProperty("x", p.getX());
        obj.addProperty("y", p.getY());
        obj.addProperty("z", p.getZ());

        obj.addProperty("owner", owner.toString());
        obj.addProperty("password", passwordHash);

        JsonArray wlArray = new JsonArray();
        for (UUID id : whitelist) wlArray.add(id.toString());
        obj.add("whitelist", wlArray);

        return obj;
    }

    public static LockData fromJson(JsonObject obj) {
        // (ตอนนี้ยัง default เป็น Overworld เพื่อความง่าย)
        ResourceKey<Level> dim = Level.OVERWORLD;
        BlockPos pos = new BlockPos(
                obj.get("x").getAsInt(),
                obj.get("y").getAsInt(),
                obj.get("z").getAsInt()
        );

        UUID owner = UUID.fromString(obj.get("owner").getAsString());
        String password = obj.get("password").getAsString();

        LockData data = new LockData(GlobalPos.of(dim, pos), owner, password);

        if (obj.has("whitelist")) {
            for (var el : obj.getAsJsonArray("whitelist")) {
                data.whitelist().add(UUID.fromString(el.getAsString()));
            }
        }

        return data;
    }
}

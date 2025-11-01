package net.ppekkungz;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.ppekkungz.util.LockData;
import net.ppekkungz.util.PasswordUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ตัวจัดการระบบล็อคทั้งหมดของ ChestLocker
 * ทำงานฝั่งเซิร์ฟเวอร์เท่านั้น
 */
public final class ChestLockManager {

    // แผนที่เก็บ chest ที่ถูกล็อคทั้งหมด (ตำแหน่งพร้อมมิติ → ข้อมูล lock)
    private static final Map<GlobalPos, LockData> LOCKED = new ConcurrentHashMap<>();

    // Path ของไฟล์ save
    private static Path savePath;

    private ChestLockManager() {}

    // ---------- ตรวจสอบสถานะ ----------
    public static boolean isLocked(Level level, BlockPos pos) {
        if (level.isClientSide) return false;
        return LOCKED.containsKey(global(level, pos));
    }

    public static Optional<LockData> getLock(Level level, BlockPos pos) {
        if (level.isClientSide) return Optional.empty();
        return Optional.ofNullable(LOCKED.get(global(level, pos)));
    }

    // ---------- ล็อค / ปลดล็อค ----------
    public static boolean lock(ServerLevel level, BlockPos pos, UUID owner, String rawPassword) {
        GlobalPos g = global(level, pos);
        if (LOCKED.containsKey(g)) return false;

        String hash = PasswordUtil.hash(rawPassword);
        LockData data = new LockData(g, owner, hash);
        LOCKED.put(g, data);
        save();
        return true;
    }

    public static boolean unlock(ServerLevel level, BlockPos pos, UUID playerId) {
        GlobalPos g = global(level, pos);
        LockData data = LOCKED.get(g);
        if (data == null) return false;

        // ปลดล็อคได้เฉพาะเจ้าของ
        if (!data.owner().equals(playerId)) return false;

        LOCKED.remove(g);
        save();
        return true;
    }

    // ---------- ตรวจสิทธิ์การเข้าถึง ----------
    public static boolean canAccess(ServerLevel level, BlockPos pos, UUID playerId, String passwordOrEmpty) {
        GlobalPos g = global(level, pos);
        LockData data = LOCKED.get(g);
        if (data == null) return true; // ถ้ายังไม่ล็อคก็เปิดได้เลย

        if (data.owner().equals(playerId)) return true;
        if (data.whitelist().contains(playerId)) return true;

        if (!passwordOrEmpty.isEmpty()) {
            return PasswordUtil.matches(passwordOrEmpty, data.passwordHash());
        }

        return false;
    }

    // ---------- Whitelist ----------
    public static boolean addToWhitelist(ServerLevel level, BlockPos pos, UUID owner, UUID target) {
        GlobalPos g = global(level, pos);
        LockData data = LOCKED.get(g);
        if (data == null || !data.owner().equals(owner)) return false;

        data.whitelist().add(target);
        save();
        return true;
    }

    public static boolean removeFromWhitelist(ServerLevel level, BlockPos pos, UUID owner, UUID target) {
        GlobalPos g = global(level, pos);
        LockData data = LOCKED.get(g);
        if (data == null || !data.owner().equals(owner)) return false;

        data.whitelist().remove(target);
        save();
        return true;
    }

    // ---------- Helper ----------
    private static GlobalPos global(Level level, BlockPos pos) {
        ResourceKey<Level> dim = level.dimension();
        return GlobalPos.of(dim, pos);
    }

    // ---------- Save / Load ----------
    public static void load(Path path) {
        savePath = path;
        if (Files.notExists(path)) return;

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonArray arr = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement el : arr) {
                LockData data = LockData.fromJson(el.getAsJsonObject());
                LOCKED.put(data.pos(), data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ปลดล็อค chest ด้วยกุญแจ (ต้องเป็น owner เดิมและ password ตรง)
     * @return true ถ้าปลดล็อคสำเร็จ
     */
    public static boolean unlockWithKey(ServerLevel level, BlockPos pos, UUID keyOwnerUuid, String rawPassword) {
        GlobalPos g = GlobalPos.of(level.dimension(), pos);
        LockData data = LOCKED.get(g);
        if (data == null) return false;

        // ต้องเป็น owner เดิม (UUID ตรง)
        if (!data.owner().equals(keyOwnerUuid)) return false;

        // ต้องใช้ password เดียวกับที่ล็อคไว้ (hash เทียบ hash)
        if (!PasswordUtil.matches(rawPassword, data.passwordHash())) return false;

        // ✅ ปลดล็อคสำเร็จ
        LOCKED.remove(g);
        save();
        return true;
    }

    public static void save() {
        if (savePath == null) return;

        JsonArray arr = new JsonArray();
        for (LockData data : LOCKED.values()) {
            arr.add(data.toJson());
        }

        try (Writer writer = Files.newBufferedWriter(savePath)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(arr, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
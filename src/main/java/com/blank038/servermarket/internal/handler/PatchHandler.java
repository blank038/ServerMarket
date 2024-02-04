package com.blank038.servermarket.internal.handler;

import com.blank038.servermarket.dto.impl.MysqlStorageHandlerImpl;
import com.blank038.servermarket.internal.plugin.ServerMarket;
import lombok.Getter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class PatchHandler {
    private static final Map<String, BooleanSupplier> PATCH_MAP = new HashMap<>();

    static {
        // Patch id example: "version-level-number"
        // Patch level: F=FATAL, U=URGENT, S=SLIGHT, O=OPTIONAL
        PATCH_MAP.put("251-U-1", () -> {
            AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            if (ServerMarket.getStorageHandler() instanceof MysqlStorageHandlerImpl) {
                MysqlStorageHandlerImpl.getStorageHandler().connect((statement) -> {
                    try {
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        atomicBoolean.set(false);
                        ServerMarket.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to update column data type.");
                    }
                }, "ALTER TABLE `servermarket_offline_transactions` CHANGE `amount` `amount` INT;");
            }
            return atomicBoolean.get();
        });
    }

    public static boolean executePatch(String id) {
        if (PATCH_MAP.containsKey(id)) {
            try {
                return PATCH_MAP.get(id).getAsBoolean();
            } catch (Exception e) {
                ServerMarket.getInstance().getLogger().log(Level.SEVERE, e, () -> "Patch failed to apply: " + id);
                return false;
            }
        }
        return false;
    }

    public static void registerPatch(String id, BooleanSupplier supplier) {
        PATCH_MAP.putIfAbsent(id, supplier);
    }

    public static Set<String> getPatchIds() {
        return PATCH_MAP.keySet();
    }
}

package com.blank038.servermarket.internal.service.notify;

import com.blank038.servermarket.internal.cache.other.NotifyCache;
import com.blank038.servermarket.internal.service.notify.impl.mysql.MySQLNotifyServiceImpl;
import com.blank038.servermarket.internal.service.notify.impl.self.SelfNotifyServiceImpl;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Blank038
 */
public class NotifyCenter {
    private static final Map<String, INotifyService> NOTIFY_SERVICES = new HashMap<>();
    private static final Map<String, Class<? extends INotifyService>> NOTIFY_CLASSES = new HashMap<>();

    static {
        NotifyCenter.register("self", SelfNotifyServiceImpl.class);
        NotifyCenter.register("mysql", MySQLNotifyServiceImpl.class);
    }

    public static boolean isRegister(String source) {
        return NOTIFY_CLASSES.containsKey(source);
    }

    public static boolean isCreated(String source) {
        return NOTIFY_SERVICES.containsKey(source);
    }

    public static void register(String source, Class<? extends INotifyService> classZ) {
        NOTIFY_CLASSES.putIfAbsent(source, classZ);
    }

    public static INotifyService create(String source, INotifyService service, ConfigurationSection config) {
        if (NOTIFY_SERVICES.containsKey(source)) {
            return null;
        }
        service.register(config);
        NOTIFY_SERVICES.put(source, service);
        return service;
    }

    public static Class<? extends INotifyService> findNotifyClass(String source) {
        return NOTIFY_CLASSES.getOrDefault(source, null);
    }

    public static INotifyService getService(String service) {
        return NOTIFY_SERVICES.getOrDefault(service, null);
    }

    public static Map<String, INotifyService> getServices() {
        return NOTIFY_SERVICES;
    }

    public static void pushNotify(NotifyCache cache) {
        NotifyCenter.getServices().forEach((k, v) -> v.push(cache));
    }
}
package com.ttvape;

import com.mojang.logging.LogUtils;
import com.ttvape.config.ConfigManager;
import com.ttvape.modules.ModuleManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TTClient.MOD_ID)
public class TTClient {
    public static final String MOD_ID = "ttvape";
    public static final String NAME = "vape";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ModuleManager modules;
    public static ConfigManager config;

    public TTClient(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} v{} common entry loaded", NAME, VERSION);
    }
}

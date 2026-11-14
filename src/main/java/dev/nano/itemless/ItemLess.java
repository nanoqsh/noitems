package dev.nano.itemless;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ItemLess.MODID)
public class ItemLess {
    public static final String MODID = "itemless";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ItemLess(IEventBus bus, ModContainer modContainer) {
        bus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }
}

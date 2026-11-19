package dev.nano.itemless;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(ItemLess.ID)
public class ItemLess {
    static final String ID = "itemless";
    static final Logger LOGGER = LogUtils.getLogger();

    public ItemLess(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent e) -> {
            if (e.getEntity() instanceof ItemEntity ie && ItemLess.banned(ie.getItem())) {
                e.setCanceled(true);
            }
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
            var inv = e.getEntity().getInventory();
            for (var i = 0; i < inv.getContainerSize(); i++) {
                if (ItemLess.banned(inv.getItem(i))) {
                    inv.setItem(i, ItemStack.EMPTY);
                }
            }
        });

        NeoForge.EVENT_BUS.addListener((PlayerContainerEvent.Open e) -> {
            if (e.getEntity().level().isClientSide) {
                return;
            }

            var menu = e.getContainer();
            for (var s : menu.slots) {
                if (ItemLess.banned(s.getItem())) {
                    s.set(ItemStack.EMPTY);
                }
            }

            menu.broadcastChanges();
        });
    }

    static final TagKey<Item> BANNED = TagKey
            .create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ItemLess.ID, "banned"));

    static void info(String s) {
        var evar = System.getenv("ITEMLESS_DEBUG");
        var debug = evar != null && !evar.isBlank() && evar != "0";

        if (debug) {
            ItemLess.LOGGER.info("[" + ItemLess.ID + "] " + s);
        }
    }

    static boolean banned(ItemStack s) {
        var banned = s.is(ItemLess.BANNED);
        if (banned) {
            ItemLess.info("Banned item: " + s.getItem());
        }

        return banned;
    }
}

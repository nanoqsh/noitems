package dev.nano.itemless;

import java.nio.file.Files;

import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

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

        ItemLess.addItems();
        ItemLess.ITEMS.register(bus);
        ItemLess.TABS.register(bus);
    }

    static final TagKey<Item> BANNED = TagKey
            .create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ItemLess.ID, "banned"));

    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ItemLess.ID);

    static void info(String s) {
        var evar = System.getenv("ITEMLESS_DEBUG");
        var debug = evar != null && !evar.isBlank() && !evar.equals("0");

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

    static void addItems() {
        var path = FMLPaths.CONFIGDIR.get().resolve("itemless.json");
        try {
            if (!Files.exists(path)) {
                return;
            }

            for (var el : JsonParser.parseString(Files.readString(path)).getAsJsonArray()) {
                String name;
                int stack;
                if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                    name = el.getAsString();
                    stack = 1;
                } else if (el.isJsonObject()) {
                    var o = el.getAsJsonObject();
                    name = o.get("id").getAsString();
                    stack = o.has("stack") ? o.get("stack").getAsInt() : 1;
                } else {
                    throw new Exception("invalid item properties, expected a string or an object");
                }

                var props = new Item.Properties().stacksTo(stack);
                ItemLess.ITEMS.registerSimpleItem(name, props);
            }
        } catch (Exception e) {
            ItemLess.LOGGER.error("[" + ItemLess.ID + "] failed to read itemless.json: " + e);
        }
    }

    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ItemLess.ID);

    static {
        ItemLess.TABS.register("tab", () -> CreativeModeTab.builder()
                .title(Component.literal(ItemLess.class.getSimpleName()))
                .icon(() -> new ItemStack(Items.PAPER))
                .displayItems((p, out) -> ItemLess.ITEMS.getEntries().forEach(item -> out.accept(item.get())))
                .build());
    }
}

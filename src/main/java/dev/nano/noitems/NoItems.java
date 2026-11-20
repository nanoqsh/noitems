package dev.nano.noitems;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(NoItems.ID)
public class NoItems {
    static final String ID = "noitems";
    static final Logger LOGGER = LogUtils.getLogger();

    public NoItems(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent e) -> {
            if (e.getEntity() instanceof ItemEntity ie && NoItems.banned(ie.getItem())) {
                e.setCanceled(true);
            }
        });

        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
            var inv = e.getEntity().getInventory();
            for (var i = 0; i < inv.getContainerSize(); i++) {
                if (NoItems.banned(inv.getItem(i))) {
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
                if (NoItems.banned(s.getItem())) {
                    s.set(ItemStack.EMPTY);
                }
            }

            menu.broadcastChanges();
        });

        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract e) -> {
            if (e.getLevel().isClientSide || !(e.getTarget() instanceof AbstractVillager v)) {
                return;
            }

            v.getOffers().removeIf(o -> NoItems.banned(o.getResult()) || NoItems.banned(o.getCostA())
                    || NoItems.banned(o.getCostB()));
        });

        Consumer<MinecraftServer> removeRecipes = s -> {
            var manager = s.getRecipeManager();
            var reg = s.registryAccess();
            var recipes = manager
                    .getRecipes()
                    .stream()
                    .filter(h -> !NoItems.banned(h.value().getResultItem(reg)))
                    .toList();

            manager.replaceRecipes(recipes);
        };

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> removeRecipes.accept(e.getServer()));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent e) -> removeRecipes.accept(e.getPlayerList().getServer()));

        bus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            new ArrayList<>(e.getParentEntries())
                    .stream()
                    .filter(NoItems::banned)
                    .forEach(s -> e.remove(s, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY));

            new ArrayList<>(e.getSearchEntries())
                    .stream()
                    .filter(NoItems::banned)
                    .forEach(s -> e.remove(s, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY));
        });

        NoItems.addItems();
        NoItems.ITEMS.register(bus);
        NoItems.TABS.register(bus);
    }

    static final TagKey<Item> BANNED = TagKey
            .create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(NoItems.ID, "banned"));

    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NoItems.ID);

    static void info(String s) {
        var evar = System.getenv("NOITEMS_DEBUG");
        var debug = evar != null && !evar.isBlank() && !evar.equals("0");

        if (debug) {
            NoItems.LOGGER.info("[" + NoItems.ID + "] " + s);
        }
    }

    static boolean banned(ItemStack s) {
        var banned = s.is(NoItems.BANNED);
        if (banned) {
            NoItems.info("Banned item: " + s.getItem());
        }

        return banned;
    }

    static void addItems() {
        var path = FMLPaths.CONFIGDIR.get().resolve("noitems.json");
        try {
            if (!Files.exists(path)) {
                return;
            }

            for (var el : JsonParser.parseString(Files.readString(path)).getAsJsonArray()) {
                String id;
                int stack;
                if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                    id = el.getAsString();
                    stack = 1;
                } else if (el.isJsonObject()) {
                    var o = el.getAsJsonObject();
                    id = o.get("id").getAsString();
                    stack = o.has("stack") ? o.get("stack").getAsInt() : 1;
                } else {
                    throw new Exception("invalid item properties, expected a string or an object");
                }

                var props = new Item.Properties().stacksTo(stack);
                NoItems.ITEMS.registerSimpleItem(id, props);
            }
        } catch (Exception e) {
            NoItems.LOGGER.error("[" + NoItems.ID + "] failed to read 'noitems.json' file: " + e);
        }
    }

    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, NoItems.ID);

    static {
        NoItems.TABS.register("tab", () -> CreativeModeTab.builder()
                .title(Component.literal(NoItems.class.getSimpleName()))
                .icon(() -> new ItemStack(Items.PAPER))
                .displayItems((p, out) -> NoItems.ITEMS.getEntries().forEach(item -> out.accept(item.get())))
                .build());
    }
}

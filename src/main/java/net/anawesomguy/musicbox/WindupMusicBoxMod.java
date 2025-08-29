package net.anawesomguy.musicbox;

import net.anawesomguy.musicbox.block.MusicBoxBlock;
import net.anawesomguy.musicbox.block.MusicBoxBlockEntity;
import net.anawesomguy.musicbox.item.MusicBoxData;
import net.anawesomguy.musicbox.item.MusicBoxDataComponent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindupMusicBoxMod implements ModInitializer {
    public static final String MOD_ID = "windup_music_box";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier MUSIC_BOX_ID = Identifier.of(MOD_ID, "music_box");

    public static final Block MUSIC_BOX = new MusicBoxBlock(
        AbstractBlock.Settings.create()
                              .strength(0.8F)
                              .nonOpaque()
                              .sounds(BlockSoundGroup.WOOD)
                              .registryKey(RegistryKey.of(RegistryKeys.BLOCK, MUSIC_BOX_ID)));

    public static final RegistryKey<Registry<MusicBoxData>> MUSIC_BOX_DATA_KEY = RegistryKey.ofRegistry(
        id("music_box_data"));
    public static final ComponentType<MusicBoxDataComponent> MUSIC_BOX_DATA =
        ComponentType.<MusicBoxDataComponent>builder()
                     .codec(MusicBoxDataComponent.CODEC)
                     .packetCodec(MusicBoxDataComponent.PACKET_CODEC)
                     .build();

    public static final Item MUSIC_BOX_ITEM =
        new BlockItem(MUSIC_BOX, new Item.Settings().component(MUSIC_BOX_DATA, null)
                                                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, MUSIC_BOX_ID)));

    public static final SoundEvent MUSIC_BOX_NOTE_C4 = SoundEvent.of(id("block.music_box.music_box_note.c4"));
    public static final SoundEvent MUSIC_BOX_NOTE_C6 = SoundEvent.of(id("block.music_box.music_box_note.c6"));
    public static final SoundEvent MUSIC_BOX_WIND_UP = SoundEvent.of(id("block.music_box.music_box_wind_up"));

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, MUSIC_BOX_ID, MUSIC_BOX);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, MUSIC_BOX_ID, MusicBoxBlockEntity.TYPE);

        Registry.register(Registries.ITEM, MUSIC_BOX_ID, MUSIC_BOX_ITEM);

        Registry.register(Registries.DATA_COMPONENT_TYPE, MUSIC_BOX_DATA_KEY.getValue(), MUSIC_BOX_DATA);

        Registry.register(Registries.SOUND_EVENT, MUSIC_BOX_NOTE_C4.id(), MUSIC_BOX_NOTE_C4);
        Registry.register(Registries.SOUND_EVENT, MUSIC_BOX_NOTE_C6.id(), MUSIC_BOX_NOTE_C6);
        Registry.register(Registries.SOUND_EVENT, MUSIC_BOX_WIND_UP.id(), MUSIC_BOX_WIND_UP);

        ComponentTooltipAppenderRegistry.addLast(MUSIC_BOX_DATA);

        DynamicRegistries.registerSynced(MUSIC_BOX_DATA_KEY, MusicBoxData.CODEC);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            ItemStack defaultStack = MUSIC_BOX_ITEM.getDefaultStack();
            entries.add(defaultStack);
            entries.getContext().lookup().getOrThrow(MUSIC_BOX_DATA_KEY).streamEntries().forEach(reference -> {
                ItemStack stack = defaultStack.copy();
                stack.set(MUSIC_BOX_DATA, new MusicBoxDataComponent(reference));
                entries.add(stack);
            });
        });
    }

    public static Identifier id(String path) {
        return MUSIC_BOX_ID.withPath(path);
    }
}
package net.anawesomguy.musicbox;

import net.anawesomguy.musicbox.block.MusicBoxBlock;
import net.anawesomguy.musicbox.block.MusicBoxBlockEntity;
import net.anawesomguy.musicbox.item.MusicBoxDrumComponent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class WindupMusicBoxMod implements ModInitializer {
    public static final String MOD_ID = "windup_music_box";

    public static final Identifier MUSIC_BOX_ID = Identifier.of(MOD_ID, "music_box");
    public static final Block MUSIC_BOX = new MusicBoxBlock(
        AbstractBlock.Settings.create()
                              .strength(0.8F)
                              .nonOpaque()
                              .sounds(BlockSoundGroup.WOOD)
                              .registryKey(RegistryKey.of(RegistryKeys.BLOCK, MUSIC_BOX_ID)));
    public static final ComponentType<MusicBoxDrumComponent> DRUM_COMPONENT =
        ComponentType.<MusicBoxDrumComponent>builder().codec(MusicBoxDrumComponent.CODEC).build();

    public static final SoundEvent MUSIC_BOX_NOTE = SoundEvent.of(id("block.music_box.music_box_note"));
    public static final SoundEvent MUSIC_BOX_WIND_UP = SoundEvent.of(id("block.music_box.music_box_wind_up"));

    public static final Item MUSIC_BOX_ITEM =
        new BlockItem(MUSIC_BOX, new Item.Settings().component(DRUM_COMPONENT, null)
                                                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, MUSIC_BOX_ID)));

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, MUSIC_BOX_ID, MUSIC_BOX);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, MUSIC_BOX_ID, MusicBoxBlockEntity.TYPE);
        Registry.register(Registries.ITEM, MUSIC_BOX_ID, MUSIC_BOX_ITEM);

        Registry.register(Registries.SOUND_EVENT, MUSIC_BOX_NOTE.id(), MUSIC_BOX_NOTE);
        Registry.register(Registries.SOUND_EVENT, MUSIC_BOX_WIND_UP.id(), MUSIC_BOX_WIND_UP);

        ComponentTooltipAppenderRegistry.addLast(DRUM_COMPONENT);
    }

    public static Identifier id(String path) {
        return MUSIC_BOX_ID.withPath(path);
    }
}
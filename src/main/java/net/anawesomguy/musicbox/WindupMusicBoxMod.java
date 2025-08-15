package net.anawesomguy.musicbox;

import net.anawesomguy.musicbox.block.MusicBoxBlock;
import net.anawesomguy.musicbox.block.MusicBoxBlockEntity;
import net.anawesomguy.musicbox.item.MusicBoxDrumComponent;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindupMusicBoxMod implements ModInitializer {
    public static final String MOD_ID = "windup_music_box";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier MUSIC_BOX_ID = Identifier.of(MOD_ID, "music_box");
    public static final Block MUSIC_BOX = new MusicBoxBlock(AbstractBlock.Settings.create().strength(0.8F));
    public static final ComponentType<MusicBoxDrumComponent> DRUM_COMPONENT = ComponentType.<MusicBoxDrumComponent>builder()
                                                                                           .codec(MusicBoxDrumComponent.CODEC)
                                                                                           .build();
    public static final Item MUSIC_BOX_ITEM =
            new BlockItem(MUSIC_BOX, new Item.Settings().component(DRUM_COMPONENT, null));

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, MUSIC_BOX_ID, MUSIC_BOX);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, MUSIC_BOX_ID, MusicBoxBlockEntity.TYPE);
        Registry.register(Registries.ITEM, MUSIC_BOX_ID, MUSIC_BOX_ITEM);
    }

    public static Identifier id(String path) {
        return MUSIC_BOX_ID.withPath(path);
    }
}
package net.anawesomguy.musicbox.block;

import net.anawesomguy.musicbox.WindupMusicBoxMod;
import net.anawesomguy.musicbox.item.MusicBoxDrumComponent;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class MusicBoxBlockEntity extends BlockEntity {
    public static final BlockEntityType<MusicBoxBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(
            MusicBoxBlockEntity::new, WindupMusicBoxMod.MUSIC_BOX).build();

    public MusicBoxDrumComponent drumComponent = null;

    public MusicBoxBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public ItemStack getPickStack(ItemStack in) {
        in.set(WindupMusicBoxMod.DRUM_COMPONENT, drumComponent);
        return in;
    }
}

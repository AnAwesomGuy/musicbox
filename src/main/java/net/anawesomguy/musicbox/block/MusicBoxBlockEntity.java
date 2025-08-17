package net.anawesomguy.musicbox.block;

import net.anawesomguy.musicbox.WindupMusicBoxMod;
import net.anawesomguy.musicbox.item.MusicBoxDrumComponent;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MusicBoxBlockEntity extends BlockEntity {
    public static final BlockEntityType<MusicBoxBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(
        MusicBoxBlockEntity::new, WindupMusicBoxMod.MUSIC_BOX).build();
    public static final int
        KEY_ROTATION = 360 / 30, // 12
        MAX_TENSION = 36;

    public static void tick(World world, BlockPos pos, BlockState state, MusicBoxBlockEntity entity) {
        entity.ticks++;
        if (entity.windCooldown > 0)
            entity.windCooldown--;
    }

    public int ticks;
    private int windCooldown = 0;
    private int keyRotation = 1;
    private int tension = 0;
    public boolean open = true;
    public MusicBoxDrumComponent drumComponent = null;

    public MusicBoxBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public boolean isWinding() {
        return windCooldown > 0;
    }

    public int getTension() {
        return tension;
    }

    public int getKeyRotation() {
        return keyRotation;
    }

    @SuppressWarnings("AssignmentUsedAsCondition")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.isSneaking()) {
            SoundEvent sound = (open = !open) ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE;
            world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1F,
                            world.getRandom().nextFloat() * 0.1F + 0.9F);
        } else if (!isWinding() && tension < MAX_TENSION) {
            windCooldown = 6;
            keyRotation = (keyRotation + 1) % KEY_ROTATION;
            tension++;
            world.playSound(null, pos, WindupMusicBoxMod.MUSIC_BOX_WIND_UP, SoundCategory.BLOCKS, 1F,
                            world.getRandom().nextFloat() * 0.1F + 0.9F);
        }
        return ActionResult.SUCCESS;
    }

    public ItemStack getPickStack(ItemStack in) {
        in.set(WindupMusicBoxMod.DRUM_COMPONENT, drumComponent);
        return in;
    }
}

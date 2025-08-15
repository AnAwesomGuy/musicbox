package net.anawesomguy.musicbox.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import static java.util.Objects.requireNonNull;

public class MusicBoxBlock extends Block implements BlockEntityProvider {
    public MusicBoxBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MusicBoxBlockEntity(pos, state);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getPickStack(world, pos, state, includeData);
        return includeData ?
                ((MusicBoxBlockEntity) requireNonNull(world.getBlockEntity(pos))).getPickStack(stack) :
                stack;
    }
}

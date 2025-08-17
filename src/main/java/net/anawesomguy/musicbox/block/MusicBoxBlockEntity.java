package net.anawesomguy.musicbox.block;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.anawesomguy.musicbox.WindupMusicBoxMod;
import net.anawesomguy.musicbox.item.MusicBoxDrumComponent;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MusicBoxBlockEntity extends BlockEntity {
    public static final BlockEntityType<MusicBoxBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(
        MusicBoxBlockEntity::new, WindupMusicBoxMod.MUSIC_BOX).build();
    public static final int
        KEY_ROTATION = 360 / 30, // 12
        MAX_TENSION = 36;

    public static void tick(World world, BlockPos pos, BlockState state, MusicBoxBlockEntity entity) {
        int ticks = entity.ticks++;
        if (entity.windCooldown > 0)
            entity.windCooldown--;

        // music playing functions
        MusicBoxDrumComponent drumComponent = entity.drumComponent;
        if (drumComponent != null && ticks % drumComponent.getTicksPerBeat() == 0) {
            short[] notes = drumComponent.getNotes();
            int currentNote = entity.currentNote;
            entity.currentNote = (currentNote + 1) % notes.length;
            FloatList pitches = entity.pitchesCache;
            drumComponent.getPitches(currentNote, pitches);
            for (int i = 0, pitchesSize = pitches.size(); i < pitchesSize; i++) {
                float pitch = pitches.getFloat(i);
                world.playSound(null, pos, WindupMusicBoxMod.MUSIC_BOX_NOTE, SoundCategory.BLOCKS, world.getRandom().nextFloat() * 0.2F + 0.9F, pitch);
            }
        }
    }

    public int ticks;
    private int windCooldown = 0;
    private int keyRotation = 1;
    private int tension = 0;
    private int currentNote = 0;
    private final FloatList pitchesCache = new FloatArrayList();
    public boolean open = true;
    public MusicBoxDrumComponent drumComponent = null;

    public MusicBoxBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        drumComponent = components.get(WindupMusicBoxMod.DRUM_COMPONENT);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(WindupMusicBoxMod.DRUM_COMPONENT, drumComponent);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        tension = view.getInt("tension", 0);
        open = view.getBoolean("open", true);
        currentNote = view.getInt("currentNote", 0);
        drumComponent = view.read("drumComponent", MusicBoxDrumComponent.CODEC).orElse(null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("tension", tension);
        view.putBoolean("open", open);
        view.putInt("currentNote", 0);
        if (drumComponent != null)
            view.put("drumComponent", MusicBoxDrumComponent.CODEC, drumComponent);
    }

    public int getCurrentNote() {
        return currentNote;
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

    public ActionResult onUse(World world, BlockPos pos, PlayerEntity player) {
        if (player.isSneaking()) {
            @SuppressWarnings("AssignmentUsedAsCondition")
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

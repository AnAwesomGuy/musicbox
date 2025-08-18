package net.anawesomguy.musicbox.block;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.anawesomguy.musicbox.WindupMusicBoxMod;
import net.anawesomguy.musicbox.item.MusicBoxDataComponent;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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
        if (!world.isClient && entity.tension > 0) {
            int tension = entity.tension;
            entity.tension = tension - 1;
            MusicBoxDataComponent data = entity.data;
            if (data != null) {
                int ticksPerBeat = data.getTicksPerBeat();
                if (tension <= 9)
                    //noinspection SuspiciousIntegerDivAssignment (not sus, intended)
                    ticksPerBeat *= tension / 3;
                if (ticks % ticksPerBeat == 0) {
                    short[] notes = data.getNotes();
                    int currentNote = entity.currentNote;
                    entity.currentNote = (currentNote + 1) % notes.length;
                    IntList semitones = entity.semitonesCache;
                    data.getSemitones(currentNote, semitones);
                    for (int i = 0, semitonesSize = semitones.size(); i < semitonesSize; i++) {
                        double semitone = semitones.getInt(i);
                        world.playSound(null, pos, WindupMusicBoxMod.MUSIC_BOX_NOTE, SoundCategory.BLOCKS,
                                        world.getRandom().nextFloat() * 0.2F + 0.9F,
                                        (float)Math.pow(2.0, semitone / 12.0));
                        ((ServerWorld)world).spawnParticles(
                            ParticleTypes.NOTE, // 7 indents is crazy lol
                            pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                            0, semitone / 24.0,
                            // for some reason the note particle uses the velocityX to determine its color
                            0.0, 0.0, 1.0
                        );
                    }
                }
            }
        }
    }

    public int ticks;
    private int windCooldown = 0;
    private int keyRotation = 1;
    private int tension = 0;
    private int currentNote = 0;
    private final IntList semitonesCache = new IntArrayList();
    public boolean open = true;
    @Nullable // always null on the client
    public MusicBoxDataComponent data = null;
    // @Environment(EnvType.CLIENT)
    public int notesLength;

    public MusicBoxBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("tension", tension);
        nbt.putBoolean("open", open);
        nbt.putInt("currentNote", 0);
        nbt.putInt("notesLength", data == null ? 0 : data.getNotes().length);
        return nbt;
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        data = components.get(WindupMusicBoxMod.MUSIC_BOX_DATA);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(WindupMusicBoxMod.MUSIC_BOX_DATA, data);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        tension = view.getInt("tension", 0);
        open = view.getBoolean("open", true);
        currentNote = view.getInt("currentNote", 0);
        data = view.read("musicBoxData", MusicBoxDataComponent.CODEC).orElse(null);
        notesLength = view.getOptionalInt("notesLength").orElse(0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("tension", tension);
        view.putBoolean("open", open);
        view.putInt("currentNote", 0);
        if (data != null)
            view.put("musicBoxData", MusicBoxDataComponent.CODEC, data);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("tension");
        view.remove("open");
        view.remove("currentNote");
        view.remove("musicBoxData");
        view.remove("notesLength");
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
        in.set(WindupMusicBoxMod.MUSIC_BOX_DATA, data);
        return in;
    }
}

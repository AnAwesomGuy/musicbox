package net.anawesomguy.musicbox.block;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.anawesomguy.musicbox.WindupMusicBoxMod;
import net.anawesomguy.musicbox.item.MusicBoxData;
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
import net.minecraft.text.Text;
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
        if (world.isClient)
            return;

        int ticks = entity.ticks++;
        boolean winding = entity.windCooldown > 0;
        if (winding)
            entity.windCooldown--;

        // music playing functions
        int tension = entity.tension;
        if (tension > 0 && !winding) {
            MusicBoxDataComponent dataEntry = entity.data;
            if (dataEntry != null) {
                MusicBoxData data = dataEntry.value();
                int ticksPerNote = data.getTicksPerNote();
                if (tension < 5)
                    ticksPerNote *= 2;
                if (ticks % ticksPerNote == 0) {
                    if (ticks % data.getTicksPerBeat() == 0)
                        entity.tension = tension - 1;
                    short[] notes = data.getNotes();
                    int currentNote = entity.currentNote;
                    entity.currentNote = (currentNote + 1) % notes.length;
                    IntList semitones = entity.semitonesCache;
                    data.getSemitones(currentNote, semitones);
                    for (int i = 0, semitonesSize = semitones.size(); i < semitonesSize; i++) {
                        double semitone = semitones.getInt(i);
                        SoundEvent sound;
                        double adjustedTone;
                        // mc limits the pitch to only two octaves, so i have to have two sounds to double that
                        if (semitone >= 12) {
                            sound = WindupMusicBoxMod.MUSIC_BOX_NOTE_C6;
                            adjustedTone = semitone - 24.0;
                        } else {
                            sound = WindupMusicBoxMod.MUSIC_BOX_NOTE_C4;
                            adjustedTone = semitone;
                        }
                        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1F,
                                        (float)Math.pow(2.0, adjustedTone / 12.0));
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
    public int notesLength;

    public MusicBoxBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public ActionResult onUse(World world, BlockPos pos, PlayerEntity player) {
        if (player.isSneaking()) {
            @SuppressWarnings("AssignmentUsedAsCondition")
            SoundEvent sound = (open = !open) ? SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE;
            world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1F,
                            world.getRandom().nextFloat() * 0.1F + 0.9F);
        } else if (!world.isClient && windCooldown <= 2 && tension < MAX_TENSION) {
            windCooldown = 7;
            keyRotation = (keyRotation + 1) % KEY_ROTATION;
            tension += 5;
            world.playSound(null, pos, WindupMusicBoxMod.MUSIC_BOX_WIND_UP, SoundCategory.BLOCKS, 1F,
                            world.getRandom().nextFloat() * 0.1F + 0.9F);
            ((ServerWorld)world).getChunkManager().markForUpdate(pos);
            if (data == null)
                player.sendMessage(Text.translatable("windup_music_box.message.music_box.empty"), true);
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    public ItemStack getPickStack(ItemStack in) {
        in.set(WindupMusicBoxMod.MUSIC_BOX_DATA, data);
        return in;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("open", open);
        nbt.putInt("currentNote", 0);
        nbt.putInt("notesLength", data == null ? 0 : data.value().getNotes().length);
        nbt.putInt("keyRotation", keyRotation);
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
        keyRotation = view.getInt("keyRotation", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("tension", tension);
        view.putBoolean("open", open);
        view.putInt("currentNote", 0);
        view.putInt("keyRotation", keyRotation);
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

    public int getTension() {
        return tension;
    }

    public int getKeyRotation() {
        return keyRotation;
    }
}

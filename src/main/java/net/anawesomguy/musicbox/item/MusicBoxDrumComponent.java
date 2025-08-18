package net.anawesomguy.musicbox.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class MusicBoxDrumComponent implements TooltipAppender {
    public static final Codec<short[]> NOTES_CODEC = Codec.of(MusicBoxDrumComponent::encodeNotes,
                                                              MusicBoxDrumComponent::decodeNotes);
    public static final Codec<MusicBoxDrumComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("key_offset", 0).forGetter(MusicBoxDrumComponent::getKeyOffset),
        Codec.BOOL.optionalFieldOf("minor", Boolean.FALSE).forGetter(MusicBoxDrumComponent::isInMinor),
        Codecs.POSITIVE_INT.fieldOf("ticks_per_beat").forGetter(MusicBoxDrumComponent::getTicksPerBeat),
        Codec.STRING.optionalFieldOf("song_artist", "").forGetter(MusicBoxDrumComponent::getArtist),
        Codec.STRING.fieldOf("song_name").forGetter(MusicBoxDrumComponent::getSongName),
        NOTES_CODEC.fieldOf("notes").forGetter(MusicBoxDrumComponent::getNotes)
    ).apply(instance, MusicBoxDrumComponent::new));

    public static final int TOTAL_BEATS = 36; // length of the music
    public static final int NOTES_RANGE = 15; // the amount of notes each value in `notes` represents

    private static final int[] MAJOR_OFFSETS = {0, 2, 4, 5, 7, 9, 11}; // semi-tone offsets in a major key
    private static final int[] MINOR_OFFSETS = {0, 2, 3, 5, 7, 8, 10}; // semi-tone offsets in a minor key
    private static final int SCALE_LENGTH = 7; // MAJOR_OFFSETS.length

    private final int keyOffset;
    private final boolean minor;
    private final int ticksPerBeat; // the music box is always in x/4 tempo so one beat is a quarter note
    private final String artist;
    private final String songName;
    /**
     * An array of all the notes. The first element of the array would be played on the first {@link #ticksPerNote}, the second on the second, and so on.
     * <p>
     * For example, if {@link #keyOffset} was 0, then the first bit would represent C4, the second bit D4, the eighth C5, and so on.
     * <p>
     * The music box can only play 2 and a half octaves, so the sign bit is ignored (shorts have 16 bits)
     */
    private final short[] notes;
    private final int ticksPerNote; // ticks per each value in `notes`

    public MusicBoxDrumComponent(int keyOffset, boolean minor, int ticksPerBeat, String artist, String song) {
        this.keyOffset = keyOffset;
        this.minor = minor;
        if (ticksPerBeat <= 0)
            throw new IllegalArgumentException("ticksPerBeat is not positive");
        this.ticksPerBeat = ticksPerBeat;
        this.artist = Objects.requireNonNull(artist);
        this.songName = Objects.requireNonNull(song);
        // basically, if it's divisible by 2, then ticksPerNote = ticksPerBeat / 2,
        // if it's divisible by 4, then it's ticksPerBeat / 4,
        // and the same for 8
        int ticksPerNote = this.ticksPerNote = (ticksPerBeat % 2 == 0) ? (ticksPerBeat / (ticksPerBeat % 4 == 0 ? (ticksPerBeat % 8 == 0 ? 8 : 4) : 2)) : ticksPerBeat;
        this.notes = new short[TOTAL_BEATS * ticksPerNote];
    }

    public MusicBoxDrumComponent(int keyOffset, boolean minor, int ticksPerBeat, String artist, String song, short[] notes) {
        this(keyOffset, minor, ticksPerBeat, artist, song);
        System.arraycopy(notes, 0, this.notes, 0, Math.min(notes.length, this.notes.length));
    }

    public void getSemitones(int index, IntList output) {
        output.clear();
        short notes = this.notes[index];
        if (notes == 0 || notes == -32768)
            return;
        int i = NOTES_RANGE;
        while (i-- > 0) { // 14, 13, ... 1, 0
            if ((notes & 1) == 1) {
                int semitoneOffset = (minor ? MINOR_OFFSETS : MAJOR_OFFSETS)[i % SCALE_LENGTH] + keyOffset;
                output.add(semitoneOffset);
            }
            notes >>>= 1;
        }
    }

    public String getArtist() {
        return artist;
    }

    public String getSongName() {
        return songName;
    }

    // PLEASE DO NOT MODIFY
    public short[] getNotes() {
        return notes;
    }

    public int getKeyOffset() {
        return keyOffset;
    }

    public boolean isInMinor() {
        return minor;
    }

    public int getTicksPerBeat() {
        return ticksPerBeat;
    }

    public int getTicksPerNote() {
        return ticksPerNote;
    }

    public int getBpm() {
        return (60 * 20) / ticksPerBeat;
    }

    public float getBpm(float tickRate) {
        return (60F * tickRate) / ticksPerBeat;
    }

    public static <T> DataResult<T> encodeNotes(short[] notes, DynamicOps<T> ops, T prefix) {
        ListBuilder<T> list = ops.listBuilder();
        for (int i = 0, j = 0, len = notes.length; i < len; i++) {
            short note = notes[i];
            if (note == 0 || note == -32768) {
                j++;
            } else {
                if (j > 0) {
                    list.add(ops.createInt(j));
                    j = 0;
                }
                list.add(ops.createString(
                    StringUtils.leftPad(
                        StringUtils.substring(Integer.toBinaryString(note), -NOTES_RANGE),
                        NOTES_RANGE,
                        '0'
                    )));
            }
        }
        return list.build(prefix);
    }

    public static <T> DataResult<Pair<short[], T>> decodeNotes(DynamicOps<T> ops, T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).map(stream -> {
            ShortList list = new ShortArrayList(72);
            Stream.Builder<T> failed = Stream.builder();
            stream.accept(t -> {
                DataResult<String> oString = ops.getStringValue(t);
                if (oString.isSuccess()) {
                    String str = oString.getOrThrow();
                    short s;
                    try {
                        s = Short.parseShort(String.format("+%.15s", str), 2);
                    } catch (NumberFormatException e) {
                        failed.add(ops.createString("Invalid binary input, replacing with 0: " + str));
                        s = 0;
                    }
                    list.add(s);
                    return;
                }

                DataResult<Number> oNum = ops.getNumberValue(t);
                if (oNum.isSuccess()) {
                    int s = oNum.getOrThrow().intValue();
                    while (s-- > 0) {
                        list.add((short)0);
                    }
                }

            });
            return Pair.of(list.toShortArray(), ops.createList(failed.build()));
        });
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        if (!songName.isEmpty()) {
            Text songText = Text.literal(songName).formatted(Formatting.ITALIC, Formatting.GRAY);
            Text text = artist.isEmpty() ?
                songText :
                Text.literal(artist + " - ").formatted(Formatting.GRAY).append(songText);
            textConsumer.accept(text);
        }
    }
}

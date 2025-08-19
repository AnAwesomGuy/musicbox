package net.anawesomguy.musicbox.item;

import com.mojang.serialization.Codec;
import net.anawesomguy.musicbox.WindupMusicBoxMod;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public record MusicBoxDataComponent(RegistryEntry<MusicBoxData> entry) implements TooltipAppender {
    public static final Codec<MusicBoxDataComponent> CODEC =
        RegistryElementCodec.of(WindupMusicBoxMod.MUSIC_BOX_DATA_KEY, MusicBoxData.CODEC)
                            .xmap(MusicBoxDataComponent::new, MusicBoxDataComponent::entry);
    public static final PacketCodec<RegistryByteBuf, MusicBoxDataComponent> PACKET_CODEC = PacketCodecs.registryEntry(
        WindupMusicBoxMod.MUSIC_BOX_DATA_KEY, MusicBoxData.PACKET_CODEC
    ).xmap(MusicBoxDataComponent::new, MusicBoxDataComponent::entry);

    public MusicBoxData value() {
        return entry.value();
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        entry.value().appendTooltip(context, textConsumer, type, components);
    }
}

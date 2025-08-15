package net.anawesomguy.musicbox;

import net.anawesomguy.musicbox.block.MusicBoxBlockEntity;
import net.anawesomguy.musicbox.client.block.MusicBoxModel;
import net.anawesomguy.musicbox.client.block.MusicBoxRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class WindupMusicBoxClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(MusicBoxRenderer.LAYER_LOCATION,
                                                    MusicBoxModel::getTexturedModelData);
        BlockEntityRendererFactories.register(MusicBoxBlockEntity.TYPE, MusicBoxRenderer::new);
    }
}
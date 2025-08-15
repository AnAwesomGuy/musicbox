package net.anawesomguy.musicbox.client.block;

import net.anawesomguy.musicbox.WindupMusicBoxMod;
import net.anawesomguy.musicbox.block.MusicBoxBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class MusicBoxRenderer implements BlockEntityRenderer<MusicBoxBlockEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(WindupMusicBoxMod.MUSIC_BOX_ID, "main");

    private final MusicBoxModel model;

    public MusicBoxRenderer(BlockEntityRendererFactory.Context ctx) {
        this(ctx.getLoadedEntityModels());
    }

    public MusicBoxRenderer(LoadedEntityModels models) {
        this.model = new MusicBoxModel(models.getModelPart(LAYER_LOCATION));
    }

    @Override
    public void render(MusicBoxBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {

    }
}

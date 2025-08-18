package net.anawesomguy.musicbox.client.block;

import net.anawesomguy.musicbox.WindupMusicBoxMod;
import net.anawesomguy.musicbox.block.MusicBoxBlock;
import net.anawesomguy.musicbox.block.MusicBoxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.math.Vec3d;

public class MusicBoxRenderer implements BlockEntityRenderer<MusicBoxBlockEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(WindupMusicBoxMod.MUSIC_BOX_ID, "main");
    @SuppressWarnings("deprecation")
    public static final SpriteIdentifier MUSIC_BOX_TEXTURE = new SpriteIdentifier(
        SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, WindupMusicBoxMod.id("block/music_box"));

    private final MusicBoxModel model;

    public MusicBoxRenderer(BlockEntityRendererFactory.Context ctx) {
        this(ctx.getLoadedEntityModels());
    }

    public MusicBoxRenderer(LoadedEntityModels models) {
        this.model = new MusicBoxModel(models.getModelPart(LAYER_LOCATION));
    }

    @Override
    public void render(MusicBoxBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        model.update(entity, tickProgress);
        matrices.push();
        matrices.multiply(
            RotationAxis.NEGATIVE_Y.rotationDegrees(
                RotationPropertyHelper.toDegrees(entity.getCachedState().get(MusicBoxBlock.ROTATION))),
            0.5F, 0F, 0.5F);
        VertexConsumer vertexConsumer = MUSIC_BOX_TEXTURE.getVertexConsumer(vertexConsumers,
                                                                            RenderLayer::getEntitySolid);
        model.render(matrices, vertexConsumer, light, overlay);
        matrices.pop();
    }
}

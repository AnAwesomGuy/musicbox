package net.anawesomguy.musicbox.client.block;

import net.anawesomguy.musicbox.block.MusicBoxBlockEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.client.model.ModelPartBuilder.create;

public class MusicBoxModel extends Model {
    private final ModelPart key;
    private final ModelPart governer;
    private final ModelPart drum;
    private final ModelPart lid;

    public MusicBoxModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
        this.key = root.getChild("key");
        this.governer = root.getChild("governer");
        this.drum = root.getChild("drum");
        this.lid = root.getChild("lid");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("key",
                               create().uv(0, 12)
                                       .cuboid(-0.5F, -0.5F, 0F, 1F, 1F, 2F)
                                       .uv(0, 15)
                                       .cuboid(-1.5F, -0.5F, 0F, 3F, 1F, 0F),
                               ModelTransform.of(2.5F, 20.5F, -7F, 0F, 0F, MathHelper.RADIANS_PER_DEGREE * 30));

        ModelPartData shell = modelPartData.addChild("shell", create().uv(0, 8).cuboid(-4F, -2F, -4F, 8F, 1F, 8F),
                                                     ModelTransform.NONE);
        ModelPartBuilder side = create().uv(0, 0).cuboid(-5F, -7F, 4F, 9F, 7F, 1F);
        shell.addChild("north", side, ModelTransform.of(0F, 0F, 0F, 0F, MathHelper.PI, 0F));
        shell.addChild("south", side, ModelTransform.NONE);
        shell.addChild("east", side, ModelTransform.of(0F, 0F, 0F, 0F, -MathHelper.TAU, 0F));
        shell.addChild("west", side, ModelTransform.of(0F, 0F, 0F, 0F, MathHelper.TAU, 0F));

        ModelPartData insides = modelPartData.addChild("insides", create().uv(0, 24)
                                                                          .cuboid(1F, -6F, -4F, 3F, 4F, 4F)
                                                                          .uv(18, 0)
                                                                          .cuboid(-4F, -4F, 0F, 5F, 0F, 4F)
                                                                          .uv(0, 17)
                                                                          .cuboid(-4F, -2.5F, 0F, 8F, 1F, 4F)
                                                                          .uv(-3, 29)
                                                                          .cuboid(2F, -6F, 0F, 1F, 0F, 3F)
                                                                          .uv(4, 17)
                                                                          .cuboid(2F, -6.25F, 1.5F, 1F, 4F, 1F,
                                                                                  new Dilation(-0.2499F)),
                                                       ModelTransform.NONE);
        insides.addChild("gear", create().uv(0, 28).cuboid(-0.5F, -1F, -1F, 1F, 2F, 2F),
                         ModelTransform.of(2.5F, -3F, 0.25F, -MathHelper.PI / 4, 0F, 0F));

        modelPartData.addChild("governer", create().uv(0, 30).cuboid(1F, -5.5F, 1.5F, 3F, 1F, 1F, new Dilation(-0.1F)),
                               ModelTransform.NONE);
        modelPartData.addChild("drum", create().uv(11, 26).cuboid(-2.5F, -1.5F, -1.5F, 5F, 3F, 3F),
                               ModelTransform.of(-1.5F, -4F, -2F, MathHelper.PI / 4, 0F, 0F));
        modelPartData.addChild("lid", create().uv(0, 0).cuboid(-5F, -10F, 0F, 10F, 10F, 1F),
                               ModelTransform.of(0F, -7F, 5F, -MathHelper.RADIANS_PER_DEGREE * 30, 0F, 0F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    public void update(MusicBoxBlockEntity entity) {
        drum.visible = entity.drumComponent != null;
    }
}
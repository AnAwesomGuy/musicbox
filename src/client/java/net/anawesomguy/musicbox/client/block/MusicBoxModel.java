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

import static net.minecraft.client.model.ModelPartBuilder.create;
import static net.minecraft.util.math.MathHelper.HALF_PI;
import static net.minecraft.util.math.MathHelper.PI;
import static net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE;

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
                               create().uv(24, 5)
                                       .cuboid(-0.5F, -0.5F, 0F, 1F, 1F, 2F)
                                       .uv(24, 0)
                                       .cuboid(-1.5F, -0.5F, 0.001F, 3F, 1F, 0F),
                               ModelTransform.origin(5.5F, 3.5F, 1F));

        ModelPartData shell = modelPartData.addChild("shell", create().uv(0, 0).cuboid(4F, 1F, 4F, 8F, 1F, 8F),
                                                     ModelTransform.NONE);
        ModelPartBuilder side = create().uv(22, 9).cuboid(-5F, 0F, 4F, 9F, 7F, 1F);
        shell.addChild("north", side, ModelTransform.of(8F, 0F, 8F, 0F, PI, 0F));
        shell.addChild("south", side, ModelTransform.origin(8F, 0F, 8F));
        shell.addChild("east", side, ModelTransform.of(8F, 0F, 8F, 0F, -HALF_PI, 0));
        shell.addChild("west", side, ModelTransform.of(8F, 0F, 8F, 0F, HALF_PI, 0F));

        ModelPartData insides = modelPartData.addChild("insides", create().uv(24, 17)
                                                                          .mirrored()
                                                                          .cuboid(4F, 2F, 4F, 3F, 4F, 4F)
                                                                          .uv(16, 25)
                                                                          .cuboid(7F, 4F, 8F, 5F, 0F, 4F)
                                                                          .uv(0, 20)
                                                                          .cuboid(4F, 1.5F, 8F, 8F, 1F, 4F)
                                                                          .uv(35, 21)
                                                                          .cuboid(5F, 6F, 8F, 1F, 0F, 3F)
                                                                          .uv(32, 4)
                                                                          .cuboid(5F, 2.249F, 9.5F, 1F, 4F, 1F,
                                                                                  new Dilation(-0.25F)),
                                                       ModelTransform.NONE);
        insides.addChild("gear", create().uv(36, 5).cuboid(-0.5F, -1F, -0.5F, 1F, 2F, 2F),
                         ModelTransform.of(5.5F, 3F, 7.75F, PI / -4F, 0F, 0F));

        modelPartData.addChild("governer",
                               create().uv(30, 27).cuboid(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new Dilation(-0.1F)),
                               ModelTransform.origin(5.5F, 5, 10));
        modelPartData.addChild("drum", create().uv(0, 25).cuboid(-2.5F, -1.5F, -1.5F, 5F, 3F, 3F),
                               ModelTransform.origin(9.5F, 4F, 6F));
        modelPartData.addChild("lid", create().uv(0, 9).cuboid(-5F, 0F, 0F, 10F, 10F, 1F),
                               ModelTransform.origin(8F, 7F, 13F));

        return TexturedModelData.of(modelData, 64, 32);
    }

    public void update(MusicBoxBlockEntity entity, float tickProgress) {
        int notesLength = entity.notesLength;
        drum.visible = notesLength > 0;
//        if (notesLength > 0) {
//            drum.pitch = TAU * ((float)entity.getCurrentNote() / notesLength - 1) + (PI / 4F); // 360deg * (value from 0-1)
//            drum.visible = true;
//        } else
//            drum.visible = false;
//        governer.yaw = (drum.pitch * 200) % TAU; // it's fast
        lid.pitch = (entity.open ? RADIANS_PER_DEGREE * 30F : -HALF_PI);
        key.roll = RADIANS_PER_DEGREE * 30F * entity.getKeyRotation();
    }
}
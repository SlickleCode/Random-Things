package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

public class PlatformBlock extends Block {
    protected static final VoxelShape SHAPE = Block.box(0, 14, 0, 16, 16, 16);

    public PlatformBlock() {
        super(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Entity entity = context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ecc ? ecc.getEntity() : null;

        if (entity instanceof Player && entity.isShiftKeyDown() || entity != null && entity.getY() < pos.getY() + 14F / 16F) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, worldIn, pos, context);
    }
}

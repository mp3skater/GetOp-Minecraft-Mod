package net.mp3skater.getop.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.mp3skater.getop.block.entity.custom.AnvilOfSageBlock_Entity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.Map;

public class AnvilOfSageBlock extends FallingBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Map für die Hitboxes pro Himmelsrichtung
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        // --- 1. East ---
        VoxelShape plateN = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
        VoxelShape pillarN = Block.box(5.0D, 4.0D, 4.0D, 11.0D, 11.0D, 12.0D);
        VoxelShape topN = Block.box(4.0D, 11.0D, 0.0D, 12.0D, 16.0D, 16.0D);
        SHAPES.put(Direction.EAST, Shapes.or(plateN, pillarN, topN));

        // --- 2. North ---
        VoxelShape plateE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
        VoxelShape pillarE = Block.box(4.0D, 4.0D, 5.0D, 12.0D, 11.0D, 11.0D);
        VoxelShape topE = Block.box(0.0D, 11.0D, 4.0D, 16.0D, 16.0D, 12.0D);
        SHAPES.put(Direction.NORTH, Shapes.or(plateE, pillarE, topE));

        // --- 3. West ---
        VoxelShape plateS = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
        VoxelShape pillarS = Block.box(5.0D, 4.0D, 4.0D, 11.0D, 11.0D, 12.0D);
        VoxelShape topS = Block.box(4.0D, 11.0D, 0.0D, 12.0D, 16.0D, 16.0D); // Gleicher Aufbau, da symmetrisch gedreht
        SHAPES.put(Direction.WEST, Shapes.or(plateS, pillarS, topS));

        // --- 4. South ---
        VoxelShape plateW = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
        VoxelShape pillarW = Block.box(4.0D, 4.0D, 5.0D, 12.0D, 11.0D, 11.0D);
        VoxelShape topW = Block.box(0.0D, 11.0D, 4.0D, 16.0D, 16.0D, 12.0D);
        SHAPES.put(Direction.SOUTH, Shapes.or(plateW, pillarW, topW));
    }

    public AnvilOfSageBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    protected void falling(FallingBlockEntity pFallingEntity) {
        pFallingEntity.setHurtsEntities(16f, 40);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction facing = pState.hasProperty(FACING) ? pState.getValue(FACING) : Direction.NORTH;
        return SHAPES.getOrDefault(facing, SHAPES.get(Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    /* BLOCK ENTITY */

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof AnvilOfSageBlock_Entity) {
                ((AnvilOfSageBlock_Entity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof AnvilOfSageBlock_Entity) {
                NetworkHooks.openGui((ServerPlayer) pPlayer, (MenuProvider) entity, pPos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AnvilOfSageBlock_Entity(pPos, pState);
    }

    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        super.triggerEvent(pState, pLevel, pPos, pId, pParam);
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        return blockentity == null ? false : blockentity.triggerEvent(pId, pParam);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        return blockentity instanceof MenuProvider ? (MenuProvider) blockentity : null;
    }
}
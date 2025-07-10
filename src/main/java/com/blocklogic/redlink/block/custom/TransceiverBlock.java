package com.blocklogic.redlink.block.custom;

import com.blocklogic.redlink.block.entity.RLBlockEntities;
import com.blocklogic.redlink.block.entity.TransceiverBlockEntity;
import com.blocklogic.redlink.item.custom.RedstoneRemoteItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class TransceiverBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty PULSE_MODE = BooleanProperty.create("pulse_mode");

    public static final MapCodec<TransceiverBlock> CODEC = simpleCodec(TransceiverBlock::new);

    private static final VoxelShape DOWN_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 2.0, 12.0);
    private static final VoxelShape UP_SHAPE = Block.box(4.0, 14.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape NORTH_SHAPE = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 2.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0, 4.0, 4.0, 2.0, 12.0, 12.0);
    private static final VoxelShape EAST_SHAPE = Block.box(14.0, 4.0, 4.0, 16.0, 12.0, 12.0);

    public TransceiverBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(ACTIVE, false)
                .setValue(PULSE_MODE, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> DOWN_SHAPE;
            case UP -> UP_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TransceiverBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockPos supportingBlockPos = context.getClickedPos().relative(clickedFace.getOpposite());

        if (context.getLevel().getBlockState(supportingBlockPos).isFaceSturdy(context.getLevel(), supportingBlockPos, clickedFace)) {
            return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite());
        }

        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof RedstoneRemoteItem remoteItem) {
            if (!player.isShiftKeyDown()) {
                return handleRemoteLinking(remoteItem, stack, level, pos, player);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            return handleModeSwitch(level, pos, player);
        }

        return handleStatusDisplay(level, pos, player);
    }

    private ItemInteractionResult handleRemoteLinking(RedstoneRemoteItem remoteItem, ItemStack stack, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!remoteItem.isBoundToHub(stack)) {
            player.displayClientMessage(Component.translatable("redlink.transceiver.remote_not_bound"), true);
            return ItemInteractionResult.FAIL;
        }

        if (!remoteItem.isHubAccessible(stack, level, player)) {
            player.displayClientMessage(Component.translatable("redlink.transceiver.hub_not_accessible"), true);
            return ItemInteractionResult.FAIL;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TransceiverBlockEntity transceiverEntity)) {
            return ItemInteractionResult.FAIL;
        }

        BlockPos hubPos = remoteItem.getBoundHubPos(stack);
        int channel = remoteItem.getCurrentChannel(stack);

        if (transceiverEntity.linkToHub(hubPos, channel)) {
            updateBlockState(level, pos, transceiverEntity);

            var hub = remoteItem.getBoundHub(stack, level);
            if (hub != null) {
                String channelName = hub.getChannelName(channel);
                int channelColor = hub.getChannelColor(channel);

                Component channelComponent = Component.literal(channelName)
                        .withStyle(style -> style.withColor(channelColor));

                player.displayClientMessage(Component.translatable("redlink.transceiver.linked_to_channel",
                        channelComponent), true);
            }

            return ItemInteractionResult.SUCCESS;
        } else {
            player.displayClientMessage(Component.translatable("redlink.transceiver.link_failed"), true);
            return ItemInteractionResult.FAIL;
        }
    }

    private InteractionResult handleModeSwitch(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TransceiverBlockEntity transceiverEntity)) {
            return InteractionResult.FAIL;
        }

        transceiverEntity.switchMode();
        updateBlockState(level, pos, transceiverEntity);

        Component modeComponent = transceiverEntity.getModeDisplayName();
        player.displayClientMessage(Component.translatable("redlink.transceiver.mode_switched", modeComponent), true);

        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleStatusDisplay(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TransceiverBlockEntity transceiverEntity)) {
            return InteractionResult.FAIL;
        }

        player.displayClientMessage(transceiverEntity.getChannelInfo(), false);
        player.displayClientMessage(transceiverEntity.getStatusInfo(), false);

        return InteractionResult.SUCCESS;
    }

    public void updateBlockState(Level level, BlockPos pos, TransceiverBlockEntity entity) {
        BlockState currentState = level.getBlockState(pos);
        BlockState newState = currentState
                .setValue(ACTIVE, entity.isActive())
                .setValue(PULSE_MODE, entity.isPulseMode());

        if (!currentState.equals(newState)) {
            level.setBlock(pos, newState, 3);
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite() && !this.canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing.getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, PULSE_MODE);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(FACING) == direction ? getSignal(state, level, pos, direction) : 0;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(blockEntityType, RLBlockEntities.TRANSCEIVER_BLOCK_ENTITY.get(),
                (level1, pos, state1, blockEntity) -> blockEntity.tick());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TransceiverBlockEntity transceiverEntity) {
                transceiverEntity.unlink();
            }

            level.updateNeighborsAt(pos, this);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
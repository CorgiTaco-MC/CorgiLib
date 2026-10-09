package corgitaco.corgilib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import corgitaco.corgilib.network.UpdateStructureBoxPacketC2S;
import corgitaco.corgilib.platform.PlatformNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BoundingBoxRenderable;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;

public class StructureBoxEditor {
    @Nullable
    public static AABB structureBox;
    @Nullable
    public static BlockPos structureBlockPos;
    @Nullable
    public static BlockPos structureOffset;
    private static double scrollRemainder;

    public static void beginEditing(StructureBlockEntity blockEntity) {
        clear();
        Vec3i size = blockEntity.getStructureSize();
        if (blockEntity.getMode() == StructureMode.SAVE && size.getX() > 0 && size.getY() > 0 && size.getZ() > 0) {
            structureBox = new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ());
            structureBlockPos = blockEntity.getBlockPos().immutable();
            structureOffset = blockEntity.getStructurePos().immutable();
        }
    }

    public static void clear() {
        structureBox = null;
        structureBlockPos = null;
        structureOffset = null;
        scrollRemainder = 0;
    }

    public static void extractGizmos(LevelRenderer levelRenderer) {
        AABB box = getEditedWorldBox();
        if (box != null) {
            try (Gizmos.TemporaryCollection ignored = levelRenderer.collectPerFrameGizmos()) {
                Gizmos.cuboid(box, GizmoStyle.stroke(0xFFFF0000, 2.5F));
            }
        }
    }

    public static void render(PoseStack stack, VertexConsumer consumer, double camX, double camY, double camZ, boolean coloredCorner, LevelRenderer levelRenderer) {
        AABB box = getEditedWorldBox();
        if (box != null) {
            ShapeRenderer.renderShape(stack, consumer, Shapes.create(box), -camX, -camY, -camZ, 0xFFFF0000, 2.5F);
        }
    }

    public static boolean onScroll(double scrollValue) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || minecraft.getOverlay() != null || !player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.GOLDEN_AXE) || !Double.isFinite(scrollValue)) {
            scrollRemainder = 0;
            return false;
        }
        boolean resize = isKeyDown(minecraft, GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(minecraft, GLFW.GLFW_KEY_RIGHT_SHIFT);
        boolean move = isKeyDown(minecraft, GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyDown(minecraft, GLFW.GLFW_KEY_RIGHT_CONTROL);
        if (!resize && !move) {
            scrollRemainder = 0;
            return false;
        }
        AABB worldBox = getEditedWorldBox();
        if (worldBox == null) {
            return false;
        }
        Vec3 eyePosition = player.getEyePosition();
        Vec3 end = eyePosition.add(player.getViewVector(1.0F).scale(250));
        BlockHitResult hit = worldBox.contains(eyePosition)
                ? AABB.clip(Collections.singleton(worldBox), end, eyePosition, BlockPos.ZERO)
                : AABB.clip(Collections.singleton(worldBox), eyePosition, end, BlockPos.ZERO);
        if (hit == null) {
            scrollRemainder = 0;
            return false;
        }
        scrollRemainder = Math.clamp(scrollRemainder + scrollValue, -StructureBlockEntity.MAX_SIZE_PER_AXIS * 2.0, StructureBlockEntity.MAX_SIZE_PER_AXIS * 2.0);
        int steps = (int) scrollRemainder;
        scrollRemainder -= steps;
        if (steps == 0) {
            return true;
        }

        Direction worldDirection = hit.getDirection().getOpposite();
        if (resize) {
            StructureBlockEntity blockEntity = getEditedBlockEntity();
            if (blockEntity == null) {
                return false;
            }
            Rotation inverse = switch (blockEntity.getRotation()) {
                case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
                case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
                case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
                default -> Rotation.NONE;
            };
            Direction localDirection = blockEntity.getMirror().mirror(inverse.rotate(worldDirection));
            structureBox = new AABB(0, 0, 0,
                    clampSize((int) structureBox.getXsize() + localDirection.getStepX() * steps),
                    clampSize((int) structureBox.getYsize() + localDirection.getStepY() * steps),
                    clampSize((int) structureBox.getZsize() + localDirection.getStepZ() * steps));
        } else {
            structureOffset = new BlockPos(
                    clampOffset(structureOffset.getX() + worldDirection.getStepX() * steps),
                    clampOffset(structureOffset.getY() + worldDirection.getStepY() * steps),
                    clampOffset(structureOffset.getZ() + worldDirection.getStepZ() * steps));
        }
        BoundingBox dimensions = new BoundingBox(0, 0, 0,
                (int) structureBox.getXsize() - 1, (int) structureBox.getYsize() - 1, (int) structureBox.getZsize() - 1);
        PlatformNetwork.NETWORK.sendToServer(new UpdateStructureBoxPacketC2S(structureBlockPos, structureOffset, dimensions));
        return true;
    }

    private static int clampSize(int value) {
        return Math.clamp(value, 1, StructureBlockEntity.MAX_SIZE_PER_AXIS);
    }

    private static int clampOffset(int value) {
        return Math.clamp(value, -StructureBlockEntity.MAX_OFFSET_PER_AXIS, StructureBlockEntity.MAX_OFFSET_PER_AXIS);
    }

    @Nullable
    private static StructureBlockEntity getEditedBlockEntity() {
        Minecraft minecraft = Minecraft.getInstance();
        if (structureBox == null || structureBlockPos == null || structureOffset == null || minecraft.level == null) {
            return null;
        }
        if (minecraft.level.getBlockEntity(structureBlockPos) instanceof StructureBlockEntity blockEntity && blockEntity.getMode() == StructureMode.SAVE) {
            return blockEntity;
        }
        if (minecraft.player != null) {
            minecraft.player.sendOverlayMessage(Component.literal("No longer editing structure block."));
        }
        clear();
        return null;
    }

    @Nullable
    private static AABB getEditedWorldBox() {
        StructureBlockEntity blockEntity = getEditedBlockEntity();
        if (blockEntity == null) {
            return null;
        }
        return transformedBox(structureOffset, new Vec3i((int) structureBox.getXsize(), (int) structureBox.getYsize(), (int) structureBox.getZsize()),
                blockEntity.getMirror(), blockEntity.getRotation()).move(structureBlockPos);
    }

    @Nullable
    public static AABB getStructureWorldBox(StructureBlockEntity blockEntity) {
        Vec3i size = blockEntity.getStructureSize();
        if (blockEntity.getMode() != StructureMode.SAVE || size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
            return null;
        }
        BoundingBoxRenderable.RenderableBox box = blockEntity.getRenderableBox();
        BlockPos origin = box.localPos();
        return new AABB(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + box.size().getX(),
                origin.getY() + box.size().getY(), origin.getZ() + box.size().getZ());
    }

    static AABB transformedBox(BlockPos offset, Vec3i size, Mirror mirror, Rotation rotation) {
        int xDiff = mirror == Mirror.FRONT_BACK ? -size.getX() : size.getX();
        int zDiff = mirror == Mirror.LEFT_RIGHT ? -size.getZ() : size.getZ();
        int x0;
        int z0;
        int x1;
        int z1;
        switch (rotation) {
            case CLOCKWISE_90 -> {
                x0 = offset.getX() + (zDiff < 0 ? 0 : 1);
                z0 = offset.getZ() + (xDiff < 0 ? 1 : 0);
                x1 = x0 - zDiff;
                z1 = z0 + xDiff;
            }
            case CLOCKWISE_180 -> {
                x0 = offset.getX() + (xDiff < 0 ? 0 : 1);
                z0 = offset.getZ() + (zDiff < 0 ? 0 : 1);
                x1 = x0 - xDiff;
                z1 = z0 - zDiff;
            }
            case COUNTERCLOCKWISE_90 -> {
                x0 = offset.getX() + (zDiff < 0 ? 1 : 0);
                z0 = offset.getZ() + (xDiff < 0 ? 0 : 1);
                x1 = x0 + zDiff;
                z1 = z0 - xDiff;
            }
            default -> {
                x0 = offset.getX() + (xDiff < 0 ? 1 : 0);
                z0 = offset.getZ() + (zDiff < 0 ? 1 : 0);
                x1 = x0 + xDiff;
                z1 = z0 + zDiff;
            }
        }
        return new AABB(x0, offset.getY(), z0, x1, offset.getY() + size.getY(), z1);
    }

    public static boolean isKeyDown(Minecraft minecraft, int keyValue) {
        return InputConstants.isKeyDown(minecraft.getWindow(), keyValue);
    }
}

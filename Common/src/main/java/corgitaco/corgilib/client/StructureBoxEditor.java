package corgitaco.corgilib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import corgitaco.corgilib.network.UpdateStructureBoxPacketC2S;
import corgitaco.corgilib.platform.PlatformNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;

public class StructureBoxEditor {

    public static AABB structureBox = null;

    public static BlockPos structureBlockPos;
    public static BlockPos structureOffset;


    public static void render(PoseStack stack, VertexConsumer consumer, double camX, double camY, double camZ, boolean b) {
        if (structureBox != null && structureBlockPos != null && structureOffset != null) {
            AABB aabb = structureBox.move(structureOffset).move(structureBlockPos);

            LevelRenderer.renderVoxelShape(stack, consumer, Shapes.create(aabb), -camX, -camY - 1, -camZ, 1, 0, 0, 0, b);
        }
    }


    public static boolean onScroll(double scrollValue) {
        if (structureBox != null && structureBlockPos != null && structureOffset != null) {

            LocalPlayer player = Minecraft.getInstance().player;


            if (player != null && player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.GOLDEN_AXE)) {
                if (!Minecraft.getInstance().level.getBlockState(structureBlockPos).is(Blocks.STRUCTURE_BLOCK)) {
                    player.displayClientMessage(Component.literal("No longer editing structure block."), true);
                    structureBox = null;
                    structureBlockPos = null;
                    structureOffset = null;
                    return false;
                }

                Vec3 eyePosition = player.getEyePosition().add(0, 1, 0);

                Vec3 viewVector = player.getViewVector(0);

                int distance = 250;
                Vec3 add = eyePosition.add(viewVector.x * distance, viewVector.y * distance, viewVector.z * distance);

                BlockHitResult clip = AABB.clip(Collections.singleton(structureBox.move(structureOffset).move(structureBlockPos)), eyePosition, add, BlockPos.ZERO);

                if (clip != null && clip.getType() != HitResult.Type.MISS) {
                    Direction opposite = clip.getDirection().getOpposite();
                    double xStep = opposite.getStepX() * scrollValue;
                    double yStep = opposite.getStepY() * scrollValue;
                    double zStep = opposite.getStepZ() * scrollValue;
                    if (isKeyDown(Minecraft.getInstance(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                        structureBox = new AABB(structureBox.minX, structureBox.minY, structureBox.minZ, Math.max(structureBox.minX + 1, structureBox.maxX + xStep), Math.max(structureBox.minY + 1, structureBox.maxY + yStep), Math.max(structureBox.minZ + 1, structureBox.maxZ + zStep));
                        PlatformNetwork.NETWORK.sendToServer(new UpdateStructureBoxPacketC2S(structureBlockPos, structureOffset, new BoundingBox((int) structureBox.minX, (int) structureBox.minY, (int) structureBox.minZ, (int) structureBox.maxX, (int) structureBox.maxY, (int) structureBox.maxZ)));
                        return true;
                    }

                    if (isKeyDown(Minecraft.getInstance(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
                        structureOffset = structureOffset.offset((int) xStep, (int) yStep, (int) zStep);
                        PlatformNetwork.NETWORK.sendToServer(new UpdateStructureBoxPacketC2S(structureBlockPos, structureOffset, new BoundingBox((int) structureBox.minX, (int) structureBox.minY, (int) structureBox.minZ, (int) structureBox.maxX, (int) structureBox.maxY, (int) structureBox.maxZ)));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public static AABB getStructureWorldBox(StructureBlockEntity pBlockEntity) {
        BlockPos structurePos = pBlockEntity.getStructurePos();
        Vec3i structureSize = pBlockEntity.getStructureSize();
        if (structureSize.getX() >= 1 && structureSize.getY() >= 1 && structureSize.getZ() >= 1) {
            if (pBlockEntity.getMode() == StructureMode.SAVE) {
                double structurePosX = structurePos.getX();
                double structurePosZ = structurePos.getZ();
                double minY = structurePos.getY();
                double maxY = minY + (double) structureSize.getY();
                double structureSizeX;
                double structureSizeZ;
                switch (pBlockEntity.getMirror()) {
                    case LEFT_RIGHT -> {
                        structureSizeX = structureSize.getX();
                        structureSizeZ = -structureSize.getZ();
                    }
                    case FRONT_BACK -> {
                        structureSizeX = -structureSize.getX();
                        structureSizeZ = structureSize.getZ();
                    }
                    default -> {
                        structureSizeX = structureSize.getX();
                        structureSizeZ = structureSize.getZ();
                    }
                }

                double minX;
                double minZ;
                double maxX;
                double maxZ;
                switch (pBlockEntity.getRotation()) {
                    case CLOCKWISE_90 -> {
                        minX = structureSizeZ < 0.0D ? structurePosX : structurePosX + 1.0D;
                        minZ = structureSizeX < 0.0D ? structurePosZ + 1.0D : structurePosZ;
                        maxX = minX - structureSizeZ;
                        maxZ = minZ + structureSizeX;
                    }
                    case CLOCKWISE_180 -> {
                        minX = structureSizeX < 0.0D ? structurePosX : structurePosX + 1.0D;
                        minZ = structureSizeZ < 0.0D ? structurePosZ : structurePosZ + 1.0D;
                        maxX = minX - structureSizeX;
                        maxZ = minZ - structureSizeZ;
                    }
                    case COUNTERCLOCKWISE_90 -> {
                        minX = structureSizeZ < 0.0D ? structurePosX + 1.0D : structurePosX;
                        minZ = structureSizeX < 0.0D ? structurePosZ : structurePosZ + 1.0D;
                        maxX = minX + structureSizeZ;
                        maxZ = minZ - structureSizeX;
                    }
                    default -> {
                        minX = structureSizeX < 0.0D ? structurePosX + 1.0D : structurePosX;
                        minZ = structureSizeZ < 0.0D ? structurePosZ + 1.0D : structurePosZ;
                        maxX = minX + structureSizeX;
                        maxZ = minZ + structureSizeZ;
                    }
                }

                if (pBlockEntity.getMode() == StructureMode.SAVE || pBlockEntity.getShowBoundingBox()) {
                    return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }

        return null;
    }

    public static boolean isKeyDown(Minecraft minecraft, int keyValue) {
        long window = minecraft.getWindow().getWindow();
        return InputConstants.isKeyDown(window, keyValue);
    }
}

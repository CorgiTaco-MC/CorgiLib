package corgitaco.corgilib.mixin;

import corgitaco.corgilib.client.StructureBoxEditor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureBlock.class)
public class MixinStructureBlock {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void bindBox(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.GOLDEN_AXE)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StructureBlockEntity structureBlockEntity) {
                if (level.isClientSide()) {
                    StructureBoxEditor.structureBox = StructureBoxEditor.getStructureWorldBox(structureBlockEntity);
                    StructureBoxEditor.structureBlockPos = structureBlockEntity.getBlockPos();
                    StructureBoxEditor.structureOffset = structureBlockEntity.getStructurePos();
                    player.displayClientMessage(Component.literal("Editing structure block."), true);
                }
                cir.setReturnValue(InteractionResult.PASS);
            }
        }

    }
}
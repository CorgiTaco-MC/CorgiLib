package corgitaco.corgilib.server.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.math.Transformation;
import corgitaco.corgilib.platform.ModPlatform;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class PlaceAllCommand {


    public static void register(LiteralArgumentBuilder<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        LiteralArgumentBuilder<CommandSourceStack> placeAll = LiteralArgumentBuilder.<CommandSourceStack>literal("place_all").requires(commandSourceStack -> commandSourceStack.hasPermission(3) && commandSourceStack.getServer().isSingleplayer());

        placeAll.then(LiteralArgumentBuilder.<CommandSourceStack>literal("templates").then(Commands.argument("mod_id", StringArgumentType.word()).suggests((context1, builder) -> SharedSuggestionProvider.suggest(ModPlatform.PLATFORM.getModIDS(), builder)).then(Commands.argument("block", BlockStateArgument.block(commandBuildContext)).then(Commands.argument("depth", IntegerArgumentType.integer()).executes(context -> {
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            dumpTemplates(context.getSource().getPosition(), context.getSource().getLevel(), StringArgumentType.getString(context, "mod_id"), blockInput.getState(), IntegerArgumentType.getInteger(context, "depth"));
            return 1;
        })))));

        placeAll.then(LiteralArgumentBuilder.<CommandSourceStack>literal("features").then(Commands.argument("mod_id", StringArgumentType.word()).suggests((context1, builder) -> SharedSuggestionProvider.suggest(ModPlatform.PLATFORM.getModIDS(), builder)).then(Commands.argument("block", BlockStateArgument.block(commandBuildContext)).then(Commands.argument("depth", IntegerArgumentType.integer()).executes(context -> {
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            dumpConfiguredFeatures(context.getSource().getPosition(), context.getSource().getLevel(), StringArgumentType.getString(context, "mod_id"), blockInput.getState(), IntegerArgumentType.getInteger(context, "depth"));
            return 1;
        })))));

        placeAll.then(LiteralArgumentBuilder.<CommandSourceStack>literal("structures").then(Commands.argument("mod_id", StringArgumentType.word()).suggests((context1, builder) -> SharedSuggestionProvider.suggest(ModPlatform.PLATFORM.getModIDS(), builder)).then(Commands.argument("block", BlockStateArgument.block(commandBuildContext)).then(Commands.argument("depth", IntegerArgumentType.integer()).executes(context -> {
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            dumpStructures(context.getSource().getPosition(), context.getSource().getLevel(), StringArgumentType.getString(context, "mod_id"), blockInput.getState(), IntegerArgumentType.getInteger(context, "depth"));
            return 1;
        })))));

        dispatcher.then(placeAll);
    }

    private static void dumpTemplates(Vec3 position, ServerLevel serverLevel, String modId, BlockState state, int floorDepth) {
        StructureTemplateManager structureManager = serverLevel.getStructureManager();
        List<ResourceLocation> list = structureManager.listTemplates().filter(location -> location.getNamespace().equalsIgnoreCase(modId)).sorted().toList();
        int size = list.size();
        int rowsAndCols = (int) (Math.floor(Math.sqrt(size) / 2D));
        generateObject(position, serverLevel, rowsAndCols, 48, state, floorDepth, (idx, pos) -> {
            ResourceLocation templateLocation = list.get(idx);
            StructureTemplate structureTemplate = structureManager.get(templateLocation).get();
            structureTemplate.placeInWorld(serverLevel, pos, pos, new StructurePlaceSettings(), serverLevel.random, 2);

            Vec3i structureTemplateSize = structureTemplate.getSize();
            generateText(serverLevel, Vec3.atCenterOf(pos.offset(structureTemplateSize.getX() / 2, structureTemplateSize.getY() + 3, structureTemplateSize.getZ() / 2)), templateLocation.toString());
        });
    }

    public static void dumpConfiguredFeatures(Vec3 position, ServerLevel serverLevel, String modId, BlockState state, int floorDepth) {
        List<Holder.Reference<ConfiguredFeature<?, ?>>> list = serverLevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).get().holders().filter(reference -> reference.key().location().getNamespace().equalsIgnoreCase(modId)).sorted(Comparator.comparing(holder -> holder.key().location())).toList();
        int size = list.size();
        int rowsAndCols = (int) (Math.floor(Math.sqrt(size) / 2D));
        generateObject(position, serverLevel, rowsAndCols, 32, state, floorDepth, (idx, offset) -> {
            Holder.Reference<ConfiguredFeature<?, ?>> configuredFeatureReference = list.get(idx);
            String textComponent = "";

            if (!configuredFeatureReference.value().place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.random, offset)) {
                textComponent = "Failed: ";
            }

            generateText(serverLevel, Vec3.atCenterOf(offset.above(32)), textComponent + configuredFeatureReference.key().location().toString());
        });
    }

    public static void dumpStructures(Vec3 position, ServerLevel serverLevel, String modId, BlockState state, int floorDepth) {
        List<Holder.Reference<Structure>> list = serverLevel.registryAccess().registry(Registries.STRUCTURE).get().holders().filter(reference -> reference.key().location().getNamespace().equalsIgnoreCase(modId)).sorted(Comparator.comparing(holder -> holder.key().location())).toList();
        int size = list.size();
        int rowsAndCols = (int) (Math.floor(Math.sqrt(size) / 2D));
        generateObject(position, serverLevel, rowsAndCols, 512, state, floorDepth, (idx, offset) -> {
            Holder.Reference<Structure> structureReference = list.get(idx);
            structureReference.unwrap().right().ifPresent(structure -> {
                ChunkGenerator generator = serverLevel.getChunkSource().getGenerator();
                StructureStart generatedStart = structure.generate(serverLevel.registryAccess(), generator, generator.getBiomeSource(), serverLevel.getChunkSource().randomState(), serverLevel.getStructureManager(), serverLevel.getSeed(), new ChunkPos(offset), 0, serverLevel, biomeHolder -> true);

                BoundingBox boundingBox = generatedStart.getBoundingBox();
                ChunkPos start = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
                ChunkPos end = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));

                ChunkPos.rangeClosed(start, end).forEach((chunkPos) -> generatedStart.placeInChunk(serverLevel, serverLevel.structureManager(), generator, serverLevel.getRandom(), new BoundingBox(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight(), chunkPos.getMaxBlockZ()), chunkPos));
            });
        });
    }

    public static void generateObject(Vec3 position, ServerLevel serverLevel, int rowsAndCols, int size, BlockState floorBlock, int floorDepth, BiConsumer<Integer, BlockPos> consumer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int templateIDX = 0;

        for (int x = -rowsAndCols; x <= rowsAndCols; x++) {
            for (int z = -rowsAndCols; z <= rowsAndCols; z++) {
                for (int xFill = 0; xFill <= size; xFill++) {
                    for (int zFill = 0; zFill <= size; zFill++) {

                        for (int y = 0; y < floorDepth; y++) {
                            mutableBlockPos.set((int) position.x() + (x * size) + xFill, (int) position.y - y, (int) position.z() + (z * size) + zFill);
                            serverLevel.setBlock(mutableBlockPos, floorBlock, 2, 0);
                        }
                    }
                }
                mutableBlockPos.set((int) position.x + (x * size) + 16, position.y + 1, (int) position.z + (z * size) + 16);
                consumer.accept(templateIDX, mutableBlockPos);

                templateIDX++;
            }
        }
    }

    private static void generateText(ServerLevel serverLevel, Vec3 spawnPos, String displayText) {
        Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel);
        textDisplay.setPos(spawnPos);

        textDisplay.setText(Component.literal(displayText));

        Matrix4f matrix = new Matrix4f();
        matrix.scale(5);
        textDisplay.setTransformation(new Transformation(matrix));

        textDisplay.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        serverLevel.addFreshEntity(textDisplay);
    }
}

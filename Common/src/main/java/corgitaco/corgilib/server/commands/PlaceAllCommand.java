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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
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
import java.util.Optional;

public class PlaceAllCommand {


    public static void register(LiteralArgumentBuilder<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        LiteralArgumentBuilder<CommandSourceStack> placeAll = LiteralArgumentBuilder.<CommandSourceStack>literal("place_all").requires(commandSourceStack -> commandSourceStack.permissions().hasPermission(Permissions.COMMANDS_OWNER) && commandSourceStack.getServer().isSingleplayer());

        placeAll.then(LiteralArgumentBuilder.<CommandSourceStack>literal("templates").then(Commands.argument("mod_id", StringArgumentType.word()).suggests((context1, builder) -> SharedSuggestionProvider.suggest(ModPlatform.PLATFORM.getModIDS(), builder)).then(Commands.argument("block", BlockStateArgument.block(commandBuildContext)).then(Commands.argument("depth", IntegerArgumentType.integer(0)).executes(context -> {
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            dumpTemplates(context.getSource().getPosition(), context.getSource().getLevel(), StringArgumentType.getString(context, "mod_id"), blockInput.getState(), IntegerArgumentType.getInteger(context, "depth"));
            return 1;
        })))));

        placeAll.then(LiteralArgumentBuilder.<CommandSourceStack>literal("features").then(Commands.argument("mod_id", StringArgumentType.word()).suggests((context1, builder) -> SharedSuggestionProvider.suggest(ModPlatform.PLATFORM.getModIDS(), builder)).then(Commands.argument("block", BlockStateArgument.block(commandBuildContext)).then(Commands.argument("depth", IntegerArgumentType.integer(0)).executes(context -> {
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            dumpConfiguredFeatures(context.getSource().getPosition(), context.getSource().getLevel(), StringArgumentType.getString(context, "mod_id"), blockInput.getState(), IntegerArgumentType.getInteger(context, "depth"));
            return 1;
        })))));

        placeAll.then(LiteralArgumentBuilder.<CommandSourceStack>literal("structures").then(Commands.argument("mod_id", StringArgumentType.word()).suggests((context1, builder) -> SharedSuggestionProvider.suggest(ModPlatform.PLATFORM.getModIDS(), builder)).then(Commands.argument("block", BlockStateArgument.block(commandBuildContext)).then(Commands.argument("depth", IntegerArgumentType.integer(0)).executes(context -> {
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            dumpStructures(context.getSource().getPosition(), context.getSource().getLevel(), StringArgumentType.getString(context, "mod_id"), blockInput.getState(), IntegerArgumentType.getInteger(context, "depth"));
            return 1;
        })))));

        dispatcher.then(placeAll);
    }

    private static void dumpTemplates(Vec3 position, ServerLevel serverLevel, String modId, BlockState state, int floorDepth) {
        StructureTemplateManager structureManager = serverLevel.getStructureManager();
        List<Identifier> list = structureManager.listTemplates().filter(location -> location.getNamespace().equalsIgnoreCase(modId)).sorted().toList();
        generateObjects(position, serverLevel, list.size(), 48, state, floorDepth, (idx, pos) -> {
            Identifier templateLocation = list.get(idx);
            Optional<StructureTemplate> template = structureManager.get(templateLocation);
            if (template.isEmpty()) {
                generateText(serverLevel, Vec3.atCenterOf(pos.above(3)), "Failed: " + templateLocation);
                return;
            }
            StructureTemplate structureTemplate = template.get();
            boolean placed = structureTemplate.placeInWorld(serverLevel, pos, pos, new StructurePlaceSettings(), serverLevel.getRandom(), 2);

            Vec3i structureTemplateSize = structureTemplate.getSize();
            generateText(serverLevel, Vec3.atCenterOf(pos.offset(structureTemplateSize.getX() / 2, structureTemplateSize.getY() + 3, structureTemplateSize.getZ() / 2)), (placed ? "" : "Failed: ") + templateLocation);
        });
    }

    public static void dumpConfiguredFeatures(Vec3 position, ServerLevel serverLevel, String modId, BlockState state, int floorDepth) {
        List<Holder.Reference<ConfiguredFeature<?, ?>>> list = serverLevel.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).listElements().filter(reference -> reference.key().identifier().getNamespace().equalsIgnoreCase(modId)).sorted(Comparator.comparing(holder -> holder.key().identifier())).toList();
        generateObjects(position, serverLevel, list.size(), 32, state, floorDepth, (idx, offset) -> {
            Holder.Reference<ConfiguredFeature<?, ?>> configuredFeatureReference = list.get(idx);
            String textComponent = "";

            if (!configuredFeatureReference.value().place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), offset)) {
                textComponent = "Failed: ";
            }

            generateText(serverLevel, Vec3.atCenterOf(offset.above(32)), textComponent + configuredFeatureReference.key().identifier().toString());
        });
    }

    public static void dumpStructures(Vec3 position, ServerLevel serverLevel, String modId, BlockState state, int floorDepth) {
        List<Holder.Reference<Structure>> list = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElements().filter(reference -> reference.key().identifier().getNamespace().equalsIgnoreCase(modId)).sorted(Comparator.comparing(holder -> holder.key().identifier())).toList();
        generateObjects(position, serverLevel, list.size(), 512, state, floorDepth, (idx, offset) -> {
            Holder.Reference<Structure> structureReference = list.get(idx);
            Structure structure = structureReference.value();
            ChunkGenerator generator = serverLevel.getChunkSource().getGenerator();
            StructureStart generatedStart = structure.generate(structureReference, serverLevel.dimension(), serverLevel.registryAccess(), generator, generator.getBiomeSource(), serverLevel.getChunkSource().randomState(), serverLevel.getStructureManager(), serverLevel.getSeed(), ChunkPos.containing(offset), 0, serverLevel, biomeHolder -> true);
            if (!generatedStart.isValid()) {
                generateText(serverLevel, Vec3.atCenterOf(offset.above(32)), "Failed: " + structureReference.key().identifier());
                return;
            }

            BoundingBox boundingBox = generatedStart.getBoundingBox();
            ChunkPos start = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
            ChunkPos end = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));

            ChunkPos.rangeClosed(start, end).forEach((chunkPos) -> generatedStart.placeInChunk(serverLevel, serverLevel.structureManager(), generator, serverLevel.getRandom(), new BoundingBox(chunkPos.getMinBlockX(), serverLevel.getMinY(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxY() - 1, chunkPos.getMaxBlockZ()), chunkPos));
            generateText(serverLevel, Vec3.atCenterOf(offset.above(32)), structureReference.key().identifier().toString());
        });
    }

    public static void generateObject(Vec3 position, ServerLevel serverLevel, int rowsAndCols, int size, BlockState floorBlock, int floorDepth, BiConsumer<Integer, BlockPos> consumer) {
        if (rowsAndCols < 0) {
            throw new IllegalArgumentException("Grid radius must not be negative");
        }
        int width = Math.addExact(Math.multiplyExact(rowsAndCols, 2), 1);
        generateObjects(position, serverLevel, Math.multiplyExact(width, width), size, floorBlock, floorDepth, consumer);
    }

    public static void generateObjects(Vec3 position, ServerLevel serverLevel, int count, int size, BlockState floorBlock, int floorDepth, BiConsumer<Integer, BlockPos> consumer) {
        if (floorDepth < 0) {
            throw new IllegalArgumentException("Floor depth must not be negative");
        }
        int floorY = BlockPos.containing(position).getY();
        int depth = Math.min(floorDepth, Math.max(0, floorY - serverLevel.getMinY() + 1));
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        forEachGridPosition(position, count, size, (index, objectPosition) -> {
            BlockPos floorOrigin = objectPosition.offset(-16, -1, -16);
            for (int x = 0; x <= size; x++) {
                for (int z = 0; z <= size; z++) {
                    for (int y = 0; y < depth; y++) {
                        mutableBlockPos.set(floorOrigin.getX() + x, floorY - y, floorOrigin.getZ() + z);
                        serverLevel.setBlock(mutableBlockPos, floorBlock, 2, 0);
                    }
                }
            }
            consumer.accept(index, objectPosition);
        });
    }

    static void forEachGridPosition(Vec3 position, int count, int spacing, BiConsumer<Integer, BlockPos> consumer) {
        if (count < 0 || spacing <= 0) {
            throw new IllegalArgumentException("Object count must be nonnegative and grid spacing must be positive");
        }
        if (count == 0) {
            return;
        }
        int width = (int) Math.ceil(Math.sqrt(count));
        BlockPos origin = BlockPos.containing(position);
        for (int index = 0; index < count; index++) {
            int x = index / width - width / 2;
            int z = index % width - width / 2;
            consumer.accept(index, origin.offset(x * spacing + 16, 1, z * spacing + 16));
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

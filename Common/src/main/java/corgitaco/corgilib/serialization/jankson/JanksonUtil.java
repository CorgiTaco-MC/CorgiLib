package corgitaco.corgilib.serialization.jankson;

import blue.endless.jankson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import corgitaco.corgilib.CorgiLib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

public class JanksonUtil {

    public static RuntimeException thrown = null;

    public static final String HEADER_OPEN = """
            /*
            This file uses the ".json5" file extension which allows for comments like this in a json file!
            Your text editor may show this file with invalid/no syntax, if so, we recommend you download:
                    
            VSCode: https://code.visualstudio.com/
            JSON5 plugin(for VSCode): https://marketplace.visualstudio.com/items?itemName=mrmlnc.vscode-json5
                        
            to make editing this file much easier.""";

    public static final String HEADER_CLOSED = HEADER_OPEN + "\n*/";

    public static final Jankson JANKSON = Jankson.builder().allowBareRootObject().build();
    public static final Supplier<JsonGrammar.Builder> JSON_GRAMMAR_BUILDER = () -> new JsonGrammar.Builder().withComments(true).bareSpecialNumerics(true).printCommas(true);

    public static final JsonGrammar JSON_GRAMMAR = JSON_GRAMMAR_BUILDER.get().build();


    public static JsonElement addCommentsAndAlphabeticallySortRecursively(Map<String, String> comments, JsonElement element, String parentKey, boolean alphabeticallySorted) {
        if (element instanceof JsonArray jsonArray) {
            return handleArray(comments, alphabeticallySorted, parentKey, jsonArray);
        } else if (element instanceof JsonObject object) {
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String objectKey = entry.getKey();
                String commentsKey = parentKey + objectKey;

                String comment = object.getComment(entry.getKey());
                if (comments.containsKey(commentsKey) && comment == null) {
                    String commentToAdd = comments.get(commentsKey);
                    object.setComment(objectKey, commentToAdd);
                    comment = commentToAdd;
                }

                JsonElement value = entry.getValue();
                if (value instanceof JsonArray array) {
                    JsonArray jsonElements = handleArray(comments, alphabeticallySorted, commentsKey, array);
                    object.put(objectKey, jsonElements, comment);
                }

                if (value instanceof JsonObject nestedObject) {
                    object.put(objectKey, addCommentsAndAlphabeticallySortRecursively(comments, nestedObject, commentsKey + ".", alphabeticallySorted), comment);
                }
            }

            if (alphabeticallySorted) {
                JsonObject alphabeticallySortedJsonObject = new JsonObject();
                TreeMap<String, JsonElement> map = new TreeMap<>(String::compareTo);
                map.putAll(object);
                alphabeticallySortedJsonObject.putAll(map);
                alphabeticallySortedJsonObject.forEach((key, entry) -> alphabeticallySortedJsonObject.setComment(key, object.getComment(key)));

                return alphabeticallySortedJsonObject;
            }
            return object;
        } else if (element instanceof JsonPrimitive) {
            return element;
        }

        throw new IllegalArgumentException("Unknown Jankson JsonElementType");
    }

    private static JsonArray handleArray(Map<String, String> comments, boolean alphabeticallySorted, String objectKey, JsonArray array) {
        JsonArray sortedJsonElements = new JsonArray();
        for (JsonElement element1 : array) {
            if (element1 instanceof JsonObject nestedObject) {
                sortedJsonElements.add(addCommentsAndAlphabeticallySortRecursively(comments, nestedObject, objectKey + ".", alphabeticallySorted));
            } else if (element1 instanceof JsonArray array1) {
                JsonArrayOfArrays arrayOfArrays = new JsonArrayOfArrays();
                arrayOfArrays.addAll(handleArray(comments, alphabeticallySorted, objectKey, array1));
                sortedJsonElements.add(arrayOfArrays);
            } else {
                sortedJsonElements.add(element1);
            }
        }
        return sortedJsonElements;
    }

    public static <T> void createConfig(Path path, Codec<T> codec, String header, Map<String, String> comments, DynamicOps<JsonElement> ops, T from) {
        DataResult<JsonElement> dataResult = codec.encodeStart(ops, from);
        Optional<DataResult.Error<JsonElement>> error = dataResult.error();
        if (error.isPresent()) {
            throw new IllegalArgumentException(String.format("Jankson file creation for \"%s\" failed due to the following error(s): %s", path.toString(), error.get().message()));
        }

        JsonElement jsonElement = dataResult.result().orElseThrow();

        if (jsonElement instanceof JsonObject jsonObject) {
            jsonElement = addCommentsAndAlphabeticallySortRecursively(comments, jsonObject, "", true);
        }
        try {
            Files.createDirectories(path.getParent());
            String output = header + "\n" + jsonElement.toJson(JSON_GRAMMAR);
            Files.write(path, output.getBytes());
        } catch (IOException e) {
            CorgiLib.LOGGER.error(e.toString());
        }
    }

    public static <T> T readConfig(Path path, Codec<T> codec, DynamicOps<JsonElement> ops) {
        JsonElement load;

        try {
            load = JANKSON.loadElement(configStringFromBytes(path).strip());

            DataResult<Pair<T, JsonElement>> decode = codec.decode(ops, load);
            Optional<DataResult.Error<Pair<T, JsonElement>>> error = decode.error();
            if (error.isPresent()) {
                IllegalArgumentException illegalArgumentException = new IllegalArgumentException(String.format("Jankson file reading for \"%s\" failed due to the following error(s): %s", path, error.get().message()));
                thrown = illegalArgumentException;
                throw illegalArgumentException;
            }
            return decode.result().orElseThrow().getFirst();
        } catch (Exception errorMsg) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException(String.format("Jankson file reading for \"%s\" failed due to the following error(s): %s", path, errorMsg.getMessage()));
            thrown = illegalArgumentException;
            throw illegalArgumentException;
        }
    }

    private static String configStringFromBytes(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return String.format("Unable to read file bytes \"%s\" due to error(s):\n%s", path, e);
        }
    }
}

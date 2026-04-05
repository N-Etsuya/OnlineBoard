package onlineboard.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken; 
import onlineboard.models.Post;
import onlineboard.models.Thread;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class JsonConverter {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                    return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME);
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME));
                }
            })
            .create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static Map<String, Object> jsonToMap(String json) {
        return fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
    }

    public static List<Thread> jsonToThreadList(String json) {
        Type listType = new com.google.gson.reflect.TypeToken<List<Thread>>(){}.getType();
        return fromJson(json, listType);
    }

    public static List<Post> jsonToPostList(String json) {
        Type listType = new com.google.gson.reflect.TypeToken<List<Post>>(){}.getType();
        return fromJson(json, listType);
    }
}
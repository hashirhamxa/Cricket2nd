package livecricket.livecrickettv.cricketstreaming.util;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import livecricket.livecrickettv.cricketstreaming.data.model.Score;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<Score> fromScoreString(String value) {
        Type listType = new TypeToken<List<Score>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromScoreList(List<Score> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> fromStringListString(String value) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromStringList(List<String> list) {
        return gson.toJson(list);
    }
}

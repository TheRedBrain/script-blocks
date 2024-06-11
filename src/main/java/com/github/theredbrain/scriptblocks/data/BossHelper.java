package com.github.theredbrain.scriptblocks.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.Reader;
import java.lang.reflect.Type;

public class BossHelper {

    private static Type registeredBossesFileFormat = new TypeToken<Boss>() {}.getType();

    public static Boss decode(Reader reader) {
        var gson = new Gson();
        Boss boss = gson.fromJson(reader, registeredBossesFileFormat);
        return boss;
    }

    public static Boss decode(JsonReader json) {
        var gson = new Gson();
        Boss boss = gson.fromJson(json, registeredBossesFileFormat);
        return boss;
    }

    public static String encode(Boss boss) {
        var gson = new Gson();
        return gson.toJson(boss);
    }
}

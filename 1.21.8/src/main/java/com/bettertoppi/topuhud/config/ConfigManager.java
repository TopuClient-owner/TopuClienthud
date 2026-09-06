package com.bettertoppi.topuhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Field;

public final class ConfigManager {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final TopuHudConfig CONFIG=new TopuHudConfig();
    private static Path path(){return FabricLoader.getInstance().getConfigDir().resolve("topuhud.json");}
    public static TopuHudConfig get(){return CONFIG;}
    public static void load(){
        try{
            Path p=path();
            if(!Files.exists(p)){save();return;}
            JsonElement root=GSON.fromJson(Files.readString(p),JsonElement.class);
            if(!(root instanceof JsonObject obj))return;
            for(Field f:TopuHudConfig.class.getFields()){
                if(!obj.has(f.getName())||obj.get(f.getName()).isJsonNull())continue;
                try{
                    JsonElement v=obj.get(f.getName());
                    if(f.getType()==boolean.class&&v.isJsonPrimitive())f.setBoolean(CONFIG,v.getAsBoolean());
                    else if(f.getType()==int.class&&v.isJsonPrimitive())f.setInt(CONFIG,v.getAsInt());
                }catch(Exception ignored){}
            }
        }catch(Exception ignored){}
    }
    public static void save(){
        try{Files.createDirectories(path().getParent());Files.writeString(path(),GSON.toJson(CONFIG));}
        catch(Exception ignored){}
    }
}

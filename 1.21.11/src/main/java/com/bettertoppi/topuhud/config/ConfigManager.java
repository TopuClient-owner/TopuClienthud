package com.bettertoppi.topuhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final TopuHudConfig CONFIG=new TopuHudConfig();
    private static Path path(){return FabricLoader.getInstance().getConfigDir().resolve("topuhud.json");}
    public static TopuHudConfig get(){return CONFIG;}
    public static void load(){
        try{
            Path p=path();
            if(!Files.exists(p)){save();return;}
            TopuHudConfig c=GSON.fromJson(Files.readString(p),TopuHudConfig.class);
            if(c!=null)copy(c,CONFIG);
        }catch(Exception ignored){}
    }
    public static void save(){
        try{Files.createDirectories(path().getParent());Files.writeString(path(),GSON.toJson(CONFIG));}
        catch(Exception ignored){}
    }
    private static void copy(TopuHudConfig a,TopuHudConfig b){
        try{
            for(var f:TopuHudConfig.class.getFields()){
                if(f.getType()==boolean.class||f.getType()==int.class)f.set(b,f.get(a));
            }
        }catch(Exception ignored){}
    }
}

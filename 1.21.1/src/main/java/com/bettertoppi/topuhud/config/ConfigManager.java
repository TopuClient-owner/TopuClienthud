package com.bettertoppi.topuhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final TopuHudConfig CONFIG =
            new TopuHudConfig();

    private ConfigManager() {
    }

    private static Path path() {

        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve("topuhud.json");
    }

    public static TopuHudConfig get() {

        return CONFIG;
    }

    public static void load() {

        try {

            Path p = path();

            if (!Files.exists(p)) {

                save();
                return;
            }

            TopuHudConfig loaded =
                    GSON.fromJson(
                            Files.readString(p),
                            TopuHudConfig.class
                    );

            if (loaded != null) {

                copy(
                        loaded,
                        CONFIG
                );
            }

        } catch (Exception ignored) {
        }
    }

    public static void save() {

        try {

            Path p = path();

            Files.createDirectories(
                    p.getParent()
            );

            Files.writeString(
                    p,
                    GSON.toJson(CONFIG)
            );

        } catch (Exception ignored) {
        }
    }

    private static void copy(
            TopuHudConfig source,
            TopuHudConfig target
    ) {

        try {

            for (var field :
                    TopuHudConfig.class.getFields()) {

                if (field.getType() == boolean.class ||
                        field.getType() == int.class) {

                    field.set(
                            target,
                            field.get(source)
                    );
                }
            }

        } catch (Exception ignored) {
        }
    }
}

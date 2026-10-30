package com.jeff.listmymods.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collection;

public class ListMyModsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        Collection<ModContainer> list = FabricLoader.getInstance().getAllMods();
        MutableComponent cFabricName = Component.literal("Fabric API").withStyle(ChatFormatting.GOLD);
        MutableComponent cFabricLink = Component.literal("https://github.com/FabricMC/fabric").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.UNDERLINE)
                .withStyle(style -> {
                    return style.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/FabricMC/fabric")));
                });

        String fabricName = "Fabric API";
        String fabricLink = "https://github.com/FabricMC/fabric";

        final String[] currentSuggestions = new String[]{"normal", "file", "md file", "extensive"};

        SuggestionProvider<FabricClientCommandSource> suggestions = (context, builder) -> {
            String lowercase = builder.getRemainingLowerCase();
            for (String s : currentSuggestions) {
                if (s.toLowerCase().startsWith(lowercase)) {
                    builder.suggest(s);
                }
            }
            return builder.buildFuture();
        };
        ClientCommandRegistrationCallback.EVENT.register((dispatcher2, registryAccess) -> dispatcher2.register(ClientCommands.literal("listmymods")
                .then(ClientCommands.argument("format", StringArgumentType.greedyString())
                        .suggests(suggestions)
                        .executes((context) -> {
                            String format = StringArgumentType.getString(context, "format");

                            switch (format) {
                                case "normal", "" -> {
                                    context.getSource().sendFeedback(cFabricName.append(": ").append(cFabricLink));
                                    for (ModContainer mc : list) {
                                        String name = mc.getMetadata().getName();
                                        MutableComponent nameC = Component.literal(name).withStyle(ChatFormatting.GOLD);
                                        boolean isURLPresent = mc.getMetadata().getContact().get("sources").isPresent();
                                        String url = isURLPresent ? mc.getMetadata().getContact().get("sources").get() : "";
                                        MutableComponent urlC = Component.literal(url).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.UNDERLINE);
                                        if (!url.equals("https://github.com/FabricMC/fabric") && isURLPresent) {
                                            context.getSource().sendFeedback(nameC.append(": ").append(urlC)
                                                    .withStyle(style -> {
                                                        return style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)));
                                                    }));
                                        }
                                    }
                                }
                                /*case "md", "markdown" -> {
                                    context.getSource().sendFeedback(Component.literal("[").append(cFabricName.withStyle(ChatFormatting.RESET).append("]").append("(").append(cFabricLink.withStyle(ChatFormatting.RESET)).append(")")));
                                    for (ModContainer mc : list) {
                                        String name = mc.getMetadata().getName();
                                        MutableComponent nameC = Component.literal(name);
                                        boolean isURLPresent = mc.getMetadata().getContact().get("sources").isPresent();
                                        String url = isURLPresent ? mc.getMetadata().getContact().get("sources").get() : "";
                                        MutableComponent urlC = Component.literal(url);
                                        if (!url.equals("https://github.com/FabricMC/fabric") && isURLPresent) {
                                            context.getSource().sendFeedback(Component.literal("[").append(nameC.append("]").append("(").append(urlC).append(")")));
                                        }
                                    }
                                }*/
                                case "file" -> {

                                    File dir = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath() + "/mymodlist");
                                    File file = new File(dir, "my-mods.txt");
                                    try {
                                        Files.createDirectories(dir.toPath());
                                        file.createNewFile();
                                        file.setWritable(true);
                                        try (FileWriter writer = new FileWriter(file)) {
                                            writer.write(fabricName + ": " + fabricLink + "\n");
                                            for (ModContainer mc : list) {
                                                String name = mc.getMetadata().getName();
                                                boolean isURLPresent = mc.getMetadata().getContact().get("sources").isPresent();
                                                String url = isURLPresent ? mc.getMetadata().getContact().get("sources").get() : "";
                                                if (!url.equals("https://github.com/FabricMC/fabric") && isURLPresent) {
                                                    try {
                                                        writer.write(name + ": " + url + "\n");
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                            }
                                            Blaze3D.openPath(file.toPath());
                                        }
                                        context.getSource().sendFeedback(Component.literal("File saved to " + file.getAbsolutePath()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                case "md file", "md_file" -> {
                                    File dir = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath() + "/mymodlist");
                                    File file = new File(dir, "my-mods.md");
                                    try {
                                        Files.createDirectories(dir.toPath());
                                        file.createNewFile();
                                        file.setWritable(true);
                                        try (FileWriter writer = new FileWriter(file)) {
                                            writer.write("[" + fabricName + "]" + "(" + fabricLink + ")\n\n");
                                            for (ModContainer mc : list) {
                                                String name = mc.getMetadata().getName();
                                                boolean isURLPresent = mc.getMetadata().getContact().get("sources").isPresent();
                                                String url = isURLPresent ? mc.getMetadata().getContact().get("sources").get() : "";
                                                if (!url.equals("https://github.com/FabricMC/fabric") && isURLPresent) {
                                                    try {
                                                        writer.write("[" + name + "]" + "(" + url + ")\n\n");
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                            }
                                            Blaze3D.openPath(file.toPath());
                                        }
                                        context.getSource().sendFeedback(Component.literal("File saved to " + file.getAbsolutePath()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                case "extensive" -> {
                                    Component cFabricOwner = Component.literal("modmuss50");
                                    Component cFabricId = Component.literal("fabric-api").withStyle(ChatFormatting.GRAY);
                                    Component cFabricVer = Component.literal(FabricLoader.getInstance().getModContainer("fabric-api").get().getMetadata().getVersion().getFriendlyString()
                                            ).withStyle(ChatFormatting.DARK_GREEN);
                                    context.getSource().sendFeedback(cFabricName
                                                    .append(" (")
                                                    .append(cFabricId)
                                                    .append(") ")
                                                    .append(cFabricVer)
                                                    .append(" by ")
                                                    .append(cFabricOwner)
                                                    .append(" ")
                                                    .append(cFabricLink));
                                    for (ModContainer mc : list) {
                                        String name = mc.getMetadata().getName();
                                        String id = mc.getMetadata().getId();
                                        String version = mc.getMetadata().getVersion().getFriendlyString();
                                        Collection<Person> authors = mc.getMetadata().getAuthors();
                                        MutableComponent nameC = Component.literal(name).withStyle(ChatFormatting.GOLD);
                                        MutableComponent idC = Component.literal(id).withStyle(ChatFormatting.GRAY);
                                        MutableComponent ver = Component.literal(version).withStyle(ChatFormatting.GREEN);
                                        MutableComponent author = Component.literal("");
                                        for (Person person : authors) {
                                            author.append(person.getName() + ", ");
                                        }
                                        boolean isURLPresent = mc.getMetadata().getContact().get("sources").isPresent();
                                        String url = isURLPresent ? mc.getMetadata().getContact().get("sources").get() : "";
                                        MutableComponent urlC = Component.literal(url).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.UNDERLINE);
                                        if (!url.equals("https://github.com/FabricMC/fabric") && isURLPresent) {
                                            context.getSource().sendFeedback(nameC.append(" (")
                                                    .append(idC)
                                                            .append(") ")
                                                            .append(ver)
                                                            .append(" by ")
                                                            .append(author)
                                                    .append(urlC)
                                                    .withStyle(style -> {
                                                        return style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)));
                                                    }));
                                        }
                                    }
                                }
                                case null, default -> context.getSource().sendFeedback(Component.literal("Unknown input \"" + format + "\"").withStyle(ChatFormatting.RED));
                            }
                            return 1;
                        }))));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, c) -> dispatcher.register(
                ClientCommands.literal("listmymods")
                        .executes((context) -> {
                            context.getSource().sendFeedback(cFabricName.append(": ").append(cFabricLink));
                            for (ModContainer mc : list) {
                                String name = mc.getMetadata().getName();
                                MutableComponent nameC = Component.literal(name).withStyle(ChatFormatting.GOLD);
                                boolean isURLPresent = mc.getMetadata().getContact().get("sources").isPresent();
                                String url = isURLPresent ? mc.getMetadata().getContact().get("sources").get() : "";
                                MutableComponent urlC = Component.literal(url).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.UNDERLINE);
                                if (!url.equals("https://github.com/FabricMC/fabric") && isURLPresent) {
                                    context.getSource().sendFeedback(nameC.append(": ").append(urlC)
                                            .withStyle(style -> {
                                                return style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)));
                                            }));
                                }
                            }
                            return 1;
                        })
        ));
    }
}
package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrKind;
import com.cmcplugins.asmr.core.AsmrMode;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class AsmrCommands {
    private AsmrCommands() {}

    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSource> root = Commands.literal("asmr");
        root.then(Commands.literal("menu").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrException();
            AsmrNetwork.send(player, AsmrPacket.menu());
            return 1;
        }));
        root.then(play(false));
        root.then(Commands.literal("stop").executes(context ->
            stop(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()))));

        LiteralArgumentBuilder<CommandSource> target = Commands.literal("target");
        target.requires(source -> source.hasPermission(2));
        target.then(Commands.argument("players", EntityArgument.players())
            .then(play(true))
            .then(Commands.literal("stop").executes(context ->
                stop(context.getSource(), EntityArgument.getPlayers(context, "players")))));
        root.then(target);
        event.getDispatcher().register(root);
    }

    private static LiteralArgumentBuilder<CommandSource> play(boolean targeted) {
        LiteralArgumentBuilder<CommandSource> play = Commands.literal("play");
        for (AsmrKind kind : AsmrKind.values()) {
            LiteralArgumentBuilder<CommandSource> kindNode = Commands.literal(kind.name().toLowerCase(java.util.Locale.ROOT));
            for (AsmrMode mode : AsmrMode.values()) {
                kindNode.then(Commands.literal(mode.name().toLowerCase(java.util.Locale.ROOT)).executes(context -> {
                    Collection<ServerPlayerEntity> players = targeted
                        ? EntityArgument.getPlayers(context, "players")
                        : Collections.singleton(context.getSource().getPlayerOrException());
                    for (ServerPlayerEntity player : players) {
                        AsmrServer.start(player, kind, mode);
                        context.getSource().sendSuccess(new TranslationTextComponent("asmr.command.started",
                            player.getName(), new TranslationTextComponent("asmr.kind." + kind.name().toLowerCase(java.util.Locale.ROOT)),
                            new TranslationTextComponent("asmr.mode." + mode.name().toLowerCase(java.util.Locale.ROOT))), false);
                    }
                    return players.size();
                }));
            }
            play.then(kindNode);
        }
        return play;
    }

    private static int stop(CommandSource source, Collection<ServerPlayerEntity> players) {
        int stopped = 0;
        for (ServerPlayerEntity player : players) {
            if (AsmrServer.stop(player)) {
                stopped++;
                source.sendSuccess(new TranslationTextComponent("asmr.command.stopped", player.getName()), false);
            } else {
                source.sendFailure(new TranslationTextComponent("asmr.command.no_session", player.getName()));
            }
        }
        return stopped;
    }
}

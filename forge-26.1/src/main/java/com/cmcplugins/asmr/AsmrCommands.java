package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrKind;
import com.cmcplugins.asmr.core.AsmrMode;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class AsmrCommands {
    private AsmrCommands() {}

    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("asmr");
        root.then(Commands.literal("menu").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            AsmrNetwork.send(player, AsmrPacket.menu());
            return 1;
        }));
        root.then(play(false));
        root.then(Commands.literal("stop").executes(context ->
            stop(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()))));

        LiteralArgumentBuilder<CommandSourceStack> target = Commands.literal("target");
        target.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
        target.then(Commands.argument("players", EntityArgument.players())
            .then(play(true))
            .then(Commands.literal("stop").executes(context ->
                stop(context.getSource(), EntityArgument.getPlayers(context, "players")))));
        root.then(target);
        event.getDispatcher().register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> play(boolean targeted) {
        LiteralArgumentBuilder<CommandSourceStack> play = Commands.literal("play");
        for (AsmrKind kind : AsmrKind.values()) {
            LiteralArgumentBuilder<CommandSourceStack> kindNode = Commands.literal(kind.name().toLowerCase(Locale.ROOT));
            for (AsmrMode mode : AsmrMode.values()) {
                kindNode.then(Commands.literal(mode.name().toLowerCase(Locale.ROOT)).executes(context -> {
                    Collection<ServerPlayer> players = targeted
                        ? EntityArgument.getPlayers(context, "players")
                        : Collections.singleton(context.getSource().getPlayerOrException());
                    for (ServerPlayer player : players) {
                        AsmrServer.start(player, kind, mode);
                        context.getSource().sendSuccess(() -> Component.translatable("asmr.command.started",
                            player.getName(), Component.translatable("asmr.kind." + kind.name().toLowerCase(Locale.ROOT)),
                            Component.translatable("asmr.mode." + mode.name().toLowerCase(Locale.ROOT))), false);
                    }
                    return players.size();
                }));
            }
            play.then(kindNode);
        }
        return play;
    }

    private static int stop(CommandSourceStack source, Collection<ServerPlayer> players) {
        int stopped = 0;
        for (ServerPlayer player : players) {
            if (AsmrServer.stop(player)) {
                stopped++;
                source.sendSuccess(() -> Component.translatable("asmr.command.stopped", player.getName()), false);
            } else {
                source.sendFailure(Component.translatable("asmr.command.no_session", player.getName()));
            }
        }
        return stopped;
    }
}

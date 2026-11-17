package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrKind;
import com.cmcplugins.asmr.core.AsmrMode;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

public final class ClientAsmrCommands {
    private ClientAsmrCommands() {}

    public static void register(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("asmr");
        root.then(Commands.literal("menu").executes(context -> {
            ClientAsmr.openMenu();
            return 1;
        }));
        root.then(play());
        root.then(Commands.literal("stop").executes(context -> {
            boolean local = !ClientAsmr.serverPresent();
            boolean wasPlaying = ClientAsmr.kind() != null;
            ClientAsmr.stop();
            if (local) {
                if (wasPlaying) {
                    context.getSource().sendSuccess(() -> Component.translatable("asmr.command.stopped",
                        context.getSource().getDisplayName()), false);
                } else {
                    context.getSource().sendFailure(Component.translatable("asmr.command.no_session",
                        context.getSource().getDisplayName()));
                }
            }
            return wasPlaying ? 1 : 0;
        }));
        event.getDispatcher().register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> play() {
        LiteralArgumentBuilder<CommandSourceStack> play = Commands.literal("play");
        for (AsmrKind kind : AsmrKind.values()) {
            LiteralArgumentBuilder<CommandSourceStack> kindNode = Commands.literal(kind.name().toLowerCase(Locale.ROOT));
            for (AsmrMode mode : AsmrMode.values()) {
                kindNode.then(Commands.literal(mode.name().toLowerCase(Locale.ROOT)).executes(context -> {
                    boolean local = !ClientAsmr.serverPresent();
                    ClientAsmr.play(kind, mode);
                    if (local) {
                        context.getSource().sendSuccess(() -> Component.translatable("asmr.command.started",
                            context.getSource().getDisplayName(),
                            Component.translatable("asmr.kind." + kind.name().toLowerCase(Locale.ROOT)),
                            Component.translatable("asmr.mode." + mode.name().toLowerCase(Locale.ROOT))), false);
                    }
                    return 1;
                }));
            }
            play.then(kindNode);
        }
        return play;
    }
}

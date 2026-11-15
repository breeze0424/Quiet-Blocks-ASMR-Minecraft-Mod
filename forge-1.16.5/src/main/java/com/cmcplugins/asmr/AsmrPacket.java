package com.cmcplugins.asmr;

import com.cmcplugins.asmr.core.AsmrKind;
import com.cmcplugins.asmr.core.AsmrMode;
import net.minecraft.network.PacketBuffer;

public final class AsmrPacket {
    public final boolean playing;
    public final boolean openMenu;
    public final AsmrKind kind;
    public final AsmrMode mode;

    public AsmrPacket(boolean playing, AsmrKind kind, AsmrMode mode) {
        this(playing, false, kind, mode);
    }

    private AsmrPacket(boolean playing, boolean openMenu, AsmrKind kind, AsmrMode mode) {
        this.playing = playing;
        this.openMenu = openMenu;
        this.kind = kind;
        this.mode = mode;
    }

    public static AsmrPacket menu() {
        return new AsmrPacket(false, true, AsmrKind.TAP, AsmrMode.ALTERNATE);
    }

    public static void encode(AsmrPacket message, PacketBuffer buffer) {
        buffer.writeBoolean(message.openMenu);
        buffer.writeBoolean(message.playing);
        buffer.writeByte(message.kind.ordinal());
        buffer.writeByte(message.mode.ordinal());
    }

    public static AsmrPacket decode(PacketBuffer buffer) {
        boolean openMenu = buffer.readBoolean();
        boolean playing = buffer.readBoolean();
        int kind = buffer.readUnsignedByte();
        int mode = buffer.readUnsignedByte();
        AsmrKind[] kinds = AsmrKind.values();
        AsmrMode[] modes = AsmrMode.values();
        return new AsmrPacket(playing, openMenu, kinds[Math.min(kind, kinds.length - 1)], modes[Math.min(mode, modes.length - 1)]);
    }
}

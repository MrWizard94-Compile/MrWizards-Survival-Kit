package com.mrwizard94.survivalkit.client.gui;

import com.mrwizard94.survivalkit.logic.DoorAction;
import com.mrwizard94.survivalkit.network.DoorActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Simple Enter / Pack Up choice screen for the exterior tent door.
 */
public final class TentDoorScreen extends Screen {

    private final BlockPos doorPos;
    private final UUID tentId;

    /**
     * Documented member.
     *
     * @param doorPos controller position
     * @param tentId  tent UUID
     */
    public TentDoorScreen(@NotNull final BlockPos doorPos, @NotNull final UUID tentId) {
        super(Component.translatable("gui.mrwizardssurvivalkit.tent_door"));
        this.doorPos = doorPos;
        this.tentId = tentId;
    }

    @Override
    protected void init() {
        final int cx = this.width / 2;
        final int cy = this.height / 2;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.mrwizardssurvivalkit.enter"),
                        button -> send(DoorAction.ENTER))
                .bounds(cx - 100, cy - 24, 200, 20)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.mrwizardssurvivalkit.pack_up"),
                        button -> send(DoorAction.PACK_UP))
                .bounds(cx - 100, cy + 4, 200, 20)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.mrwizardssurvivalkit.cancel"),
                        button -> this.onClose())
                .bounds(cx - 100, cy + 32, 200, 20)
                .build());
    }

    private void send(final DoorAction action) {
        PacketDistributor.sendToServer(new DoorActionPayload(doorPos, tentId, action));
        this.onClose();
    }

    @Override
    public void render(
            @NotNull final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 48, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package com.blank038.servermarket.internal.gui;

import com.blank038.servermarket.internal.gui.context.GuiContext;

import java.util.UUID;

public interface IGui {

    GuiContext getContext();

    boolean isCooldown(UUID uuid);
}

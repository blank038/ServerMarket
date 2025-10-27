package com.blank038.servermarket.internal.gui.context;

import com.blank038.servermarket.api.handler.filter.FilterHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuiContext {
    private final String marketId;
    private int page = 1;
    private String sort = "default";
    private String type = "all";
    private FilterHandler filter;

    public GuiContext(String marketId) {
        this.marketId = marketId;
    }

    public static GuiContext normal(String market) {
        return new GuiContext(market);
    }
}

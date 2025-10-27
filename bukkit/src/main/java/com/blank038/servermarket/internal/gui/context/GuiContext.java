package com.blank038.servermarket.internal.gui.context;

import com.blank038.servermarket.api.handler.filter.FilterHandler;
import com.blank038.servermarket.api.handler.filter.impl.TypeFilterImpl;
import com.google.common.collect.Lists;
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
        this.initFilter();
    }

    private void initFilter() {
        if (this.filter == null) {
            this.filter = new FilterHandler();
        }
        if (this.filter.getTypeFilter() == null) {
            this.filter.setTypeFilter(new TypeFilterImpl(Lists.newArrayList(this.type)));
        } else {
            this.type = ((TypeFilterImpl) this.filter.getTypeFilter()).getTypes().get(0);
        }
    }

    public static GuiContext normal(String market) {
        return new GuiContext(market);
    }
}

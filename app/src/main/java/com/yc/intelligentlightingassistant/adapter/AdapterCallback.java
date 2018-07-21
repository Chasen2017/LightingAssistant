package com.yc.intelligentlightingassistant.adapter;

/**
 * RecyclerView更新方法的接口
 */

public interface AdapterCallback<Data> {
    void update(Data data, RecyclerAdapter.ViewHolder<Data> holder);

}

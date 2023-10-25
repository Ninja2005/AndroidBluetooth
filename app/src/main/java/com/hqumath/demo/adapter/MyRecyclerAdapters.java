package com.hqumath.demo.adapter;

import android.content.Context;

import com.hqumath.demo.R;
import com.hqumath.demo.base.BaseRecyclerAdapter;
import com.hqumath.demo.base.BaseRecyclerViewHolder;
import com.hqumath.demo.bean.ReposEntity;

import java.util.List;

public class MyRecyclerAdapters {

    //我的仓库
    public static class ReposRecyclerAdapter extends BaseRecyclerAdapter<ReposEntity> {
        public ReposRecyclerAdapter(Context context, List<ReposEntity> mData) {
            super(context, mData, R.layout.recycler_item_repos);
        }

        @Override
        public void convert(BaseRecyclerViewHolder holder, int position) {
            ReposEntity data = mData.get(position);
            holder.setText(R.id.tv_name, data.getName());
            holder.setText(R.id.tv_description, data.getDescription());
            holder.setText(R.id.tv_author, data.getOwner().getLogin());
        }
    }
}

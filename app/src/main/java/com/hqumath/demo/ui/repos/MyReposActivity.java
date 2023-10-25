package com.hqumath.demo.ui.repos;

import android.os.Bundle;
import android.view.View;

import com.hqumath.demo.R;
import com.hqumath.demo.adapter.MyRecyclerAdapters;
import com.hqumath.demo.base.BaseActivity;
import com.hqumath.demo.bean.ReposEntity;
import com.hqumath.demo.databinding.ActivityMyReposBinding;
import com.hqumath.demo.dialog.DialogUtil;
import com.hqumath.demo.utils.CommonUtil;

/**
 * ****************************************************************
 * 作    者: Created by gyd
 * 创建时间: 2023/10/25 10:09
 * 文件描述:
 * 注意事项:
 * ****************************************************************
 */
public class MyReposActivity extends BaseActivity implements MyReposPresenter.Contract {

    private ActivityMyReposBinding binding;
    private MyReposPresenter mPresenter;
    private MyRecyclerAdapters.ReposRecyclerAdapter recyclerAdapter;
    protected boolean hasRequested;//在onResume中判断是否已经请求过数据。用于懒加载

    @Override
    protected View initContentView(Bundle savedInstanceState) {
        binding = ActivityMyReposBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initListener() {
        binding.titleLayout.tvTitle.setText(R.string.my_repos);
        binding.titleLayout.ivBack.setOnClickListener(v -> finish());
        binding.refreshLayout.setOnRefreshListener(v -> mPresenter.getMyRepos(true));
        binding.refreshLayout.setOnLoadMoreListener(v -> mPresenter.getMyRepos(false));
    }

    @Override
    protected void initData() {
        mPresenter = new MyReposPresenter();
        mPresenter.attachView(this);

        recyclerAdapter = new MyRecyclerAdapters.ReposRecyclerAdapter(mContext, mPresenter.mData);
        recyclerAdapter.setOnItemClickListener((v, position) -> {
            ReposEntity data = mPresenter.mData.get(position);
            DialogUtil dialog = new DialogUtil(mContext);
            dialog.setTitle("提示");
            dialog.setMessage(data.getName());
            dialog.setOneConfirmBtn("确定", null);
            dialog.show();
        });
        binding.recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasRequested) {
            hasRequested = true;
            binding.refreshLayout.autoRefresh();//触发自动刷新
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
    }

    @Override
    public void onGetListSuccess(boolean isRefresh, boolean isNewDataEmpty) {
        recyclerAdapter.notifyDataSetChanged();
        if (isRefresh) {
            if (isNewDataEmpty) {
                binding.refreshLayout.finishRefreshWithNoMoreData();//上拉加载功能将显示没有更多数据
            } else {
                binding.refreshLayout.finishRefresh();
            }
        } else {
            if (isNewDataEmpty) {
                binding.refreshLayout.finishLoadMoreWithNoMoreData();//上拉加载功能将显示没有更多数据
            } else {
                binding.refreshLayout.finishLoadMore();
            }
        }
        binding.emptyLayout.llEmpty.setVisibility(mPresenter.mData.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onGetListError(String errorMsg, String code, boolean isRefresh) {
        CommonUtil.toast(errorMsg);
        if (isRefresh) {
            binding.refreshLayout.finishRefresh(false);//刷新失败，会影响到上次的更新时间
        } else {
            binding.refreshLayout.finishLoadMore(false);
        }
        binding.emptyLayout.llEmpty.setVisibility(mPresenter.mData.isEmpty() ? View.VISIBLE : View.GONE);
    }
}

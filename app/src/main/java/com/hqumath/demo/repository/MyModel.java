package com.hqumath.demo.repository;

import com.hqumath.demo.base.BaseModel;
import com.hqumath.demo.net.HttpListener;
import com.hqumath.demo.net.RetrofitClient;

/**
 * ****************************************************************
 * 文件名称: MyModel
 * 作    者: Created by gyd
 * 创建时间: 2019/1/21 15:12
 * 文件描述: 数据层，数据的获取、存储和处理 https://juejin.cn/post/6844903812658888712
 * 注意事项:
 * 1.提供的数据业务层直接可用
 * 2.相同的接口不要多次实现
 * 3.方便自动化测试
 * ****************************************************************
 */
public class MyModel extends BaseModel {

    public void getMyRepos(String userName, int pageSize, long pageIndex, HttpListener listener) {
        sendRequest(RetrofitClient.getInstance().getApiService().getMyRepos(userName, pageSize, pageIndex), new HttpListener() {
            @Override
            public void onSuccess(Object object) {
                listener.onSuccess(object);
            }

            @Override
            public void onError(String errorMsg, String code) {
                listener.onError(errorMsg, code);
            }
        });
    }
}

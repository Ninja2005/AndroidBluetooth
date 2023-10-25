package com.hqumath.demo.app;

import static com.hqumath.demo.app.Constant.TOKEN;

import com.hqumath.demo.utils.SPUtil;

import java.util.HashMap;

/**
 * ****************************************************************
 * 文件名称: AppNetConfig
 * 作    者: Created by gyd
 * 创建时间: 2019/1/22 14:30
 * 文件描述: 网络地址
 * 注意事项:
 * 版权声明:
 * ****************************************************************
 */
public class Constants {
    public static String baseUrl = "https://api.github.com/"; //API服务器

    //请求通用参数
    public static HashMap<String, String> getBaseMap() {
        String token = SPUtil.getInstance().getString(TOKEN);
        HashMap<String, String> map = new HashMap<>();
        map.put("token", token);
        return map;
    }

}

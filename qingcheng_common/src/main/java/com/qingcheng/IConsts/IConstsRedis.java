package com.qingcheng.IConsts;

import com.sun.org.apache.regexp.internal.RE;

/*缓存key设置的接口*/
public interface  IConstsRedis extends  IConsts{
    /*缓存前缀*/
    public final static  String  REDIS_PREFIX = "00000";


  /*  定义缓存的前缀 格式为：REDIS_PREFIX+xxx*/


   /* 导航树*/

    public final String   Category_tree_redis = REDIS_PREFIX+"tree";

    /*广告轮播图*/
    public final String   Category_ad_redis = REDIS_PREFIX+"ad";

    public final String   Sku_Prize = REDIS_PREFIX+"price";


    static String getRedisKey(String key){
        return Category_ad_redis+key;
    }




    static String getRedisKey(String key1,String key2,String key3){

        return new StringBuffer()
                .append(REDIS_PREFIX)
                .append(seperator_redis_key).append(key1)
                .append(seperator_redis_key).append(key2)
                .append(seperator_redis_key).append(key3).toString();
    }
}

package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.IConsts.IConstsRedis;
import com.qingcheng.dao.AdMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AdServiceImpl implements AdService {

    @Autowired
    private AdMapper adMapper;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 返回全部记录
     *
     * @return
     */
    public List<Ad> findAll() {
        return adMapper.selectAll();
    }

    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Ad> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Ad> ads = (Page<Ad>) adMapper.selectAll();
        return new PageResult<Ad>(ads.getTotal(), ads.getResult());
    }

    /**
     * 条件查询
     *
     * @param searchMap 查询条件
     * @return
     */
    public List<Ad> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Ad> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Ad> ads = (Page<Ad>) adMapper.selectByExample(example);
        return new PageResult<Ad>(ads.getTotal(), ads.getResult());
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @return
     */
    public Ad findById(Integer id) {
        return adMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     *
     * @param ad
     */
    public void add(Ad ad) {
        adMapper.insert(ad);
        saveAdToRedisByPosition(ad.getPosition());
    }

    /**
     * 修改
     *
     * @param ad
     */
    public void update(Ad ad) {
        /*查询当前ad的位置*/
        String postion = adMapper.selectByPrimaryKey(ad.getId()).getPosition();
        adMapper.updateByPrimaryKeySelective(ad);
        saveAdToRedisByPosition(ad.getPosition());
       /* 判断如果当前修改的位置和传进来的位置发生变化*/
        if (!postion.equals(ad.getPosition())){
            saveAdToRedisByPosition(ad.getPosition());
        }
    }

    /**
     * 删除
     *
     * @param id
     */
    public void delete(Integer id) {
        String position = adMapper.selectByPrimaryKey(id).getPosition();
        saveAdToRedisByPosition(position);
        adMapper.deleteByPrimaryKey(id);


    }

    /*根据位置查询广告列表*/
    public List<Ad> findByPosition(String position) {
        List<Ad> result = (List<Ad>)redisTemplate.opsForHash().get(IConstsRedis.getRedisKey(position), IConstsRedis.Category_ad_redis);
        if (result == null) {
            Example example = new Example(Ad.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("position", position);
            criteria.andLessThanOrEqualTo("startTime", new Date());
            criteria.andGreaterThanOrEqualTo("endTime", new Date());
            criteria.andEqualTo("status", 1);
            List<Ad> ads = adMapper.selectByExample(example);
            return ads;
        }else {
            return result;
        }
    }

    public void saveAdToRedisByPosition(String position) {
        Example example = new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("positon", position);
        criteria.andLessThanOrEqualTo("startTime", new Date());  // 当前时间大于开始时间， 广告播放都是有限制的
        criteria.andGreaterThanOrEqualTo("endTime", new Date()); // 当前时间小于结束时间
        criteria.andEqualTo("status", 1);    /*标识广告当前是否有效*/
        List<Ad> ads = adMapper.selectByExample(example);
        redisTemplate.opsForHash().put(IConstsRedis.getRedisKey(position), IConstsRedis.Category_ad_redis, ads);
    }

    /*获得广告轮播图位置列表，然后分别存入缓存*/
    private List<String> getPositionList() {
        List<String> adList = new ArrayList<String>();
        adList.add("index_1b");
        //添加广告到列表中
        return adList;
    }

    public void saveAllToRedis() {
        for (String position : getPositionList()) {
            saveAdToRedisByPosition(position);
        }
    }

    /**
     * 构建查询条件
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Ad.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 广告名称
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 广告位置
            if (searchMap.get("position") != null && !"".equals(searchMap.get("position"))) {
                criteria.andLike("position", "%" + searchMap.get("position") + "%");
            }
            // 状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andLike("status", "%" + searchMap.get("status") + "%");
            }
            // 图片地址
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // URL
            if (searchMap.get("url") != null && !"".equals(searchMap.get("url"))) {
                criteria.andLike("url", "%" + searchMap.get("url") + "%");
            }
            // 备注
            if (searchMap.get("remarks") != null && !"".equals(searchMap.get("remarks"))) {
                criteria.andLike("remarks", "%" + searchMap.get("remarks") + "%");
            }

            // ID
            if (searchMap.get("id") != null) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }

        }
        return example;
    }

}

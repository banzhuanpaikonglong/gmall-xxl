package com.littlestore.shoppingmall.shopmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.littlestore.shoppingmall.bean.*;
import com.littlestore.shoppingmall.config.RedisUtil;
import com.littlestore.shoppingmall.service.ManageService;
import com.littlestore.shoppingmall.shopmanageservice.constant.ManageConst;
import com.littlestore.shoppingmall.shopmanageservice.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author shkstart
 * @create 2019-07-03 19:48
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper ;

    @Autowired
    private SkuImageMapper skuImageMapper ;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper ;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper ;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 bc2 = new BaseCatalog2();
        bc2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(bc2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 bc3 = new BaseCatalog3();
        bc3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(bc3);
    }

    //只查一张表
//    @Override
//    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        BaseAttrInfo ba = new BaseAttrInfo();
//        ba.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(ba);
//    }


    //select自定义查询// 根据三级分类id查询属性表
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 处理baseAttrInfo
        if (baseAttrInfo.getId()!=null && baseAttrInfo.getId().length()>0){
            // 修改baseAttrInfo
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            //添加baseAttrInfo
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        // 处理baseAttrValue 修改时，先将已有的数据删除
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);

        // 添加baseAttrValue
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList!=null && attrValueList.size()>0){
//        if (attrValueList.size()>0 && attrValueList!=null ){
            for (BaseAttrValue attrValue : attrValueList) {
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        // select * from baseAttrValue where attrId = attrId
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
        return baseAttrValueList;
    }

    //根据业务逻辑
    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        //  attrId=baseAttrInfo.id
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);

        // 将平台属性值对象集合付给平台属性对象
        baseAttrInfo.setAttrValueList(baseAttrValueList);

        return baseAttrInfo;
    }


    @Override
    public List<SpuInfo> spuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<SpuInfo> spuList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {

        //第一步spuInfo
        if (spuInfo.getId() !=null && spuInfo.getId().length() > 0){
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }else{
            spuInfoMapper.insertSelective(spuInfo);
        }
        //第二步spuImage
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() >0){
            //遍历图片
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }
        // 第三步spuSaleAttr
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList !=null && spuSaleAttrList.size() >0){
//        if (spuSaleAttrList.size() >0 && spuSaleAttrList !=null ){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                // 第四步spuSaleAttrValue
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList !=null && spuSaleAttrValueList.size() >0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);


                    }
                }
            }
        }
    }


    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        return spuImageMapper.select(spuImage);
    }


    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {

        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    /*
    1.	获取页面的数据
	2.	创建一个接收前端数据的实体类？skuInfo
    3.	数据放入以下表中：
        skuInfo : 库存单元表 {小辣椒 红辣椒7X 4+64GB 学生智能手机 美颜双摄 微Q多开 人脸识别 移动联通电信4G全网通 黑色}SKU
        skuImage: 库存{sku}图片表 都是从SPU 中抽离出来的！
        skuSaleAttrValue：销售属性值关联表
        skuAttrValue：平台销售属性关联表
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //skuInfo
        if (skuInfo.getId() != null && skuInfo.getId().length() >0 ){
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }else{
            skuInfoMapper.insertSelective(skuInfo);
        }
        //skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() >0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }

        //skuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() >0 ){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }

        //skuAttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() >0 ){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        Jedis jedis = null ;

        SkuInfo skuInfo = null ;

        // return getSkuInfoRedisson(skuId, jedis);
        return getSkuInfoJedis(jedis,skuId);
    }

    private SkuInfo getSkuInfoJedis (Jedis jedis,String skuId){

        SkuInfo skuInfo = null;
        try {
            jedis = redisUtil.getJedis();
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            String skuJson = jedis.get(skuKey);
            if (skuJson == null) {
                System.out.println("缓存中没有数据");
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                String lockKey = jedis.set(skuLockKey,"OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);

                if ("OK".equals(lockKey)){
                    System.out.println("获取分布式锁！");
                    skuInfo = getSkuInfoDB(skuId);
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);
                    return skuInfo;

                }else {
                    System.out.println("等待！");
                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }

            }else {
                skuInfo = JSON.parseObject(skuJson,SkuInfo.class);
                return skuInfo;
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedisson(String skuId,Jedis jedis){

            SkuInfo skuInfo;
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://192.168.26.171:6379");
                    RedissonClient redissonClient = Redisson.create(config);

            RLock lock = redissonClient.getLock("my-lock");
            // 上锁
            lock.lock();
            jedis = redisUtil.getJedis();
            // 定义key 见名知意 sku:skuId:info
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;

            if (jedis.exists(skuKey)){
                String skuJson = jedis.get(skuKey);
                if (StringUtils.isNoneEmpty(skuJson)){
                    // 将字符串转换为对象
                    skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                    return skuInfo;
                }
            }else {
                // redis 中没有数据，则从数据库中查询，放入到redis
                SkuInfo skuInfoDB = getSkuInfoDB(skuId);

                //if (skuInfoDB!=null){
                jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfoDB));
                //}
                return skuInfoDB;
            }
            lock.unlock();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 解决空指针
            if (jedis!=null){
                jedis.close();

            }
        }
        return getSkuInfoDB(skuId);

    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImages);

        //添加SkuAttrValue
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> attrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(attrValueList);
        return  skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId) ;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        /*
            第一种：直接将集合转换为字符串。
            第二种：在mybatis 中使用 foreach 进行循环遍历。
         */
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        return baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
    }
}

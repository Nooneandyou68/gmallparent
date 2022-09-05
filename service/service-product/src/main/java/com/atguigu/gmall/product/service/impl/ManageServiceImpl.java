package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import javassist.bytecode.stackmap.BasicBlock;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @PROJECT_NAME: gamallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/26 10:23
 */
@Service
public class ManageServiceImpl implements ManageService {

    //接口上有@Mapper注解 ，extends  BaseMapper<BaseCategory1> 封装了对表的crud
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SpuPosterMapper spuPosterMapper;
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 根据一级id查询二级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:36
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 根据二级id查询二级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:36
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id", category1Id));
    }

    /**
     * 根据三级id查询三级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:36
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id", category2Id));
    }

    /**
     * 根据分类Id 获取平台属性数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 18:02
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.getAttrInfoList(category1Id, category2Id, category3Id);
    }

    /**
     * 保存平台属性方法
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 19:03
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null) {//如果不为null代表要执行修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
            // baseAttrValue 平台属性值
            // 修改：通过先删除{baseAttrValue}，在新增的方式！
            // 删除条件：baseAttrValue.attrId = baseAttrInfo.id
            QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(wrapper);
        } else {//如果为null代表要执行添加
            //  新增
            //  保存数据： base_attr_info
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        //循环遍历添加数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)) {
            attrValueList.forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }
    }

    /**
     * 根据属性id获取属性值
     *
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attrId);
        return baseAttrValueMapper.selectList(wrapper);
    }

    /**
     * 根据attrId 查询平台属性对象
     *
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        //根据attrId查询平台属性
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo != null) {
            //  查询平台属性值集合数据并赋值
            baseAttrInfo.setAttrValueList(this.getAttrValueList(attrId));
        }
        return baseAttrInfo;
    }

    /**
     * 查询销售属性集合
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/29 17:19
     */
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * 根据spuId 查询spuImageList
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> imageQueryWrapper = new QueryWrapper<>();
        imageQueryWrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(imageQueryWrapper);
    }

    /**
     * 根据spuId 查询销售属性集合
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }
    /**
     * 保存sku
     *
     * @param skuInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        //sku_info
        skuInfoMapper.insert(skuInfo);
        //sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            });
        }
        //sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }
        //sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            });
        }
        //添加布隆过滤
        RBloomFilter<Long> rbloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        rbloomFilter.add(skuInfo.getId());
    }

    /**
     * 根据三级分类数据获取到skuInfo 列表
     *
     * @param skuInfoPage
     * @param skuInfo
     * @return
     */
    @Override
    public IPage<SkuInfo> getSpuInfoList(Page<SkuInfo> skuInfoPage, SkuInfo skuInfo) {
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.eq("category3_id", skuInfo.getCategory3Id());
        return skuInfoMapper.selectPage(skuInfoPage, skuInfoQueryWrapper);
    }

    /**
     * 商品上架
     *
     * @param skuId
     * @return
     */
    @Override
    public void onSale(Long skuId) {
        // 更改销售状态
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }
    /**
     * 商品下架
     *
     * @param skuId
     * @return
     */
    @Override
    public void cancelSale(Long skuId) {
        // 更改销售状态
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    /**
     * 根据skuId获取skuInfo（redis分布式锁）
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:32
     */
    @Override
    @GmallCache(prefix = "skuInfo:")
    public SkuInfo getSkuInfo(Long skuId) {
        return getSkuInfoDB(skuId);
        //return getSkuInfoRedisson(skuId);
    }
    private SkuInfo getSkuInfoRedisson(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            // 缓存存储数据：key-value
            // 定义key sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX+ skuId +RedisConst.SKUKEY_SUFFIX;
            // 获取里面的数据？ redis 有五种数据类型 那么我们存储商品详情 使用哪种数据类型？
            // 获取缓存数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 如果从缓存中获取的数据是空
            if (skuInfo==null){
                // 直接获取数据库中的数据，可能会造成缓存击穿。所以在这个位置，应该添加锁。
                // 第二种：redisson
                // 定义锁的key sku:skuId:lock  set k1 v1 px 10000 nx
                String lockKey = RedisConst.SKUKEY_PREFIX+ skuId +RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
            /*
            第一种： lock.lock();
            第二种:  lock.lock(10,TimeUnit.SECONDS);
            第三种： lock.tryLock(100,10,TimeUnit.SECONDS);
             */
                // 尝试加锁
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res){
                    try {
                        // 处理业务逻辑 获取数据库中的数据
                        // 真正获取数据库中的数据 {数据库中到底有没有这个数据 = 防止缓存穿透}
                        skuInfo = getSkuInfoDB(skuId);
                        // 从数据库中获取的数据就是空
                        if (skuInfo==null){
                            // 为了避免缓存穿透 应该给空的对象放入缓存
                            SkuInfo skuInfo1 = new SkuInfo(); //对象的地址
                            redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        // 查询数据库的时候，有值
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);

                        // 使用redis 用的是lua 脚本删除 ，但是现在用么？ lock.unlock
                        return skuInfo;

                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        // 解锁：
                        lock.unlock();
                    }
                }else {
                    // 其他线程等待
                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }
            }else {

                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 为了防止缓存宕机：从数据库中获取数据
        return getSkuInfoDB(skuId);
    }
    private  SkuInfo getInfoRedisTemplate(Long skuId, RedisTemplate redisTemplate) {
        SkuInfo skuInfo = null;
        try {
            // 缓存存储数据：key-value
            // 定义key sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX+ skuId +RedisConst.SKUKEY_SUFFIX;
            // 获取里面的数据？ redis 有五种数据类型 那么我们存储商品详情 使用哪种数据类型？
            // 获取缓存数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 如果从缓存中获取的数据是空
            if (skuInfo==null){
                // 直接获取数据库中的数据，可能会造成缓存击穿。所以在这个位置，应该添加锁。
                // 第一种：redis ，第二种：redisson
                // 定义锁的key sku:skuId:lock  set k1 v1 px 10000 nx
                String lockKey = RedisConst.SKUKEY_PREFIX+ skuId +RedisConst.SKULOCK_SUFFIX;
                // 定义锁的值
                String uuid = UUID.randomUUID().toString().replace("-","");
                // 上锁
                Boolean isExist = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (isExist){
                    // 执行成功的话，则上锁。
                    System.out.println("获取到分布式锁！");
                    // 真正获取数据库中的数据 {数据库中到底有没有这个数据 = 防止缓存穿透}
                    skuInfo = getSkuInfoDB(skuId);
                    // 从数据库中获取的数据就是空
                    if (skuInfo==null){
                        // 为了避免缓存穿透 应该给空的对象放入缓存
                        SkuInfo skuInfo1 = new SkuInfo(); //对象的地址
                        redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        return skuInfo1;
                    }
                    // 查询数据库的时候，有值
                    redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    // 解锁：使用lua 脚本解锁
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    // 设置lua脚本返回的数据类型
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    // 设置lua脚本返回类型为Long
                    redisScript.setResultType(Long.class);
                    redisScript.setScriptText(script);
                    // 删除key 所对应的 value
                    redisTemplate.execute(redisScript, Arrays.asList(lockKey),uuid);
                    return skuInfo;
                }else {
                    // 其他线程等待
                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }
            }else {
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 为了防止缓存宕机：从数据库中获取数据

        return getSkuInfoDB(skuId);
    }

    /**
     * 根据skuId获取skuInfo(直接从数据库获取数据)
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:32
     */
    public SkuInfo getSkuInfoDB(Long skuId) {
        //根据skuId获取skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //根据skuId获取图片集合
        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        //放入skuInfo对象
        skuInfo.setSkuImageList(skuImageList);
        //返回数据
        return skuInfo;
    }

    /**
     * 根据skuId获取海报图片
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:39
     */
    @Override
    @GmallCache(prefix = "spuPoster:")
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        return spuPosterMapper.selectList(new QueryWrapper<SpuPoster>().eq("spu_id", spuId));
    }

    /**
     * 根据skuId获取最新商品价格
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:45
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        //上锁
        String locKey = "price:" + skuId;
        RLock lock = this.redissonClient.getLock(locKey);
        lock.lock();
        try {
            //构建查询条件
            QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", skuId);
            //设置查询字段
            queryWrapper.select("price");
            SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);
            if (skuInfo != null) {
                return skuInfo.getPrice();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //解锁
            lock.unlock();
        }
        return new BigDecimal(0);
    }

    /**
     * 通过三级分类id查询分类信息
     *
     * @param category3Id
     * @return
     */
    @GmallCache(prefix = "categoryView:")
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 根据spuId，skuId 查询销售属性集合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "spuSaleAttr:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {

        return spuSaleAttrMapper.findSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * 根据spuId 查询map 集合属性
     *
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "skuValueIdsMap:")
    public Map getSkuValueIdsMap(Long spuId) {
        //声明一个map集合
        HashMap<Object, Object> hashMap = new HashMap();
        //调用mapper获取数据
        List<Map> mapList = skuSaleAttrValueMapper.selectSkuValueIdsMap(spuId);
        if (!CollectionUtils.isEmpty(mapList)) {
            mapList.forEach(map -> {
                //通过映射别名获取数据
                hashMap.put(map.get("value_ids"), map.get("sku_id"));
            });
        }
        return hashMap;
    }

    /**
     * 通过skuId 集合来查询数据
     *
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = "attrList:")
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectAttrList(skuId);
    }

    /**
     * 获取全部分类信息
     *
     * @return
     */
    @Override
    @GmallCache(prefix = "index:")
    public List<JSONObject> getBaseCategoryList() {
        //创建一个存储一级分类数据的集合
        ArrayList<JSONObject> list = new ArrayList<>();
        /*
        * 数据结构组装！
        * 1、先查询所有的分类数据
        * 2、找出一级分类下的二级数据
        * 3、找出二级分类下的三级数据
        * */
        //查询所有分类数据
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //按照一级分类id进行分株 获取到map集合
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //定义一个变量
        int index = 1;
        //声明一个集合来存储二级分类数据
        ArrayList<JSONObject> category2ChildList = new ArrayList<>();
        //迭代获取一级分类数据
        Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator = category1Map.entrySet().iterator();
        while (iterator.hasNext()) {
            //声明一个存储一级分类数据的对象
            JSONObject category1 = new JSONObject();
            Map.Entry<Long, List<BaseCategoryView>> entry = iterator.next();
            //获取到一级分类id
            Long category1Id = entry.getKey();
            //获取一级分类id对应的数据
            List<BaseCategoryView> baseCategoryViewList1 = entry.getValue();
            String category1Name = baseCategoryViewList1.get(0).getCategory1Name();
            category1.put("index", index);
            category1.put("categoryId", category1Id);
            category1.put("categoryName", category1Name);
            //  存储二级分类数据集合
            //  category1.put("categoryChild","categoryChild");
            //变量迭代
            index++;

            //获取二级分类数据
            Map<Long, List<BaseCategoryView>> category2Map = baseCategoryViewList1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //迭代获取二级分类数据
            Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator1 = category2Map.entrySet().iterator();
            while (iterator1.hasNext()) {
                //声明一个存储二级分类数据的对象
                JSONObject category2 = new JSONObject();
                //获取到迭代器的数据
                Map.Entry<Long, List<BaseCategoryView>> entry1 = iterator1.next();
                //获取到二级分类id
                Long category2Id = entry1.getKey();
                //获取二级分类id对应的数据
                List<BaseCategoryView> baseCategoryViewList2 = entry1.getValue();
                String category2Name = baseCategoryViewList2.get(0).getCategory2Name();
                category2.put("categoryId", category2Id);
                category2.put("categoryName", category2Name);
                //  存储三级级分类数据集合
                //  category2.put("categoryChild","categoryChild");
                category2ChildList.add(category2);

                //声明一个集合来存储三级分类数据
                ArrayList<JSONObject> category3ChildList = new ArrayList<>();
                //获取三级分类数据
                baseCategoryViewList2.forEach(baseCategoryView -> {
                    //声明一个存储二级分类数据的对象
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId", baseCategoryView.getCategory3Id());
                    category3.put("categoryName", baseCategoryView.getCategory3Name());
                    category3ChildList.add(category3);
                });
                //将三级分类数据添加到二级分类
                category2.put("categoryChild", category3ChildList);
            }
            //将二级分类数据添加到一级分类
            category1.put("categoryChild",category2ChildList);
            //将一级分类数据添加到集合
            list.add(category1);
        }
        //返回所有数据
        return list;
    }
}

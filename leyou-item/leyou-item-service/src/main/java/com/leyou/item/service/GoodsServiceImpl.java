package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.pojo.Stock;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements  GoodsService {
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    //商品新增
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
       spuMapper.insertSelective(spuBo);
        //新增商品详情
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insertSelective(spuDetail);
        saveSkusAndStock(spuBo);
        sendMsg("insert",spuBo.getId());
    }
    //修改商品的方法
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //删除库存
        Sku record=new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = skuMapper.select(record);
        skus.forEach(sku -> {
            stockMapper.deleteByPrimaryKey(sku.getId());
        });
        //删除所有sku
        skuMapper.delete(record);
        //新增sku和库存
        saveSkusAndStock(spuBo);
        //更新spu
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        spuMapper.updateByPrimaryKeySelective(spuBo);
        //更新spudetail
        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        sendMsg("update",spuBo.getId());

    }
    private void sendMsg(String type,Long id) {
        try {
            amqpTemplate.convertAndSend("item."+type,id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    private void saveSkusAndStock(SpuBo spuBo) {
        //新增sku和库存
        spuBo.getSkus().forEach(sku -> {
            sku.setId(null);
          sku.setSpuId(spuBo.getId());
          sku.setCreateTime(new Date());
          sku.setLastUpdateTime(sku.getCreateTime());
          skuMapper.insert(sku);
            Stock stock=new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockMapper.insert(stock);
        });
    }


    public SpuDetail querySpuDetailBySpuId(Long id) {

        return spuDetailMapper.selectByPrimaryKey(id);
    }
    //根据SpuId查询sku
    public List<Sku> querySkusBySpuId(Long id) {

        Example example=new Example(Sku.class);
        Sku record=new Sku();
        record.setSpuId(id);
        List<Sku> skus = skuMapper.select(record);
        skus.forEach(sku -> {
            Stock stock = stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;
    }




    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        //开启分页查询spu
        PageHelper.startPage(page, rows);
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){criteria.andLike("title","%"+key+"%");}
        if(saleable!=null){criteria.andEqualTo("saleable",saleable);}
        List<Spu> spus = spuMapper.selectByExample(example);
        PageInfo<Spu>pageInfo=new PageInfo<>(spus);
        //封装spubo
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu, spuBo);
            List<String> names = categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "/"));
            spuBo.setBname(brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
            return spuBo;
        }).collect(Collectors.toList());

        //封装数据返回
        return new PageResult<SpuBo>(pageInfo.getTotal(),spuBos);
    }

    @Override
    public Spu querySpuById(Long id) {
        return spuMapper.selectByPrimaryKey(id);
    }
}

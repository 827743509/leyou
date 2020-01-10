package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements  SearchService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
   private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;
    private  final  static ObjectMapper MAPPER=new ObjectMapper();
    public Goods goodsBuilder(Spu spu) throws IOException {
     //查询分类
        List<String> names = categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
         //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
         //查询所有sku
        List<Sku> skus = goodsClient.querySkuBySpuId(spu.getId());
        //封装skulist
        List<Map<String,Object>> skuList=new ArrayList<>();
        skus.forEach(sku -> {
            Map map=new HashMap<String,Object>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("image",StringUtils.isBlank(sku.getImages())?"":sku.getImages().split(",")[0]);
            map.put("price",sku.getPrice());
            skuList.add(map);
        });
        //封装价格
        List<Long> prices = skus.stream().map(sku -> {
            return sku.getPrice();
        }).collect(Collectors.toList());
        //封装规格参数
        List<SpecParam> params = specificationClient.querySpecParam(null, spu.getCid3(), null, true);
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
        //通用参数
        Map<String,Object> generic_spec=MAPPER.readValue(spuDetail.getGenericSpec(),new TypeReference<Map<String,Object>>(){});
       //特殊参数
        Map<String,List<String>>special_spec=MAPPER.readValue(spuDetail.getSpecialSpec(),
                new TypeReference<Map<String,List<String>>>(){});
        Map<String, Object> specs = new HashMap<>();
        params.forEach(param ->{

            //判断是通用还是特殊参数
            if(param.getGeneric()){
                String value = generic_spec.get(param.getId().toString()).toString();
                //判断是否是数字类型
                if(param.getNumeric()){
                  value=chooseSegment(value,param);
                }
                specs.put(param.getName(), value);
            }
            else {
               specs.put(param.getName(),special_spec.get(param.getId().toString()));
            }

        } );
        Goods goods=new Goods();
       goods.setId(spu.getId());
       //标题+分类+品牌
       goods.setAll(spu.getTitle()+" "+ StringUtils.join(names," ") +" "+brand.getName());
      goods.setBrandId(spu.getBrandId());
      goods.setCid1(spu.getCid1());
      goods.setCid2(spu.getCid2());
      goods.setCid3(spu.getCid3());
      goods.setCreateTime(spu.getCreateTime());
      goods.setPrice(prices);
      goods.setSkus(MAPPER.writeValueAsString(skuList));
      goods.setSubTitle(spu.getSubTitle());
      goods.setSpecs(null);
        return goods;
    }

    @Override
    public PageResult<Goods> search(SearchRequest request) {
          //构建查询条件
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //添加分页
        queryBuilder.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
        //添加过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
        //查询
        Page<Goods> result = goodsRepository.search(queryBuilder.build());
        return new PageResult<Goods>(result.getTotalElements(),((Integer)result.getTotalPages()).longValue(),result.getContent());

    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}

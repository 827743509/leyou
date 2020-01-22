package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
        //通用参数反序列化
        Map<String,Object> generic_spec=MAPPER.readValue(spuDetail.getGenericSpec(),new TypeReference<Map<String,Object>>(){});
        //特殊参数反序列化
        Map<String,List<Object>>special_spec=MAPPER.readValue(spuDetail.getSpecialSpec(),
                new TypeReference<Map<String,List<Object>>>(){});
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
        goods.setSpecs(specs);
        return goods;
    }

    @Override
    public SearchResult search(SearchRequest request) {
        //构建查询条件
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
       // QueryBuilder basicQueryBuilder = QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND);
        BoolQueryBuilder basicQueryBuilder=buildBoolQueryBuilder(request);
        queryBuilder.withQuery(basicQueryBuilder);
        //添加分页
        queryBuilder.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
        //添加过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
        //添加分类和品牌的聚合
        String categoryAggName="categories";
        String brandAggName="brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //查询
        AggregatedPage<Goods> result = (AggregatedPage<Goods>)goodsRepository.search(queryBuilder.build());
        //获取聚合结果集并解析
        List<Map<String,Object>>categories= getCategoryAggResult(result.getAggregation(categoryAggName));
        List<Brand>brands= getBrandAggResult(result.getAggregation(brandAggName));
        //是否是一个分类,只有一个分类时才做规格参数聚合
        List<Map<String,Object>>specs=null;
        if(!CollectionUtils.isEmpty(categories)&&categories.size()==1){
            //对规格参数进行聚合
        specs=getParamAggResult((Long)categories.get(0).get("id"),basicQueryBuilder);
        }
        return new SearchResult(result.getTotalElements(),((Integer)result.getTotalPages()).longValue(),result.getContent(),categories,brands,specs);

    }


    //构建布尔查询
    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //给布尔查询添加基本的查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //添加过滤条件
        Map<String, Object> filter = request.getFilter();
        //遍历过滤信息
        for (Map.Entry<String,Object> entry:filter.entrySet()) {
            String key = entry.getKey();
            if(StringUtils.equals("品牌",key)){
                key="brandId";
            }
            else  if(StringUtils.equals("分类",key)){
                key="cid3";
            }
            else{
                key="specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return  boolQueryBuilder;
    }

    //根据查询条件聚合规格参数
    private List<Map<String, Object>> getParamAggResult(Long cid, QueryBuilder basicQueryBuilder) {
         //自定义查询对象构造
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加过滤条件

        //添加基本查询条件
        queryBuilder.withQuery(basicQueryBuilder);
        //查询要聚合的规格参数
        List<SpecParam> params = specificationClient.querySpecParam(null, cid, null, true);
        //添加规格参数的聚合
        params.forEach(param -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));

        });
        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //执行聚合查询,获取聚合结果集
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());
        //解析聚合结果集 key:聚合名称(规格参数名称) value:聚合对象
        List<Map<String,Object>> specs= new ArrayList<>();
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for(Map.Entry<String,Aggregation> entry:aggregationMap.entrySet()){
            //初始化map key:规格参数名,value:聚合的规格参数数组
            Map<String,Object> map=new HashMap<>();
            map.put("k",entry.getKey());
            //初始化一个options集合收集桶中的key
            List<String> options=new ArrayList<>();
            StringTerms terms = (StringTerms) entry.getValue();
            //获取桶集合
            terms.getBuckets().forEach(bucket -> {
                  options.add(bucket.getKeyAsString());
            });
            map.put("options",options);
            specs.add(map);
        }
        return  specs;
    }

    //解析品牌的聚合结果集
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        List<Brand> brands=new ArrayList<>();
       LongTerms terms= (LongTerms)aggregation;
         terms.getBuckets().forEach(bucket -> {
             Brand brand = brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
             brands.add(brand);
         });
        return  brands;
    }
    //解析分类的聚合结果集
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms terms= (LongTerms)aggregation;
        //获取桶的聚合转化为List<Map<String,Object>>
       return terms.getBuckets().stream().map(bucket -> {
           //初始化map
           Map<String,Object> map=new HashMap<>();
           //获取桶的分类id
            Long id = bucket.getKeyAsNumber().longValue();
            //根据分类id查询分类名称
            List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(id));
            map.put("id",id);
            map.put("name",names.get(0));
            return  map;
        }).collect(Collectors.toList());
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

    @Override
    public void save(Long id) throws IOException {
        Spu spu = goodsClient.querySpuById(id);
        Goods goods = goodsBuilder(spu);
        goodsRepository.save(goods);
    }
}

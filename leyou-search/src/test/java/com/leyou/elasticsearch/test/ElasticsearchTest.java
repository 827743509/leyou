package com.leyou.elasticsearch.test;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTest {
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private GoodsRepository repository;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;
    @Test
    public  void  save(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
        Integer page=1;
        Integer rows=100;
      do {
          PageResult<SpuBo> result = goodsClient.querySpu(null, null, page, rows);
          List<Goods> goodsList = result.getItems().stream().map(spuBo -> {
              try {
                  return searchService.goodsBuilder(spuBo);
              } catch (IOException e) {
                  e.printStackTrace();
                  return null;
              }
          }).collect(Collectors.toList());
          repository.saveAll(goodsList);
          page++;
          rows = result.getItems().size();
      }while (rows==100);


    }
    @Test
public  void testStreamMap(){
           String a="dasdsadsadasd";

        System.out.println(a.getBytes()[0]);

}
}

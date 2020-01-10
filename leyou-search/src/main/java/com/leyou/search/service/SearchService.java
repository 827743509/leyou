package com.leyou.search.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;

import java.io.IOException;

public interface SearchService {
    Goods goodsBuilder(Spu spu) throws IOException;

    PageResult<Goods> search(SearchRequest request);
}

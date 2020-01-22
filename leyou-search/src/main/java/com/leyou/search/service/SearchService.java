package com.leyou.search.service;

import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;

import java.io.IOException;

public interface SearchService {
    Goods goodsBuilder(Spu spu) throws IOException;

    SearchResult search(SearchRequest request);

    void save(Long id) throws IOException;
}

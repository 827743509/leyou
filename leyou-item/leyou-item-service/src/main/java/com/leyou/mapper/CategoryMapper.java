package com.leyou.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface  CategoryMapper extends Mapper<Category> {
    public List<Category> getCategoryById(Long id);
}

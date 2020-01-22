package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
private CategoryMapper categoryMapper;
    public List<Category> getCategoryById(Long id) {
        Category category=new Category();
        category.setParentId(id);
        List<Category> list = categoryMapper.select(category);
        return list;
    }

    @Override
    public List<String> queryNamesByIds(List<Long> ids) {
       return  ids.stream().map(id -> {
            return categoryMapper.selectByPrimaryKey(id).getName();
        }).collect(Collectors.toList());


    }
}

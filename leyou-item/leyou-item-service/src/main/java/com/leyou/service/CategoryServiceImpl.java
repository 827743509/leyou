package com.leyou.service;

import com.leyou.item.pojo.Category;
import com.leyou.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
}

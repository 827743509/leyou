package com.leyou.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface CategoryService {
    public List<Category> getCategoryById(Long id);
}

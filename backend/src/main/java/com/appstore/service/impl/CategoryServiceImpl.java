package com.appstore.service.impl;

import com.appstore.common.ResultCode;
import com.appstore.entity.Category;
import com.appstore.exception.BusinessException;
import com.appstore.mapper.AppInfoMapper;
import com.appstore.mapper.CategoryMapper;
import com.appstore.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 分类服务实现
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private AppInfoMapper appInfoMapper;

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSortOrder));
    }

    @Override
    public List<Category> getSubCategories(Long parentId) {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .eq(Category::getStatus, 1)
                .eq(Category::getDeleted, 0)
                .orderByAsc(Category::getSortOrder));
    }

    @Override
    public void addCategory(Category category) {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分类名称不能为空");
        }
        if (category.getCategoryName().length() > 50) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分类名称不能超过50字");
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        // 二级分类不允许再挂二级分类（仅支持两级）
        if (category.getParentId() != 0) {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null || parent.getParentId() != 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "只支持一级分类下挂二级分类");
            }
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        category.setStatus(category.getStatus() == null ? 1 : category.getStatus());
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Category category) {
        if (category.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分类ID不能为空");
        }
        // 名称校验
        if (category.getCategoryName() != null) {
            if (category.getCategoryName().trim().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分类名称不能为空");
            }
            if (category.getCategoryName().length() > 50) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "分类名称不能超过50字");
            }
            category.setCategoryName(category.getCategoryName().trim());
        }
        // 层级校验：不允许把分类挂到自己或子分类下面（防止循环层级）
        if (category.getParentId() != null && category.getParentId() != 0) {
            if (category.getParentId().equals(category.getId())) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不能将分类挂到自身下");
            }
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null || parent.getParentId() != 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "只支持一级分类下挂二级分类");
            }
        }
        // 二级分类修改为一级分类时校验：需先确认其下无子分类（由前端业务保证，服务端兜底）
        if (category.getParentId() != null && category.getParentId() == 0) {
            Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                    .eq(Category::getParentId, category.getId())
                    .eq(Category::getDeleted, 0));
            if (childCount > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该分类下存在子分类，不能调整为一级分类");
            }
        }
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 存在子分类时禁止删除
        Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id)
                .eq(Category::getDeleted, 0));
        if (childCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该分类下存在子分类，请先删除子分类");
        }
        // 存在关联应用时禁止删除
        Long appCount = appInfoMapper.selectCount(new LambdaQueryWrapper<com.appstore.entity.AppInfo>()
                .and(w -> w.eq(com.appstore.entity.AppInfo::getCategoryId, id)
                        .or().eq(com.appstore.entity.AppInfo::getSubCategoryId, id))
                .eq(com.appstore.entity.AppInfo::getDeleted, 0));
        if (appCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该分类下存在应用，无法删除");
        }
        categoryMapper.deleteById(id);
        log.info("删除分类: id={}, 操作人={}", id, com.appstore.util.UserContext.getUsername());
    }
}

package com.appstore.mapper;

import com.appstore.entity.AppInfo;
import com.appstore.entity.Favorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 分页查询我的收藏应用（SQL层分页，按收藏时间倒序）
     * 通过 JOIN 保证只返回未被逻辑删除的应用
     */
    @Select("SELECT a.* FROM app_favorite f " +
            "INNER JOIN app_info a ON f.app_id = a.id AND a.deleted = 0 " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    IPage<AppInfo> selectMyFavoriteApps(Page<?> page, @Param("userId") Long userId);
}

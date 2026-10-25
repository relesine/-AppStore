package com.appstore.mapper;

import com.appstore.entity.AppInfo;
import com.appstore.entity.Download;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DownloadMapper extends BaseMapper<Download> {

    /**
     * 分页查询我的下载应用（按应用去重，SQL层分页）
     * 以"最近一次下载时间"倒序，仅返回未被逻辑删除的应用
     */
    @Select("SELECT a.* FROM app_download d " +
            "INNER JOIN app_info a ON d.app_id = a.id AND a.deleted = 0 " +
            "WHERE d.user_id = #{userId} " +
            "GROUP BY d.app_id " +
            "ORDER BY MAX(d.download_time) DESC")
    IPage<AppInfo> selectMyDownloadApps(Page<?> page, @Param("userId") Long userId);
}

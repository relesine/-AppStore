package com.appstore.mapper;

import com.appstore.entity.AppInfo;
import com.appstore.vo.AppInfoVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 应用信息数据访问层
 * 说明：selectAppListPage 使用 MyBatis Plus 分页插件在 SQL 层完成分页，
 *      避免原先"全表查询后内存分页"导致的性能问题。
 */
@Mapper
public interface AppInfoMapper extends BaseMapper<AppInfo> {

    /** 应用详情（关联分类名、作者昵称） */
    @Select("SELECT a.*, c.category_name as categoryName, sc.category_name as subCategoryName, u.nickname as authorName " +
            "FROM app_info a LEFT JOIN app_category c ON a.category_id = c.id " +
            "LEFT JOIN app_category sc ON a.sub_category_id = sc.id " +
            "LEFT JOIN sys_user u ON a.author_id = u.id " +
            "WHERE a.id = #{id} AND a.deleted = 0")
    AppInfoVO selectAppDetail(@Param("id") Long id);

    /**
     * 分页查询已上架应用列表（SQL层分页）
     * 仅返回 status=1(已通过) 且审核通过(audit_status=2) 的应用，按下载量倒序
     */
    @Select("<script>" +
            "SELECT a.*, c.category_name as categoryName, u.nickname as authorName " +
            "FROM app_info a LEFT JOIN app_category c ON a.category_id = c.id " +
            "LEFT JOIN sys_user u ON a.author_id = u.id " +
            "WHERE a.deleted = 0 AND a.status = 1 AND a.audit_status = 2 " +
            "<if test='categoryId != null'> AND a.category_id = #{categoryId} </if>" +
            "<if test='subCategoryId != null'> AND a.sub_category_id = #{subCategoryId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            " AND (a.app_name LIKE CONCAT('%',#{keyword},'%') OR a.short_desc LIKE CONCAT('%',#{keyword},'%')) </if>" +
            "ORDER BY a.download_count DESC" +
            "</script>")
    IPage<AppInfoVO> selectAppListPage(Page<?> page,
                                       @Param("categoryId") Long categoryId,
                                       @Param("subCategoryId") Long subCategoryId,
                                       @Param("keyword") String keyword);

    /**
     * 查询热门应用（仅已上架，按下载量倒序取前N条）
     */
    @Select("SELECT a.*, c.category_name as categoryName, u.nickname as authorName " +
            "FROM app_info a LEFT JOIN app_category c ON a.category_id = c.id " +
            "LEFT JOIN sys_user u ON a.author_id = u.id " +
            "WHERE a.deleted = 0 AND a.status = 1 AND a.audit_status = 2 " +
            "ORDER BY a.download_count DESC, a.create_time DESC " +
            "LIMIT #{limit}")
    List<AppInfoVO> selectHotApps(@Param("limit") Integer limit);

    /** 统计指定作者的累计应用下载总量 */
    @Select("SELECT COALESCE(SUM(a.download_count), 0) FROM app_info a " +
            "WHERE a.author_id = #{authorId} AND a.deleted = 0")
    Long selectAuthorTotalDownloads(@Param("authorId") Long authorId);

    /** 原子递增应用下载量（避免并发下丢失计数） */
    @Update("UPDATE app_info SET download_count = download_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementDownloadCount(@Param("id") Long id);

    /**
     * 原子更新应用评分（评论新增时调用）
     * 在同一 SQL 中完成 评分人数+1 与 平均分重算，避免并发评论时读改写丢失计数
     */
    @Update("UPDATE app_info SET rating_count = rating_count + 1, " +
            "rating = ROUND(((rating * rating_count) + #{rating}) / (rating_count + 1), 1) " +
            "WHERE id = #{appId} AND deleted = 0")
    int incrementRating(@Param("appId") Long appId, @Param("rating") Integer rating);

    /** 统计指定作者的待审核应用数 */
    @Select("SELECT COUNT(*) FROM app_info a " +
            "WHERE a.author_id = #{authorId} AND a.audit_status = 0 AND a.deleted = 0")
    long selectAuthorPendingCount(@Param("authorId") Long authorId);
}

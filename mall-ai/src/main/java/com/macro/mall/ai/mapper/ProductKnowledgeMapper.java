package com.macro.mall.ai.mapper;

import com.macro.mall.ai.domain.ProductInfoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI智能导购-商品知识Mapper
 *
 * @author macro
 */
@Mapper
public interface ProductKnowledgeMapper {

    /**
     * 根据关键词搜索商品
     * @param keyword 关键词
     * @param limit 返回数量限制
     * @return 商品列表
     */
    List<ProductInfoDTO> searchProductsByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 根据商品ID列表查询商品
     * @param productIds 商品ID列表
     * @return 商品列表
     */
    List<ProductInfoDTO> selectProductsByIds(@Param("productIds") List<Long> productIds);

    /**
     * 获取热门商品
     * @param limit 数量限制
     * @return 热门商品列表
     */
    List<ProductInfoDTO> getHotProducts(@Param("limit") Integer limit);

    /**
     * 根据分类查询商品
     * @param categoryName 分类名称
     * @param limit 数量限制
     * @return 商品列表
     */
    List<ProductInfoDTO> getProductsByCategory(@Param("categoryName") String categoryName, @Param("limit") Integer limit);

    /**
     * 根据价格区间查询商品
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param limit 数量限制
     * @return 商品列表
     */
    List<ProductInfoDTO> getProductsByPriceRange(@Param("minPrice") Double minPrice, 
                                                   @Param("maxPrice") Double maxPrice, 
                                                   @Param("limit") Integer limit);
}

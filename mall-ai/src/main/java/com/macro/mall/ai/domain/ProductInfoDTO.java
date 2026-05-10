package com.macro.mall.ai.domain;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI智能导购-商品信息DTO
 *
 * @author macro
 */
@Data
public class ProductInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 促销价格
     */
    private BigDecimal promotionPrice;

    /**
     * 销量
     */
    private Integer sale;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 商品分类名称
     */
    private String productCategoryName;

    /**
     * 是否新品：0->不是；1->是
     */
    private Integer newStatus;

    /**
     * 是否推荐：0->不推荐；1->推荐
     */
    private Integer recommandStatus;

    /**
     * 商品图片
     */
    private String pic;

    /**
     * 商品专辑图片
     */
    private String albumPics;
}

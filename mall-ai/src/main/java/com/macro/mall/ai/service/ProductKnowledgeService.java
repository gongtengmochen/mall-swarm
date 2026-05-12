package com.macro.mall.ai.service;

import com.macro.mall.ai.domain.ProductInfoDTO;
import com.macro.mall.ai.mapper.ProductKnowledgeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @auther macrozheng
 * @description 商品知识库服务（基于RAG）
 * @date 2024/4/27
 * @github https://github.com/macrozheng
 */
@Service
public class ProductKnowledgeService {

    @Autowired
    private ProductKnowledgeMapper productKnowledgeMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRODUCT_CACHE_KEY = "ai:product:";
    private static final long CACHE_EXPIRE_TIME = 3600; // 1小时

    /**
     * 根据用户问题检索相关商品
     * @param question 用户问题
     * @param limit 返回数量限制
     * @return 相关商品列表
     */
    public List<ProductInfoDTO> retrieveRelevantProducts(String question, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5;
        }

        // 从问题中提取关键词
        List<String> keywords = extractKeywords(question);
        
        List<ProductInfoDTO> allProducts = new ArrayList<>();
        
        // 对每个关键词进行搜索
        for (String keyword : keywords) {
            List<ProductInfoDTO> products = searchWithCache(keyword, limit);
            allProducts.addAll(products);
        }

        // 去重并返回
        return allProducts.stream()
                .distinct()
                .limit(limit * 2)
                .collect(Collectors.toList());
    }

    /**
     * 根据商品ID获取商品详情
     * @param productIds 商品ID列表
     * @return 商品列表
     */
    public List<ProductInfoDTO> getProductsByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new ArrayList<>();
        }
        return productKnowledgeMapper.selectProductsByIds(productIds);
    }

    /**
     * 获取热门商品
     * @param limit 数量限制
     * @return 热门商品列表
     */
    public List<ProductInfoDTO> getHotProducts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return productKnowledgeMapper.getHotProducts(limit);
    }

    /**
     * 将商品信息格式化为RAG上下文
     * @param products 商品列表
     * @return 格式化的上下文文本
     */
    public String formatProductContext(List<ProductInfoDTO> products) {
        if (products == null || products.isEmpty()) {
            return "没有找到相关商品。";
        }

        StringBuilder context = new StringBuilder();
        context.append("以下是相关商品信息：\n\n");

        for (int i = 0; i < products.size(); i++) {
            ProductInfoDTO product = products.get(i);
            context.append(String.format("商品%d：\n", i + 1));
            context.append(String.format("- 名称：%s\n", product.getName()));
            context.append(String.format("- 品牌：%s\n", product.getBrandName()));
            context.append(String.format("- 分类：%s\n", product.getProductCategoryName()));
            
            if (product.getSubTitle() != null && !product.getSubTitle().isEmpty()) {
                context.append(String.format("- 副标题：%s\n", product.getSubTitle()));
            }
            
            context.append(String.format("- 价格：￥%s\n", product.getPrice()));
            if (product.getPromotionPrice() != null && product.getPromotionPrice().compareTo(product.getPrice()) < 0) {
                context.append(String.format("- 促销价：￥%s\n", product.getPromotionPrice()));
            }
            
            context.append(String.format("- 销量：%d\n", product.getSale()));
            context.append(String.format("- 库存：%d\n", product.getStock()));
            
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                String desc = product.getDescription();
                if (desc.length() > 200) {
                    desc = desc.substring(0, 200) + "...";
                }
                context.append(String.format("- 描述：%s\n", desc));
            }
            
            if (product.getNewStatus() == 1) {
                context.append("- 标签：新品\n");
            }
            if (product.getRecommandStatus() == 1) {
                context.append("- 标签：推荐\n");
            }
            
            context.append("\n");
        }

        return context.toString();
    }

    /**
     * 从问题中提取关键词（简化版本，实际可以使用更复杂的NLP技术）
     * @param question 用户问题
     * @return 关键词列表
     */
    private List<String> extractKeywords(String question) {
        List<String> keywords = new ArrayList<>();
        
        if (question == null || question.trim().isEmpty()) {
            return keywords;
        }

        // 去除常见疑问词
        String cleaned = question.toLowerCase()
                .replaceAll("什么|哪个|怎么|如何|哪里|有没有|推荐|介绍|便宜|贵|好|坏", " ")
                .trim();

        // 按空格和标点分割
        String[] words = cleaned.split("[\\s,，.。!?！？]+");
        
        for (String word : words) {
            if (word.length() >= 2) { // 只保留长度>=2的词
                keywords.add(word);
            }
        }

        // 如果没有提取到关键词，使用原问题的部分词汇
        if (keywords.isEmpty()) {
            keywords.add(question.trim());
        }

        return keywords;
    }

    /**
     * 带缓存的商品搜索
     * @param keyword 关键词
     * @param limit 数量限制
     * @return 商品列表
     */
    private List<ProductInfoDTO> searchWithCache(String keyword, Integer limit) {
        String cacheKey = PRODUCT_CACHE_KEY + "search:" + keyword + ":" + limit;
        
        // 尝试从缓存获取（这里简化处理，实际应该序列化/反序列化）
        // 由于Redis缓存复杂对象需要序列化，这里暂时不使用缓存
        
        // 直接查询数据库
        List<ProductInfoDTO> products = productKnowledgeMapper.searchProductsByKeyword(keyword, limit);
        
        return products;
    }
}

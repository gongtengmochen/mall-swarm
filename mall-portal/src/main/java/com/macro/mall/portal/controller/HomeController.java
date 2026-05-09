package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.mapper.SmsFlashPromotionMapper;
import com.macro.mall.mapper.SmsFlashPromotionSessionMapper;
import com.macro.mall.model.*;
import com.macro.mall.model.SmsFlashPromotionExample;
import com.macro.mall.model.SmsFlashPromotionSessionExample;
import com.macro.mall.portal.dao.HomeDao;
import com.macro.mall.portal.domain.FlashPromotionProduct;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.service.HomeService;
import com.macro.mall.portal.util.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 首页内容管理Controller
 * Created by macro on 2019/1/28.
 */
@Controller
@Tag(name = "HomeController", description = "首页内容管理")
@RequestMapping("/home")
public class HomeController {
    private final HomeService homeService;
    private final SmsFlashPromotionMapper flashPromotionMapper;
    private final SmsFlashPromotionSessionMapper promotionSessionMapper;
    private final HomeDao homeDao;

    public HomeController(HomeService homeService,
                         SmsFlashPromotionMapper flashPromotionMapper,
                         SmsFlashPromotionSessionMapper promotionSessionMapper,
                         HomeDao homeDao) {
        this.homeService = homeService;
        this.flashPromotionMapper = flashPromotionMapper;
        this.promotionSessionMapper = promotionSessionMapper;
        this.homeDao = homeDao;
    }

    @Operation(summary = "首页内容页信息展示")
    @RequestMapping(value = "/content", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<HomeContentResult> content() {
        HomeContentResult contentResult = homeService.content();
        return CommonResult.success(contentResult);
    }

    @Operation(summary = "分页获取推荐商品")
    @RequestMapping(value = "/recommendProductList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProduct>> recommendProductList(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<PmsProduct> productList = homeService.recommendProductList(pageSize, pageNum);
        return CommonResult.success(productList);
    }

    @Operation(summary = "获取首页商品分类")
    @RequestMapping(value = "/productCateList/{parentId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProductCategory>> getProductCateList(@PathVariable Long parentId) {
        List<PmsProductCategory> productCategoryList = homeService.getProductCateList(parentId);
        return CommonResult.success(productCategoryList);
    }

    @Operation(summary = "根据分类获取专题")
    @RequestMapping(value = "/subjectList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<CmsSubject>> getSubjectList(@RequestParam(required = false) Long cateId,
                                                         @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<CmsSubject> subjectList = homeService.getSubjectList(cateId,pageSize,pageNum);
        return CommonResult.success(subjectList);
    }

    @Operation(summary = "分页获取人气推荐商品")
    @RequestMapping(value = "/hotProductList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProduct>> hotProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                         @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        List<PmsProduct> productList = homeService.hotProductList(pageNum,pageSize);
        return CommonResult.success(productList);
    }

    @Operation(summary = "分页获取新品推荐商品")
    @RequestMapping(value = "/newProductList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsProduct>> newProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                         @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        List<PmsProduct> productList = homeService.newProductList(pageNum,pageSize);
        return CommonResult.success(productList);
    }

    @Operation(summary = "获取当前秒杀活动的全部场次")
    @RequestMapping(value = "/flashSessionList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<SmsFlashPromotionSession>> flashSessionList() {
        Date now = new Date();
        Date currDate = DateUtil.getDate(now);
        // 查询当前进行中的秒杀活动
        SmsFlashPromotionExample example = new SmsFlashPromotionExample();
        example.createCriteria()
                .andStatusEqualTo(1)
                .andStartDateLessThanOrEqualTo(currDate)
                .andEndDateGreaterThanOrEqualTo(currDate);
        List<SmsFlashPromotion> flashPromotionList = flashPromotionMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(flashPromotionList)) {
            return CommonResult.success(null);
        }
        Long flashPromotionId = flashPromotionList.get(0).getId();
        // 查询该活动下所有场次，按开始时间排序
        SmsFlashPromotionSessionExample sessionExample = new SmsFlashPromotionSessionExample();
        sessionExample.createCriteria().andStatusEqualTo(1);
        sessionExample.setOrderByClause("start_time asc");
        List<SmsFlashPromotionSession> sessionList = promotionSessionMapper.selectByExample(sessionExample);
        // 为每个场次设置 flashPromotionId，并把时间加上今天的日期
        if (sessionList != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            
            for (SmsFlashPromotionSession session : sessionList) {
                session.setFlashPromotionId(flashPromotionId);
                // 给开始时间加上今天的日期
                if (session.getStartTime() != null) {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(session.getStartTime());
                    startCal.set(year, month, day);
                    session.setStartTime(startCal.getTime());
                }
                // 给结束时间加上今天的日期
                if (session.getEndTime() != null) {
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(session.getEndTime());
                    endCal.set(year, month, day);
                    session.setEndTime(endCal.getTime());
                }
            }
        }
        return CommonResult.success(sessionList);
    }

    @Operation(summary = "根据场次获取秒杀商品列表")
    @RequestMapping(value = "/flashProductList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<FlashPromotionProduct>> flashProductList(
            @RequestParam Long flashPromotionId,
            @RequestParam Long flashPromotionSessionId) {
        List<FlashPromotionProduct> productList = homeDao.getFlashProductList(flashPromotionId, flashPromotionSessionId);
        return CommonResult.success(productList);
    }
}

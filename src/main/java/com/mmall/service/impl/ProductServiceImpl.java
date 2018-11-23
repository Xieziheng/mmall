package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;

    public ServerResponse<String> saveOrUpdateProduct(Product product){
        if(product!=null){
            if(StringUtils.isNoneBlank(product.getSubImages())){
                String[] subImageArray = product.getSubImages().split(",");
                if(subImageArray.length>0){
                    //拿子图组中第一个元素作为主图
                    product.setMainImage(subImageArray[0]);
                }
            }
            int rowCount;
            if(product.getId()!=null){
                rowCount = productMapper.updateByPrimaryKey(product);
                if(rowCount>0){
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                }
                return ServerResponse.createByErrorMessage("更新产品失败");
            }else {
                rowCount = productMapper.insert(product);
                if(rowCount>0){
                    return ServerResponse.createBySuccessMessage("新增产品成功");
                }
                return ServerResponse.createByErrorMessage("新增产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("参数不正确");
    }

    public ServerResponse<String> setSaleStatus(Integer productId, Integer status){
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_AUGUMENT.getCode(), ResponseCode.ILLEGAL_AUGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount>0){
            return ServerResponse.createBySuccessMessage("更改商品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("更改商品销售状态失败");
    }

    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if(productId==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_AUGUMENT.getCode(),ResponseCode.ILLEGAL_AUGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product==null){
            return ServerResponse.createByErrorMessage("产品信息不存在");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category==null){
            //默认为根节点
            productDetailVo.setParentCategoryId(0);
        }else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem: productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setPrice(product.getPrice());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setStatus(product.getStatus());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHots(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        return productListVo;
    }

    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNoneBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem: productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }
}

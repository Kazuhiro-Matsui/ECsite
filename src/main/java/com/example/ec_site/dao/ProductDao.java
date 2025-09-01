package com.example.ec_site.dao;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.ec_site.dto.ProductDto;

/*
 * 商品情報にアクセスするためのDAO
 */
@Mapper
public interface ProductDao {

	// 商品情報の全件検索
    @Select("""
    			select
                    p.product_id,
                    p.product_name,
                    p.product_price,
                    p.product_image_path,
                    c.category_name
                from
                    product AS p
                inner join
                    category AS c
                on
                    p.category_id = c.category_id
                order by
                    p.product_id
            """)
    public ArrayList<ProductDto> getAllProducts();

	 // 商品IDをもとにした1件の商品情報の検索
    @Select("""
    			select
                    p.product_id,
                    p.product_name,
                    p.product_description,
                    p.product_price,
                    p.product_image_path,
                    c.category_name
                from
                    product AS p
                inner join
                    category AS c
                on
                    p.category_id = c.category_id
                where
                    p.product_id = #{productId}
            """)
    public ProductDto getProductByProductId(int productId);

}

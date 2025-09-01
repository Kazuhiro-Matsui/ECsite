package com.example.ec_site.dao;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 注文履歴情報にアクセスするためのDAO
 */
@Mapper
public interface OrderHistoryDao {

	// 注文情報を登録する
	@Insert("""
    			insert into
                    order_history
                    (product_id, order_count, ordered_at)
                values
                    (#{productId}, #{orderCount}, now())

            """)
	public void insertOrderHistory(int productId, int orderCount);

    @Select("""
                select ordered_at from order_history where product_id = #{productId} order by ordered_at desc limit 1
            """)
    public LocalDateTime getLatestOrderedAt(int productId);
}


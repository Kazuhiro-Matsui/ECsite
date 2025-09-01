package com.example.ec_site.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.ec_site.dao.OrderHistoryDao;

/**
* 注文履歴機能を制御するコントローラークラス
* 注文履歴の登録、注文完了画面の表示を行う
*/
@Controller
public class OrderHistoryController {

	// orderHistoryDao という変数名で OrderHistoryDao インターフェースを利用するための宣言
    @Autowired
    private OrderHistoryDao orderHistoryDao;


	// 注文履歴テーブルに注文情報を登録し注文完了画面を表示する
	//【Ｑ１】URL「http://localhost:8080/ecsite/order」とひもづけする
	@GetMapping("/ecsite/order")
	//パラメータとして送られてきた値 productId を引数「int productId」に、
	// orderCount を引数「int orderCount」に格納する
	public ModelAndView registerOrder(@RequestParam("productId")int productId, @RequestParam("orderCount")int orderCount, @RequestParam("productName")String productName, ModelAndView mav) {
		//注文履歴テーブルに注文情報を登録する
		orderHistoryDao.insertOrderHistory(productId, orderCount);
		LocalDateTime orderdAt = orderHistoryDao.getLatestOrderedAt(productId);

        mav.addObject("productName", productName);
        mav.addObject("orderCount", orderCount);
		mav.addObject("orderedAt", orderdAt);
		//レスポンスとして次に表示させるHTMLファイル名を指定する
		mav.setViewName("complete");
		return mav;
	}

}

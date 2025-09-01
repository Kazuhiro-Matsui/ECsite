package com.example.ec_site.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.ec_site.dao.ProductDao;
import com.example.ec_site.dto.ProductDto;
import java.util.ArrayList;

/**
* 商品情報を制御するコントローラークラス
* 商品一覧画面、商品詳細画面の表示を行う
*/
@Controller
public class ProductController {

	// productDao という変数名で ProductDao インターフェースを利用するための宣言
    @Autowired
    private ProductDao productDao;


	// 全商品情報を取得して商品一覧画面を表示する
    //URL「http://localhost:8080/ecsite/product」とひもづけする
	@GetMapping("/ecsite/product")
	public ModelAndView showProductList(ModelAndView mav) {
		// 商品テーブルから全ての商品情報を取得する
        ArrayList<ProductDto> productList = productDao.getAllProducts();
		// レスポンスに検索した全商品の情報を追加する
        mav.addObject("productList", productList);
		// レスポンスとして次に表示させるHTMLファイル名を指定する
        mav.setViewName("product_list");
		return mav;
	}

	// １件の商品情報を取得して商品詳細画面を表示する
	//URL「http://localhost:8080/ecsite/detail」とひもづけする
	@GetMapping("/ecsite/detail")
	//パラメータとして送られてきた値 productId を引数「int productId」に格納する
	public ModelAndView showDetail(@RequestParam("productId")int productId, ModelAndView mav) {
		//受け取った商品IDをもとにデータベースから１件の商品情報を取得し、product という変数（新たに宣言）に格納する
		ProductDto product = productDao.getProductByProductId(productId);
		//レスポンスに検索結果の商品情報１件分を追加する
		mav.addObject("product", product);

		//レスポンスとして次に表示させるHTMLファイル名を指定する
		mav.setViewName("detail");
		return mav;
	}

}

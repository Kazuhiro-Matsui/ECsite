package com.example.ec_site.dto;

/**
 * 商品情報を表すDTOクラス
 */
public class ProductDto {

	/** 商品ID */
	private int productId;

	/** 商品名 */
	private String productName;

	/** 商品説明 */
	private String productDescription;

	/** 商品価格 */
	private int productPrice;

	/** 商品画像パス */
	private String productImagePath;

	/** 商品カテゴリ名 */
	private String categoryName;


	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	public int getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(int productPrice) {
		this.productPrice = productPrice;
	}

	public String getProductImagePath() {
		return productImagePath;
	}

	public void setProductImagePath(String productImagePath) {
		this.productImagePath = productImagePath;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

}

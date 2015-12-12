package com.shopify.api.resources;

import org.codehaus.jackson.annotate.JsonProperty;

public class CustomCollectionWithProductsCount extends CustomCollection {

	@JsonProperty("products_count")
	public int getProductsCount() {
		return (Integer)getAttribute("products_count");
	}
	@JsonProperty("products_count")
	public void setProductsCount(int _products_count) {
		setAttribute("products_count", _products_count);
	}
	
	@JsonProperty("published_scope")
	public String getPublishedScope() {
		return (String)getAttribute("published_scope");
	}
	@JsonProperty("published_scope")
	public void setPublishedScope(String _published_scope) {
		setAttribute("published_scope", _published_scope);
	}
	
}

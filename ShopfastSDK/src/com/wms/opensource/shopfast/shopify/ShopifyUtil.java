/*
 * Copyright 2015 Waterloo Mobile Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This class accesses Shopify through its API to get data.
 * 
 * The source code of org.codegist.crest.HttpRequest is modified at line 326. There is a bug in getting parameters,
 * causing a NullPointException.
 * 
 * To check Shopify data using its API, The format of a URL is https://ShopifyAPIKey:ShopifyPassword@ShopifyShopName.myshopify.com/admin/...
 */
package com.wms.opensource.shopfast.shopify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.shopify.api.client.ShopifyClient;
import com.shopify.api.credentials.Credential;
import com.shopify.api.endpoints.CollectsService;
import com.shopify.api.endpoints.CountriesService;
import com.shopify.api.endpoints.CustomCollectionsService;
import com.shopify.api.endpoints.ProductsService;
import com.shopify.api.endpoints.SmartCollectionsService;
import com.shopify.api.resources.Collect;
import com.shopify.api.resources.Country;
import com.shopify.api.resources.CustomCollection;
import com.shopify.api.resources.CustomCollectionWithProductsCount;
import com.shopify.api.resources.Image;
import com.shopify.api.resources.Product;
import com.shopify.api.resources.Province;
import com.shopify.api.resources.SmartCollection;
import com.shopify.api.resources.SmartCollectionWithProductsCount;
import com.wms.opensource.shopfast.R;

public class ShopifyUtil {
	
	private static Credential creds = null;
	
	private static ShopifyClient shopify = null;
	
	public static List<Product> getProducts() {		
        ProductsService productsApi = shopify.constructService(ProductsService.class);
        Product[] products = productsApi.getProducts();        
        if(products != null) {
        	return Arrays.asList(products);
        }
        else {
        	return new ArrayList<Product>();
        }
	}
	
	public static List<Collect> getCollects() {
		CollectsService collectsApi = shopify.constructInterface(CollectsService.class);
		return collectsApi.getCollects();
	}
	
	@SuppressWarnings("rawtypes")
	public static List<SmartCollection> getSmartCollections(Context context) {
		if(creds == null) {
			initShopifyClient(context);
		}
		
		SmartCollectionsService smartCollectionsService = shopify.constructService(SmartCollectionsService.class);
		List<SmartCollection> collections = smartCollectionsService.getSmartCollections();
		List<SmartCollection> results = new ArrayList<SmartCollection>();
		for(int i = 0; i < collections.size(); i++) {
			Map map = (Map) collections.get(i);
			SmartCollection collection = new SmartCollection();
			collection.setBodyHtml((String)map.get("body_html"));
			collection.setHandle((String)map.get("handle"));
			collection.setId((Integer)map.get("id"));
			collection.setPublishedAt((String)map.get("published_at"));
			collection.setSortOrder((String)map.get("sort_order"));
			collection.setTemplateSuffix((String)map.get("template_suffix"));
			collection.setTitle((String)map.get("title"));
			collection.setUpdatedAt((String)map.get("updated_at"));
			if(map.containsKey("image")) {
				Map imageObj = (Map) map.get("image");
				Image image = new Image();
				image.setSrc((String) imageObj.get("src"));
				collection.setImage(image);
			}
			else {
				collection.setImage(null);
			}
			results.add(collection);
		}
		return results;		
	}
	
	@SuppressWarnings("rawtypes")
	public static List<CustomCollection> getCustomCollections(Context context) {
		if(creds == null) {
			initShopifyClient(context);
		}

		CustomCollectionsService customCollectionsService = shopify.constructService(CustomCollectionsService.class);
		List<CustomCollection> collections = customCollectionsService.getCustomCollections();
		List<CustomCollection> results = new ArrayList<CustomCollection>();
		for(int i = 0; i < collections.size(); i++) {
			Map map = (Map) collections.get(i);
			CustomCollection collection = new CustomCollection();
			collection.setBodyHtml((String)map.get("body_html"));
			collection.setHandle((String)map.get("handle"));
			collection.setId((Integer)map.get("id"));
			collection.setPublishedAt((String)map.get("published_at"));
			collection.setSortOrder((String)map.get("sort_order"));
			collection.setTemplateSuffix((String)map.get("template_suffix"));
			collection.setTitle((String)map.get("title"));
			collection.setUpdatedAt((String)map.get("updated_at"));
			if(map.containsKey("image")) {
				Map imageObj = (Map) map.get("image");
				Image image = new Image();
				image.setSrc((String) imageObj.get("src"));
				collection.setImage(image);
			}
			else {
				collection.setImage(null);
			}
			results.add(collection);
		}
		return results;
	}
	
	public static int getProductsCountInCollection(Context context, CollectionType collectionType, int collectionID) {
		if(creds == null) {
			initShopifyClient(context);
		}
		
		if(collectionType.equals(CollectionType.CUSTOM_COLLECTION)) {
			CustomCollectionsService customCollectionsService = shopify.constructService(CustomCollectionsService.class);
			CustomCollectionWithProductsCount collection = customCollectionsService.getCustomCollectionWithProductsCount(collectionID);
			return collection.getProductsCount();
		}
		else {
			SmartCollectionsService smartCollectionsService = shopify.constructService(SmartCollectionsService.class);
			SmartCollectionWithProductsCount collection = smartCollectionsService.getSmartCollectionWithProductsCount(collectionID);
			return collection.getProductsCount();			
		}
	}
	
	public static Product getProductByID(Context context, int productID) {
		if(creds == null) {
			initShopifyClient(context);
		}
		
		ProductsService productsApi = shopify.constructService(ProductsService.class);
		Product product = productsApi.getProduct(productID);
		return product;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Country> getCountries() {
		CountriesService countriesService = shopify.constructService(CountriesService.class);
		List<Country> countries = countriesService.getCountries();
		List<Country> results = new ArrayList<Country>();
		for(int i = 0; i < countries.size(); i++) {
			Map map = (Map) countries.get(i);
			Country country = new Country();
			country.setCode((String)map.get("code"));
			country.setId((Integer)map.get("id"));
			country.setName((String)map.get("name"));
			country.setTax((Double)map.get("tax"));
			country.setTaxName((String)map.get("tax_name"));		
			
			List<Map> provincesMap = (List)map.get("provinces");
			List<Province> provinces = new ArrayList<Province>();
			for(Map provinceMap : provincesMap) {
				Province province = new Province();
				province.setCode((String) provinceMap.get("code"));
				province.setName((String) provinceMap.get("name"));
				province.setTax((Double) provinceMap.get("tax"));
				province.setTaxPercentage((Double) provinceMap.get("tax_percentage"));
				province.setTaxName((String) provinceMap.get("tax_name"));
				provinces.add(province);
			}
			country.setProvinces(provinces);
			results.add(country);
		}
		return results;		
	}
	
	private static void initShopifyClient(Context context) {
		creds = new Credential(context.getString(R.string.ShopifyAPIKey), context.getString(R.string.ShopifySharedSecret),
								context.getString(R.string.ShopifyShopName), context.getString(R.string.ShopifyPassword));
		shopify = new ShopifyClient(creds);
	}

}

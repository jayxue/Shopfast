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

package com.wms.opensource.shopfast.shopify;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shopify.api.resources.Country;
import com.shopify.api.resources.Image;
import com.shopify.api.resources.Option;
import com.shopify.api.resources.Product;
import com.shopify.api.resources.Province;
import com.shopify.api.resources.Variant;

public class JSONProcessor {

	public static List<ProductsCountInCollection> getProductsCountsInCollectionsFromJSON(String jsonString) {
		List<ProductsCountInCollection> counts = new ArrayList<ProductsCountInCollection>();
		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				int collectionID = jsonObj.getInt("collectionID");
				int productsCount = jsonObj.getInt("productsCount");
				ProductsCountInCollection count = new ProductsCountInCollection(collectionID, productsCount);
				counts.add(count);
			}
		}
		catch (JSONException e) {

		}
		return counts;
	}
	
	public static String convertProductsCountsInCollectionsToJSON(List<ProductsCountInCollection> counts) {
		JSONArray countsJSONArray = new JSONArray();
		for(ProductsCountInCollection count : counts) {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("collectionID", count.getCollectionID());
				jsonObj.put("productsCount", count.getProductsCount());
				countsJSONArray.put(jsonObj);
			}
			catch (JSONException e) {

			}			
		}
		String countsJSONString = countsJSONArray.toString();
		
		return countsJSONString;
	}
	
	// Including both smart collections and custom collections
	public static List<Collection> getCollectionsFromJSON(String jsonString) {
		List<Collection> collections = new ArrayList<Collection>();
		if(jsonString == null) {
			return collections;
		}
		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				Collection collection = new Collection();
				collection.setId(jsonObj.getInt("id"));
				collection.setBodyHtml(jsonObj.getString("body_html"));
				collection.setTitle(jsonObj.getString("title"));

				if(jsonObj.has("image")) {
					JSONObject imageObj = jsonObj.getJSONObject("image");
					if(imageObj != null) {
						Image image = new Image();
						image.setSrc(imageObj.getString("src"));
						collection.setImage(image);
					}
				}
				else {
					collection.setImage(null);
				}
				collection.setType(CollectionType.valueOf(jsonObj.getString("type")));				
				collections.add(collection);
			}
		} 
		catch (JSONException e) {

		}
		return collections;
	}
	
	// Including both smart collections and custom collections
	public static String convertCollectionsToJSON(List<Collection> collections) {
		JSONArray collectionsJSONArray = new JSONArray();
		for(Collection collection : collections) {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("id", collection.getId());
				if(collection.getBodyHtml() != null) {
					jsonObj.put("body_html", android.text.Html.fromHtml(collection.getBodyHtml()).toString());
				}
				else {
					jsonObj.put("body_html", "");
				}
    			jsonObj.put("title", collection.getTitle());
    			
    			if(collection.getImage() != null) {
	    			JSONObject imageObj = new JSONObject();
	    			imageObj.put("src", collection.getImage().getSrc());
	    			jsonObj.put("image", imageObj);
    			}    			
    			jsonObj.put("type", collection.getType().name());
    			collectionsJSONArray.put(jsonObj);
			} 
			catch (JSONException e) {

			}
		}
		String collectionsJSONString = collectionsJSONArray.toString();		
		return collectionsJSONString;
	}

	private static Variant getVariant(JSONObject variantObj) {
		Variant variant = new Variant();
		try {
			variant.setId(variantObj.getInt("id"));
			if(variantObj.has("option1")) {
				variant.setOption1(variantObj.getString("option1"));
			}
			else {
				variant.setOption1(null);
			}
			if(variantObj.has("option2")) {
				variant.setOption2(variantObj.getString("option2"));
			}
			else {
				variant.setOption2(null);
			}
			if(variantObj.has("option3")) {
				variant.setOption3(variantObj.getString("option3"));
			}
			else {
				variant.setOption3(null);
			}
			if(variantObj.has("price")) {
				variant.setPrice(variantObj.getString("price"));
			}
			if(variantObj.has("position")) {
				variant.setPosition(variantObj.getInt("position"));
			}

		}
		catch (JSONException e) {

		}
		return variant;
	}
	
	// Only get outline (part of) product information to be displayed in products list. For complete product information, use getProductDetailsFromJSON().
	public static List<Product> getProductsOutlineFromJSONArray(JSONArray jsonArray) {
		List<Product> products = new ArrayList<Product>();
		try {
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				Product product = new Product();
				product.setId(jsonObj.getInt("id"));
				
				// Some stores may embed image in product body, so we need to remove it				
				String bodyHtml = jsonObj.getString("body_html");
				String plainBody = HTMLBodyProcessor.convertToPlainText(bodyHtml);
				product.setBodyHtml(plainBody);
				product.setTitle(jsonObj.getString("title"));
				JSONObject imageObj = new JSONObject();
				imageObj = jsonObj.getJSONObject("image");
				Image image = new Image();
				image.setSrc(imageObj.getString("src"));
				product.setImage(image);
				
				// Also need to load product variants in order to show price information in product list
				JSONArray variantsJSONArray = jsonObj.getJSONArray("variants");
				List<Variant> variants = new ArrayList<Variant>();
				for(int j = 0; j < variantsJSONArray.length(); j++) {
					JSONObject variantObj = variantsJSONArray.getJSONObject(j);
					Variant variant = getVariant(variantObj);
					variants.add(variant);				
				}
				product.setVariants(variants);				
				products.add(product);
			}
		}
		catch (JSONException e) {

		}
		return products;
	}
	
	public static List<Product> getProductsFromJSONArrayString(String jsonArrayString) {
		try {
			JSONArray jsonArray = new JSONArray(jsonArrayString);
			return getProductsOutlineFromJSONArray(jsonArray);
		}
		catch (JSONException e) {
			return new ArrayList<Product>();
		}
	}
	
	public static Product getProductDetailsFromJSON(String jsonString) {
		Product product = new Product();
		try {
			JSONObject jsonObj = new JSONObject(jsonString);
			
			product.setId(jsonObj.getInt("id"));
			
			JSONArray imagesJSONArray = jsonObj.getJSONArray("images");
			List<Image> images = new ArrayList<Image>();
			for(int i = 0; i < imagesJSONArray.length(); i++) {
				JSONObject imageObj = imagesJSONArray.getJSONObject(i);
				Image image = new Image();
				image.setId(imageObj.getInt("id"));
				image.setSrc(imageObj.getString("src"));
				images.add(image);
			}
			product.setImages(images);
			
			JSONObject imageObj = jsonObj.getJSONObject("image");
			Image image = new Image();
			image.setId(imageObj.getInt("id"));
			image.setSrc(imageObj.getString("src"));
			product.setImage(image);
			
			product.setTitle(jsonObj.getString("title"));
			
			JSONArray variantsJSONArray = jsonObj.getJSONArray("variants");
			List<Variant> variants = new ArrayList<Variant>();
			for(int i = 0; i < variantsJSONArray.length(); i++) {
				JSONObject variantObj = variantsJSONArray.getJSONObject(i);
				Variant variant = getVariant(variantObj);
				variants.add(variant);				
			}
			product.setVariants(variants);
			
			JSONArray optionsJSONArray = jsonObj.getJSONArray("options");
			List<Option> options = new ArrayList<Option>();
			for(int i = 0; i < optionsJSONArray.length(); i++) {
				JSONObject optionObj = optionsJSONArray.getJSONObject(i);
				Option option = new Option();
				option.setId(optionObj.getInt("id"));
				option.setName(optionObj.getString("name"));
				option.setPosition(optionObj.getInt("position"));
				options.add(option);
			}
			product.setOptions(options);
			
			// Some stores may embed image in product body, so we need to remove it				
			String bodyHtml = jsonObj.getString("body_html");
			String plainBody = HTMLBodyProcessor.convertToPlainText(bodyHtml);
			product.setBodyHtml(plainBody);
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		return product;
	}
	
	public static String convertProductDetailsToJSON(Product product) {
		JSONObject productObj = new JSONObject(); 
		try {
			productObj.put("id", product.getId());
			
			JSONArray imagesJSONArray = new JSONArray();
			for(Image image : product.getImages()) {
				JSONObject imageObj = new JSONObject();
				imageObj.put("id", image.getId());
				imageObj.put("src", image.getSrc());				
				imagesJSONArray.put(imageObj);
			}
			productObj.put("images", imagesJSONArray);
			
			if(product.getImage() != null) {
				JSONObject imageObj = new JSONObject();
				imageObj.put("id", product.getImage().getId());
				imageObj.put("src", product.getImage().getSrc());
				productObj.put("image", imageObj);
			}
			
			productObj.put("title", product.getTitle());
			
			JSONArray variantsJSONArray = new JSONArray();
			for(Variant variant : product.getVariants()) {
				JSONObject variantObj = new JSONObject();
				variantObj.put("id", variant.getId());
				variantObj.put("option1", variant.getOption1());
				variantObj.put("option2", variant.getOption2());
				variantObj.put("option3", variant.getOption3());
				variantObj.put("price", variant.getPrice());
				variantsJSONArray.put(variantObj);
			}
			productObj.put("variants", variantsJSONArray);
			
			JSONArray optionsJSONArray = new JSONArray();
			for(Option option : product.getOptions()) {
				JSONObject optionObj = new JSONObject();
				optionObj.put("id", option.getId());
				optionObj.put("name", option.getName());
				optionObj.put("position", option.getPosition());
				optionsJSONArray.put(optionObj);
			}
			productObj.put("options", optionsJSONArray);
			
			if(product.getBodyHtml() != null) {
				productObj.put("body_html", android.text.Html.fromHtml(product.getBodyHtml()).toString());
			}
			else {
				productObj.put("body_html", "");
			}
   			
		} 
		catch (JSONException e) {

		}
		String productJSONString = productObj.toString();
		return productJSONString;
	}
	
	public static List<Country> getCountriesFromJSON(String jsonString) {
		List<Country> countries = new ArrayList<Country>();
		try {
			JSONObject countriesObj = new JSONObject(jsonString);			
			JSONArray countryArray = countriesObj.getJSONArray("countries");
			for(int i = 0; i < countryArray.length(); i++) {
				JSONObject jsonObj = countryArray.getJSONObject(i);
				Country country = new Country();
				country.setCode(jsonObj.getString("code"));
				country.setId(jsonObj.getInt("id"));
				country.setName(jsonObj.getString("name"));
				country.setTax(jsonObj.getDouble("tax"));
				country.setTaxName(jsonObj.getString("tax_name"));

				JSONArray provincesArray = jsonObj.getJSONArray("provinces");
				List<Province> provinces = new ArrayList<Province>();
				for(int j = 0; j < provincesArray.length(); j++) {
					JSONObject provinceObj = provincesArray.getJSONObject(j);
					Province province = new Province();
					province.setCode(provinceObj.getString("code"));
					province.setName(provinceObj.getString("name"));
					province.setTax(provinceObj.getDouble("tax"));
					province.setTaxPercentage(provinceObj.getDouble("tax_percentage"));
					province.setTaxName(provinceObj.getString("tax_name"));
					provinces.add(province);
				}
				country.setProvinces(provinces);
				countries.add(country);
			}
		} 
		catch (JSONException e) {

		}
		return countries;
	}
	
}

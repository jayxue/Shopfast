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

import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.StorageUtil;

import android.content.Context;

public class Cart {

	private static List<Item> items = new ArrayList<Item>();
	
	public static List<Item> getItems() {
		return items;
	}
	
	public static void addItem(int productID, String productName, String productImageSrc, int variantID, double price, int quantity, String optionName, String option) {
		Item item = new Item();
		item.setProductID(productID);
		item.setProductName(productName);
		item.setProductImageSrc(productImageSrc);
		item.setVariantID(variantID);
		item.setPrice(price);
		item.setQuantity(quantity);
		item.setOptionName(optionName);
		item.setOption(option);
		items.add(item);
	}
	
	public static double getSubtotal() {
		double total = 0;
		for(Item item : items) {
			total += item.getPrice() * item.getQuantity();
		}
		return total;
	}
	
	public static void updateQuantity(int productID, int quantity) {
		for(Item item : items) {
			if(item.getProductID() == productID) {
				item.setQuantity(quantity);
				break;
			}
		}
	}
	
	public static void removeItem(int productID) {
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i).getProductID() == productID) {
				items.remove(i);
				break;
			}
		}
	}
	
	public static void removeItemByVariantID(int variantID) {
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i).getVariantID() == variantID) {
				items.remove(i);
				break;
			}
		}
	}
	
	public static void clear() {
		items.clear();
	}
	
	public static int getItemNum() {
		return items.size();
	}
	
	public static void loadFromJSONFile(Context context) {
    	String cartFilePath = StorageUtil.getTempDirectoryPath(context) + "/" + context.getString(R.string.cartFileName);
    	boolean cartFileExists = FileUtil.fileExist(cartFilePath);
    	if(cartFileExists) {
    		String jsonString = FileUtil.readStringFromFile(StorageUtil.getTempDirectory(context), context.getString(R.string.cartFileName), context.getString(R.string.charSetName));
    		JSONArray jsonArray;
			try {
				jsonArray = new JSONArray(jsonString);
	    		for(int i = 0; i < jsonArray.length(); i++) {
	    			JSONObject jsonObj = jsonArray.getJSONObject(i);
	    			Item item = new Item();
	    			item.setOption(jsonObj.getString("option"));
	    			if(jsonObj.has("optionName")) {
	    				item.setOptionName(jsonObj.getString("optionName"));
	    			}
	    			item.setPrice(jsonObj.getDouble("price"));
	    			item.setProductID(jsonObj.getInt("productID"));
	    			item.setProductImageSrc(jsonObj.getString("productImageSrc"));
	    			item.setProductName(jsonObj.getString("productName"));
	    			item.setQuantity(jsonObj.getInt("quantity"));
	    			item.setVariantID(jsonObj.getInt("variantID"));
	    			items.add(item);
	    		}

			} 
			catch (JSONException e) {
			}
    	}
	}
	
	public static void writeToJSONFile(Context context) {
		JSONArray jsonArray = new JSONArray();
		for(Item item : items) {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("productID", item.getProductID());
				jsonObj.put("productName", item.getProductName());
				jsonObj.put("productImageSrc", item.getProductImageSrc());
				jsonObj.put("variantID", item.getVariantID());
				jsonObj.put("price", item.getPrice());
				jsonObj.put("quantity", item.getQuantity());
				jsonObj.put("option", item.getOption());
				if(item.getOptionName() != null) {
					jsonObj.put("optionName", item.getOptionName());
				}
				jsonArray.put(jsonObj);				
			} 
			catch (JSONException e) {

			}			
		}
		FileUtil.writeStringToFile(jsonArray.toString(), StorageUtil.getTempDirectory(context), context.getString(R.string.cartFileName), context.getString(R.string.charSetName));
	}
	
	public static class Item {
		private int productID = 0;
		private String productName = "";
		private String productImageSrc = "";
		private int variantID = 0;
		private double price = 0.0;
		private int quantity = 0;
		private String optionName = "";		
		private String option = "";
		
		public Item() {}

		public int getProductID() {
			return productID;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}
		
		public String getProductImageSrc() {
			return productImageSrc;
		}

		public void setProductImageSrc(String productImageSrc) {
			this.productImageSrc = productImageSrc;
		}

		public void setProductID(int productID) {
			this.productID = productID;
		}

		public int getVariantID() {
			return variantID;
		}

		public void setVariantID(int variantID) {
			this.variantID = variantID;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		public String getOptionName() {
			return optionName;
		}

		public void setOptionName(String optionName) {
			this.optionName = optionName;
		}

		public String getOption() {
			return option;
		}

		public void setOption(String option) {
			this.option = option;
		}
	}
}

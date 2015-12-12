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

package com.wms.opensource.shopfast.cache;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonCache {

	// Include both smart collections and custom collections
	public static String collectionsJson = null;

	@SuppressLint("UseSparseArrays")
	public static Map<Integer, List<String>> productsOfCollectionsJson = new HashMap<Integer, List<String>>();
	
	@SuppressLint("UseSparseArrays")
	public static Map<Integer, String> productsJson = new HashMap<Integer, String>();
	
	public static String getProductsJsonByCollectionID(int collectionID, int page) {
		List<String> products = productsOfCollectionsJson.get(collectionID);
		if(products != null) {
			if(products.size() > page - 1) {
				return products.get(page - 1);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	
	public static void putProductsJsonOfCollection(int collectionID, int page, String productsJson) {
		List<String> products = productsOfCollectionsJson.get(collectionID);
		if(products == null) {
			products = new ArrayList<String>();
			productsOfCollectionsJson.put(collectionID, products);
		}
		products.add(productsJson);
	}
	
	public static String getProductJsonByID(Integer productID) {
		return productsJson.get(productID);
	}
	
	public static void putProductJson(Integer productID, String productJson) {
		productsJson.put(productID, productJson);
	}

}

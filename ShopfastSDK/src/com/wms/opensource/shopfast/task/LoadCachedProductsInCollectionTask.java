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

package com.wms.opensource.shopfast.task;

import java.util.ArrayList;
import java.util.List;

import com.shopify.api.resources.Product;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.cache.JsonCache;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.shopify.JSONProcessor;
import com.wms.opensource.shopfast.shopify.PersistFileNameProvider;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.MessageUtil;
import com.wms.opensource.shopfast.util.StorageUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

public class LoadCachedProductsInCollectionTask extends AsyncTask<Integer, Void, Void> {

	private Context context = null;
	private ProgressBar progressBar = null;
	private Handler handler = null;
	private int page = 1;
	
	private List<Product> products = new ArrayList<Product>();
	
	private int collectionID = 0;
	
	public LoadCachedProductsInCollectionTask(Context context, Handler handler, ProgressBar progressBar, int page) {
		this.context = context;
		this.handler = handler;
		this.progressBar = progressBar;
		this.page = page;
	}
	
	protected void onPreExecute() {
		progressBar.setVisibility(View.VISIBLE);
    }
	
	protected Void doInBackground(Integer... params) { // params[0] is collection ID
		collectionID = params[0];
		String jsonString = JsonCache.getProductsJsonByCollectionID(collectionID, page);
		if(jsonString == null) {
        	String productsJSONArrayString = FileUtil.readStringFromFile(StorageUtil.getTempDirectory(context),
        			PersistFileNameProvider.getProductsFileName(collectionID, page), context.getString(R.string.charSetName));
        	JsonCache.putProductsJsonOfCollection(collectionID, page, productsJSONArrayString);
        	products = JSONProcessor.getProductsFromJSONArrayString(productsJSONArrayString);
		}
		else {			
			products = JSONProcessor.getProductsFromJSONArrayString(JsonCache.getProductsJsonByCollectionID(collectionID, page));
		}
		
		return null;
	}

	protected void onPostExecute(Void result) {
		progressBar.setVisibility(View.INVISIBLE);
		MessageUtil.sendHandlerMessage(handler, HandlerMessage.CACHED_DATA_LOADED);
	}

	public List<Product> getProducts() {
		return products;
	}
		
}

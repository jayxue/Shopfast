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

import com.shopify.api.resources.CustomCollection;
import com.shopify.api.resources.SmartCollection;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.cache.JsonCache;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.shopify.JSONProcessor;
import com.wms.opensource.shopfast.shopify.Collection;
import com.wms.opensource.shopfast.shopify.ShopifyUtil;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.MessageUtil;
import com.wms.opensource.shopfast.util.StorageUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

public class LoadCollectionsTask extends AsyncTask<Void, Void, Void> {

	private Context context = null;
	private ProgressBar progressBar = null;	
	private Handler handler = null;
	private List<CustomCollection> customCollections = new ArrayList<CustomCollection>();	
	private List<SmartCollection> smartCollections = new ArrayList<SmartCollection>();
	private List<Collection> shopifyCollections = new ArrayList<Collection>();
		
	public LoadCollectionsTask(Context context, Handler handler, ProgressBar progressBar) {
		this.context = context;
		this.handler = handler;
		this.progressBar = progressBar;
	}

	protected void onPreExecute() {
		progressBar.setVisibility(View.VISIBLE);
    }
	
	protected Void doInBackground(Void... params) {
		smartCollections = ShopifyUtil.getSmartCollections(context);
		customCollections = ShopifyUtil.getCustomCollections(context);			
		return null;
	}
	
    protected void onPostExecute(Void result) {
    	progressBar.setVisibility(View.INVISIBLE);
   		for(SmartCollection smartCollection : smartCollections) {
   			shopifyCollections.add(new Collection(smartCollection));
   		}
   		for(CustomCollection customCollection : customCollections) {
   			shopifyCollections.add(new Collection(customCollection));
   		}
    		
   		String collectionsJSONString = JSONProcessor.convertCollectionsToJSON(shopifyCollections);
   		JsonCache.collectionsJson = collectionsJSONString;	// Keep in cache
   		FileUtil.writeStringToFile(collectionsJSONString, StorageUtil.getTempDirectory(context), context.getString(R.string.collectionsFileName), 
    				context.getString(R.string.charSetName));
    		
   		MessageUtil.sendHandlerMessage(handler, HandlerMessage.STORE_COLLECTIONS_LOADED);
    }
    
    public List<Collection> getCollections() {
    	return shopifyCollections;
    }

}

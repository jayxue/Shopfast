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

import java.util.List;

import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.shopify.JSONProcessor;
import com.wms.opensource.shopfast.shopify.ProductsCountInCollection;
import com.wms.opensource.shopfast.shopify.CollectionType;
import com.wms.opensource.shopfast.shopify.ShopifyUtil;
import com.wms.opensource.shopfast.util.DialogUtil;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.MessageUtil;
import com.wms.opensource.shopfast.util.StorageUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

public class LoadProductsCountInCollectionTask extends AsyncTask<Void, Void, Integer> {

	private Context context = null;
	private Handler handler = null;
	private CollectionType collectionType = null;
	private int collectionID = 0;
	
	private ProgressDialog progressDialog = null;
	private int productsCount = 0;

	public LoadProductsCountInCollectionTask(Activity context, Handler handler, CollectionType collectionType, int collectionID) {
		this.context = context;
		this.handler = handler;
		this.collectionType = collectionType;
		this.collectionID = collectionID;
	}
	
	protected void onPreExecute() {
		progressDialog = DialogUtil.showWaitingProgressDialog(context, ProgressDialog.STYLE_SPINNER, context.getString(R.string.loadingProducts), false);    	
    }
	
	@Override
	protected Integer doInBackground(Void... params) {
		productsCount = ShopifyUtil.getProductsCountInCollection(context, collectionType, collectionID);
		return productsCount;
	}

    protected void onPostExecute(Integer result) { 
    	if(progressDialog != null && progressDialog.isShowing() == true) {
    		progressDialog.dismiss();
    	}
    	
		String countsJSONString = FileUtil.readStringFromFile(StorageUtil.getTempDirectory(context), context.getString(R.string.productsCountInCollectionsFileName), 
				context.getString(R.string.charSetName));
		List<ProductsCountInCollection> counts = JSONProcessor.getProductsCountsInCollectionsFromJSON(countsJSONString);
		boolean collectionIDExists = false;
		ProductsCountInCollection count = new ProductsCountInCollection(collectionID, result);
		for(int i = 0; i < counts.size(); i++) {
			ProductsCountInCollection currentCount = counts.get(i);
			if(currentCount.getCollectionID() == collectionID) {
				currentCount.setProductsCount(result);
				collectionIDExists = true;
				break;
			}
		}
		if(!collectionIDExists) {
			counts.add(count);
		}
		countsJSONString = JSONProcessor.convertProductsCountsInCollectionsToJSON(counts);
		FileUtil.writeStringToFile(countsJSONString, StorageUtil.getTempDirectory(context), context.getString(R.string.productsCountInCollectionsFileName), 
				context.getString(R.string.charSetName));
		
		MessageUtil.sendHandlerMessage(handler, HandlerMessage.PRODUCTS_COUNT_LOADED);
    }

	public int getProductsCount() {
		return productsCount;
	}
        
}

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

import com.shopify.api.resources.Product;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.cache.JsonCache;
import com.wms.opensource.shopfast.cache.ProductCache;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.shopify.HTMLBodyProcessor;
import com.wms.opensource.shopfast.shopify.JSONProcessor;
import com.wms.opensource.shopfast.shopify.PersistFileNameProvider;
import com.wms.opensource.shopfast.shopify.ShopifyUtil;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.MessageUtil;
import com.wms.opensource.shopfast.util.StorageUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

public class LoadProductDetailsTask extends AsyncTask<Integer, Void, Void> {

	private Context context = null;
	private Handler handler = null;
	private ProgressBar progressBar = null;	

	private Product product = null;
	
	public LoadProductDetailsTask(Context context, Handler handler, ProgressBar progressBar) {
		this.context = context;
		this.handler = handler;
		this.progressBar = progressBar;
	}
	
	protected void onPreExecute() {
		progressBar.setVisibility(View.VISIBLE);    	
    }
	
	protected Void doInBackground(Integer... params) { // params[0] is the product ID
		product = ShopifyUtil.getProductByID(context, params[0]);
		String bodyHtml = product.getBodyHtml();
		String plainBody = HTMLBodyProcessor.convertToPlainText(bodyHtml);
		product.setBodyHtml(plainBody);
		return null;
	}
	
    protected void onPostExecute(Void result) {
    	progressBar.setVisibility(View.INVISIBLE);
		
		ProductCache.putProduct(product.getId(), product);
    	String productJSONString = JSONProcessor.convertProductDetailsToJSON(product);
		JsonCache.putProductJson(product.getId(), productJSONString);
		FileUtil.writeStringToFile(productJSONString, StorageUtil.getTempDirectory(context), PersistFileNameProvider.getProductDetailsFileName(product.getId()), 
				context.getString(R.string.charSetName));
		
		MessageUtil.sendHandlerMessage(handler, HandlerMessage.STORE_PRODUCT_DETAILS_LOADED);
    }

    public Product getProduct() {
    	return product;
    }
    	
}

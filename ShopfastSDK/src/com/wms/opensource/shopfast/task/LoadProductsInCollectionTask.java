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

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.shopify.api.resources.Product;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.cache.JsonCache;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.shopify.JSONProcessor;
import com.wms.opensource.shopfast.shopify.PersistFileNameProvider;
import com.wms.opensource.shopfast.shopify.Constants;
import com.wms.opensource.shopfast.util.DialogUtil;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.MessageUtil;
import com.wms.opensource.shopfast.util.StorageUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;

public class LoadProductsInCollectionTask extends AsyncTask<Integer, Void, Void> {

	private Context context = null;
	private ProgressBar progressBar = null;
	private int collectionID = 0;
	private int page = 1;
	
	private List<Product> products = null;
	
	private Handler handler = null;
	private String productsArrayString = null;
	
	private String errorString = "";
		
	public LoadProductsInCollectionTask(Context context, Handler handler, ProgressBar progressBar, int page) {
		this.context = context;
		this.handler = handler;
		this.progressBar = progressBar;
		this.page = page;
	}
	
	protected void onPreExecute() {
		progressBar.setVisibility(View.VISIBLE);
    }
	
	protected Void doInBackground(Integer... params) { // params[0] is the collection ID
		collectionID = params[0];

		/** Directly use HTTPClient to load products */			
		String urlString = "https://" + context.getString(R.string.ShopifyShopName) + ".myshopify.com/admin/products.json?collection_id=" + collectionID + "&page=" + page + "&limit=" + Constants.PRODUCTS_PER_PAGE;
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(urlString);			
		try {
		    String authentication = context.getString(R.string.ShopifyAPIKey) + ":" + context.getString(R.string.ShopifyPassword);
		    byte[] byteArray = authentication.getBytes("UTF-8");
		    String base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);
		    get.setHeader("Authorization", "Basic " + base64);
			get.setHeader("User-Agent","Mozilla/5.0 ( compatible ) ");
			get.setHeader("Accept","*/*");
			HttpResponse resp = client.execute(get);
			if (resp.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = resp.getEntity();
				if(entity != null) {
					String jsonString = EntityUtils.toString(entity);
					JSONObject productsObject = new JSONObject(jsonString);
					productsArrayString = productsObject.getString("products");
					products = JSONProcessor.getProductsFromJSONArrayString(productsArrayString);
				}
			} 
			else {
				errorString = "Error: " + resp.getStatusLine();
			}				
		} 
		catch (ClientProtocolException e) {
			errorString = context.getString(R.string.retryOnError);
		} 
		catch (IOException e) {
			errorString = context.getString(R.string.retryOnError);
		} 
		catch (JSONException e) {
			errorString = context.getString(R.string.retryOnError);
		}
		return null;
	}
	
    protected void onPostExecute(Void result) { 
    	progressBar.setVisibility(View.INVISIBLE);
    	if(products == null) {
    		DialogUtil.showExceptionAlertDialog(context, context.getString(R.string.error), errorString);
    	}
    	else {
    		JsonCache.putProductsJsonOfCollection(collectionID, page, productsArrayString);
    		FileUtil.writeStringToFile(productsArrayString, StorageUtil.getTempDirectory(context),
    				PersistFileNameProvider.getProductsFileName(collectionID, page), 
    				context.getString(R.string.charSetName));
    		MessageUtil.sendHandlerMessage(handler, HandlerMessage.STORE_PRODUCTS_LOADED);    		
    	}
    }
    
    public List<Product> getProducts() {
    	return products;
    }
	
}

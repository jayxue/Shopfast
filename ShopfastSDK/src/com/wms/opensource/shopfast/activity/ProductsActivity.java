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

package com.wms.opensource.shopfast.activity;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.PageIndicator;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.adapter.ProductsInCollectionFragmentAdapter;
import com.wms.opensource.shopfast.fragment.ProductsInCollectionFragment;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.shopify.JSONProcessor;
import com.wms.opensource.shopfast.shopify.ProductsCountInCollection;
import com.wms.opensource.shopfast.shopify.CollectionType;
import com.wms.opensource.shopfast.shopify.Constants;
import com.wms.opensource.shopfast.task.LoadProductsCountInCollectionTask;
import com.wms.opensource.shopfast.type.NetworkStatus;
import com.wms.opensource.shopfast.util.ActivityUtil;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.MessageUtil;
import com.wms.opensource.shopfast.util.NetworkUtil;
import com.wms.opensource.shopfast.util.StorageUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

public class ProductsActivity extends SherlockFragmentActivity {

	com.actionbarsherlock.view.Menu menu = null;
		
	private int collectionID = 0;	
	private String collectionTitle = "";
	private CollectionType collectionType = CollectionType.SMART_COLLECTION;
	
	// At least 1 page
	private int pageCount = 1;
	
    private ProductsInCollectionFragmentAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;
    
    private LoadProductsCountInCollectionTask loadProductsCountInCollectionTask = null;
    private LoadProductsCountHandler loadProductsCountHandler = null;
    
    public static int currentPage = 1;
    
    List<Fragment> fragments = new ArrayList<Fragment>();
	
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.products_in_collection);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
        	collectionID = bundle.getInt("collectionID");
        	collectionTitle = bundle.getString("collectionTitle");
        	collectionType = CollectionType.valueOf(bundle.getString("collectionType"));
        }
        
        loadProductsCountHandler = new LoadProductsCountHandler();
        
        boolean productsCountExists = false;
    	String productsCountInCollectionFilePath = StorageUtil.getTempDirectoryPath(this) + "/" + getString(R.string.productsCountInCollectionsFileName);
    	boolean fileExists = FileUtil.fileExist(productsCountInCollectionFilePath);
    	if(fileExists) {
    		String countsJSONString = FileUtil.readStringFromFile(StorageUtil.getTempDirectory(this), getString(R.string.productsCountInCollectionsFileName), 
    				getString(R.string.charSetName));
    		List<ProductsCountInCollection> counts = JSONProcessor.getProductsCountsInCollectionsFromJSON(countsJSONString);
    		for(ProductsCountInCollection count : counts) {
    			if(count.getCollectionID() == collectionID) {
    				pageCount = (int) Math.ceil(count.getProductsCount() * 1.0 / Constants.PRODUCTS_PER_PAGE);
    				productsCountExists = true;
    				break;
    			}
    		}
    	}
    	if(productsCountExists) {
        	MessageUtil.sendHandlerMessage(loadProductsCountHandler, HandlerMessage.PRODUCTS_COUNT_LOADED);
    	}
    	else {        
        	NetworkStatus networkStatus = NetworkUtil.getNetworkConnection(this);
        	if (networkStatus.equals(NetworkStatus.WIFI_CONNECTED) || networkStatus.equals(NetworkStatus.MOBILE_CONNECTED)) {
    	        loadProductsCountInCollectionTask = new LoadProductsCountInCollectionTask(this, loadProductsCountHandler, collectionType, collectionID);
    	        loadProductsCountInCollectionTask.execute();
        	}
        	else {
        		Toast.makeText(this, getString(R.string.noNetworkAvailable), Toast.LENGTH_LONG).show();
        	}
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {		
    	menu.add(getString(R.string.refresh)).setIcon(R.drawable.refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(getString(R.string.cart)).setIcon(R.drawable.cart).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);		
        
		this.menu = menu;
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals(getString(R.string.refresh))) {
        	NetworkStatus networkStatus = NetworkUtil.getNetworkConnection(this);
        	if (networkStatus.equals(NetworkStatus.WIFI_CONNECTED) || networkStatus.equals(NetworkStatus.MOBILE_CONNECTED)) {
        		FileUtil.deleteFilesInDir(StorageUtil.getTempDirectory(this), "ProductsInCollection" + collectionID + "-page");
        		currentPage = 1;
        		mIndicator.setCurrentItem(0);
        		loadProductsCountInCollectionTask = new LoadProductsCountInCollectionTask(this, loadProductsCountHandler, collectionType, collectionID);
    	        loadProductsCountInCollectionTask.execute();
        	}
        	else {
        		Toast.makeText(this, getString(R.string.noNetworkAvailable), Toast.LENGTH_LONG).show();
        	}
			return true;
		}		
		else if (item.getTitle().equals(getString(R.string.cart))) {
			ActivityUtil.goToActivity(this, CartActivity.class);
			return true;
		} 
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("NewApi")
	public void onBackPressed() {
		// If a product image view is visible, we treat backpressed as closing the image
		for(Fragment fragment : fragments) {
			ProductsInCollectionFragment f = (ProductsInCollectionFragment)fragment;
			if(f.getImageViewPreview().getVisibility() == View.VISIBLE) {
				f.getImageViewPreview().setVisibility(View.INVISIBLE);
				return;
			}				
		}
    	super.onBackPressed();
	}
	
    @SuppressLint("HandlerLeak")
	private class LoadProductsCountHandler extends Handler { 	
    	
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		if (msg.what == HandlerMessage.PRODUCTS_COUNT_LOADED) {
    			if(loadProductsCountInCollectionTask != null) {
    				pageCount = (int) Math.ceil(loadProductsCountInCollectionTask.getProductsCount() * 1.0 / Constants.PRODUCTS_PER_PAGE);
    			}
    	        if(pageCount == 0) {
    	        	pageCount = 1;
    	        }
    			mAdapter = new ProductsInCollectionFragmentAdapter(getSupportFragmentManager(), pageCount, collectionID, collectionTitle, ProductsActivity.this);

    	        mPager = (ViewPager)findViewById(R.id.pager);
    	        mPager.setAdapter(mAdapter);

    	        mIndicator = (LinePageIndicator)findViewById(R.id.indicator);
    	        mIndicator.setViewPager(mPager);
    	        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
    	            @Override
    	            public void onPageSelected(int position) {
    	            	currentPage = position + 1;
    	    			int startProduct = (currentPage - 1) * Constants.PRODUCTS_PER_PAGE + 1;
    	    			int endProduct = (currentPage - 1) * Constants.PRODUCTS_PER_PAGE + Constants.PRODUCTS_PER_PAGE;
    	            	setTitle(collectionTitle + " (" + startProduct + " to " + endProduct + ")");
    	            }

    	            @Override
    	            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    	            	
    	            }

    	            @Override
    	            public void onPageScrollStateChanged(int state) {
    	            	
    	            }
    	        });    	        
    		}
    	}
    	
    }
    
    @Override
    public void onAttachFragment (Fragment fragment) {
        fragments.add(fragment);
    }
    
}

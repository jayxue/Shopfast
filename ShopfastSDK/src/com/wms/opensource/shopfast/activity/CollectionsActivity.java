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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.adapter.CollectionArrayAdapter;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.shopify.Collection;
import com.wms.opensource.shopfast.task.LoadCachedCollectionsTask;
import com.wms.opensource.shopfast.task.LoadCollectionsTask;
import com.wms.opensource.shopfast.type.NetworkStatus;
import com.wms.opensource.shopfast.util.ActivityUtil;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.NetworkUtil;
import com.wms.opensource.shopfast.util.StorageUtil;

public class CollectionsActivity extends SherlockActivity {

	com.actionbarsherlock.view.Menu menu = null;
	
	private ProgressBar progressBar = null;
	private ListView listView = null;
	private ImageView imageViewPreview = null;

	List<Collection> collections = new ArrayList<Collection>();
	
	private LoadCollectionsTask loadCollectionsTask = null;
	private LoadCachedCollectionsTask loadCachedCollectionsTask = null;
	
	private LoadCollectionsHandler loadCollectionsHandler = new LoadCollectionsHandler();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.collections);
		
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.INVISIBLE);

		listView = (ListView) findViewById(R.id.listView);
		
		imageViewPreview = (ImageView) findViewById(R.id.imageViewPreview);
		imageViewPreview.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        imageViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageViewPreview.setLayoutParams(imageViewParams);		
		imageViewPreview.setOnClickListener(new ImageView.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Dismiss the image view
				v.setVisibility(View.INVISIBLE);
	    		Animation myAnim = AnimationUtils.loadAnimation(CollectionsActivity.this, R.anim.fadeout);
	    		v.startAnimation(myAnim);
			}
        	
        });		
		
		// Before reloading collections, display collections if they have been saved.
    	String collectionsFilePath = StorageUtil.getTempDirectoryPath(this) + "/" + getString(R.string.collectionsFileName);
    	boolean collectionsFileExists = FileUtil.fileExist(collectionsFilePath);
    	if(collectionsFileExists) {
	        loadCachedCollectionsTask = new LoadCachedCollectionsTask(CollectionsActivity.this, loadCollectionsHandler, progressBar);
	    	loadCachedCollectionsTask.execute();
    	}
    	else {
    		loadCollections();
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
			loadCollections();
			return true;
		}
		else if (item.getTitle().equals(getString(R.string.cart))) {
			gotoCartActivity();
			return true;
		} 
		else {
			return super.onOptionsItemSelected(item);
		}
	}
	
    private void setCollectionList() {		
    	CollectionArrayAdapter collectionArrayAdapter = new CollectionArrayAdapter(this, R.layout.collection_list_item, collections, imageViewPreview, progressBar);
		listView.setAdapter(collectionArrayAdapter);
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Collection collection = collections.get(position);
				Intent intent = new Intent();
				intent.setClass(CollectionsActivity.this, ProductsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("collectionID", collection.getId());
				bundle.putString("collectionTitle", collection.getTitle());
				bundle.putString("collectionType", collection.getType().name());
				intent.putExtras(bundle);
				startActivity(intent);					
			}
			
		});
    }
    
    @SuppressLint("HandlerLeak")
	private class LoadCollectionsHandler extends Handler { 	
    	
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		if (msg.what == HandlerMessage.STORE_COLLECTIONS_LOADED) {
    			collections = loadCollectionsTask.getCollections();
    		}
    		else if(msg.what == HandlerMessage.CACHED_DATA_LOADED) {
    			collections = loadCachedCollectionsTask.getCollections();    			
    		}
    		setCollectionList();
    		setTitle(getString(R.string.app_name) + " - " + getString(R.string.collectionsActivityTitle) + " (" + collections.size() + ")");
    	}
    	
    }
    
    private void loadCollections() {
    	NetworkStatus networkStatus = NetworkUtil.getNetworkConnection(this);
    	if (networkStatus.equals(NetworkStatus.WIFI_CONNECTED) || networkStatus.equals(NetworkStatus.MOBILE_CONNECTED)) {
        	loadCollectionsTask = new LoadCollectionsTask(this, loadCollectionsHandler, progressBar);           
            loadCollectionsTask.execute();
    	}
    	else {
    		Toast.makeText(this, getString(R.string.noNetworkAvailable), Toast.LENGTH_LONG).show();
    	}    	
    }
    
    private void gotoCartActivity() {
    	ActivityUtil.goToActivity(this, CartActivity.class);
    }
    
}

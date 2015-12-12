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
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.androidquery.AQuery;
import com.shopify.api.resources.Image;
import com.shopify.api.resources.Product;
import com.shopify.api.resources.Variant;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.listener.ImageButtonBackgroundSelector;
import com.wms.opensource.shopfast.listener.ProductImageViewOnTouchListener;
import com.wms.opensource.shopfast.shopify.Cart;
import com.wms.opensource.shopfast.shopify.PersistFileNameProvider;
import com.wms.opensource.shopfast.task.LoadCachedProductDetailsTask;
import com.wms.opensource.shopfast.task.LoadProductDetailsTask;
import com.wms.opensource.shopfast.type.NetworkStatus;
import com.wms.opensource.shopfast.util.ActivityUtil;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.NetworkUtil;
import com.wms.opensource.shopfast.util.StorageUtil;
import com.wms.opensource.shopfast.util.ViewUtil;

@SuppressWarnings("deprecation")
public class ProductDetailsActivity extends SherlockActivity {

	private int productID = 0;
	
	private Product product = null;

	private ProgressBar progressBar = null;
	private ImageView imageViewPicture = null;
	private RelativeLayout rootView = null;
	private TextView textViewTitle = null;
	private ImageButton imageButtonAddToCart = null;
	private TextView textViewOptionName = null;
	private Spinner spinnerOptions = null;
	private TextView textViewBody = null;

	private Gallery gallery = null;
	
	private LoadProductDetailsHandler loadProductDetailsHandler = new LoadProductDetailsHandler();
	
	private LoadProductDetailsTask loadProductDetailsTask = null;
	private LoadCachedProductDetailsTask loadCachedProductDetailsTask = null;
	private ProductImageViewOnTouchListener productImageViewOnTouchListener = null;

	private AQuery listAq = null;
	private AQuery aq = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
        	productID = bundle.getInt("productID");
        }
        
        aq = new AQuery(this);
        
        rootView = (RelativeLayout) findViewById(R.id.rootView);
        
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
     
        imageViewPicture = (ImageView) findViewById(R.id.imageViewPicture);
        imageViewPicture.setVisibility(View.INVISIBLE);
        
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        
        imageButtonAddToCart = (ImageButton) findViewById(R.id.imageButtonAddToCart);
        imageButtonAddToCart.setOnTouchListener(new ImageButtonBackgroundSelector());
        imageButtonAddToCart.setOnClickListener(new ImageButtonAddToCartOnClickListener());
        
        textViewOptionName = (TextView) findViewById(R.id.textViewOptionName);
        spinnerOptions = (Spinner) findViewById(R.id.spinnerOptions);
        
        textViewBody = (TextView) findViewById(R.id.textViewBody);
        textViewBody.setMovementMethod(new ScrollingMovementMethod());
        
        gallery = (Gallery) findViewById(R.id.gallery);
        
    	String productDetailsFilePath = StorageUtil.getTempDirectoryPath(this) + "/" + PersistFileNameProvider.getProductDetailsFileName(productID);
    	if(FileUtil.fileExist(productDetailsFilePath)) {
    		loadCachedProductDetailsTask = new LoadCachedProductDetailsTask(this, loadProductDetailsHandler, progressBar);
    		loadCachedProductDetailsTask.execute(productID);
    	}
    	else {
    		loadProductDetails();
    	}
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		menu.add(getString(R.string.refresh)).setIcon(R.drawable.refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(getString(R.string.cart)).setIcon(R.drawable.cart).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);		
        		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals(getString(R.string.refresh))) {
			loadProductDetails();
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
		
	public void onBackPressed() {
		if(imageViewPicture.getVisibility() == View.VISIBLE) {
			imageViewPicture.setVisibility(View.INVISIBLE);
    		Animation myAnim = AnimationUtils.loadAnimation(ProductDetailsActivity.this, R.anim.fadeout);
    		imageViewPicture.startAnimation(myAnim);
			return;
		}
			
    	super.onBackPressed();
	}

	private void loadProductDetails() {
    	NetworkStatus networkStatus = NetworkUtil.getNetworkConnection(this);
    	if (networkStatus.equals(NetworkStatus.WIFI_CONNECTED) || networkStatus.equals(NetworkStatus.MOBILE_CONNECTED)) {
    		loadProductDetailsTask = new LoadProductDetailsTask(this, loadProductDetailsHandler, progressBar);
    		loadProductDetailsTask.execute(productID);
    	}
    	else {
    		Toast.makeText(this, getString(R.string.noNetworkAvailable), Toast.LENGTH_LONG).show();
    	}		
	}

    @SuppressLint("HandlerLeak")
	private class LoadProductDetailsHandler extends Handler { 	
    	
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		if (msg.what == HandlerMessage.STORE_PRODUCT_DETAILS_LOADED) {
    			product = loadProductDetailsTask.getProduct();
    		}
    		else if(msg.what == HandlerMessage.CACHED_DATA_LOADED) {
    			product = loadCachedProductDetailsTask.getProduct();
    		}
			setTitle(product.getTitle());
			
			textViewTitle.setText(product.getTitle());
			String body = android.text.Html.fromHtml(product.getBodyHtml()).toString();
			textViewBody.setText(body);
			
			String[] appOptionArray = getResources().getStringArray(R.array.appMainOptionArray);
			if(appOptionArray.length > 0) {
				String currencyType = getString(R.string.currencyType);
				List<String> appOptions = Arrays.asList(appOptionArray);
				String mainOptionName = product.getOptions().get(0).getName();
				textViewOptionName.setText(mainOptionName);
				// We will use either variants or pre-defined option array to create selections for product details
				boolean useVariants = true;				
				if(product.getVariants().size() > 0) {
					for(Variant variant : product.getVariants()) {
						if(variant.getOption1().equals(mainOptionName)) {
							// Variant is something like "Size" instead of a specific size. In this case, we need to use pre-defined options
							useVariants = false;
							break;
						}
					}
				}
				List<String> options = new ArrayList<String>();
				if(useVariants) {
					for(Variant variant : product.getVariants()) {
						options.add(variant.getOption1() + " -- " + variant.getPrice() + " " + currencyType);
					}
				}
				else { 
					// Same price for all options
					if(product.getVariants().size() == 1) {
						Variant variant = product.getVariants().get(0);
						if(variant.getOption1().equals(mainOptionName)) {
							// For example, option 1 is "Size"
							for(int i = 0; i < appOptions.size(); i++) {
								String option = appOptions.get(i);
								options.add(option + " -- " + variant.getPrice() + " " + currencyType);
							}
						}
					}					
				}
				ViewUtil.setStringsToSpinner(ProductDetailsActivity.this, options, spinnerOptions);
			}
						
        	productImageViewOnTouchListener = new ProductImageViewOnTouchListener(ProductDetailsActivity.this, product, rootView);
        	imageViewPicture.setOnTouchListener(productImageViewOnTouchListener);
        	
    		List<Image> images = product.getImages();
    		listAq = new AQuery(ProductDetailsActivity.this);   		
    		ArrayAdapter<Image> aa = new ArrayAdapter<Image>(ProductDetailsActivity.this, R.layout.gallery_item, images){
    			
    			public View getView(int position, View convertView, ViewGroup parent) {
    				
    				if(convertView == null){
    					convertView = getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
    				}
    				
    				Image image = getItem(position);
    				
    				AQuery aq = listAq.recycle(convertView);
    				   				
    				String picUrl = image.getSrc();
    			
    				if(!aq.shouldDelay(position, convertView, parent, picUrl)){
    					aq.id(R.id.imageView).progress(R.id.progress).image(picUrl, true, true, 100, 0);
    				}
    				else {
    					aq.id(R.id.imageView).clear();
    				}
    				
    				return convertView;
    			}    			
    			
    		};
    		
    		aq.id(R.id.gallery).adapter(aa);
    		gallery.setSelection(images.size() / 2);
    		gallery.setOnItemClickListener(new OnItemClickListener() {

    	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	        	Image image = product.getImages().get(position);

					String pictureURL = image.getSrc();
        			// Try to load large version of the image using Android Query. If no large version, load the original version
        			AQuery aq = new AQuery(ProductDetailsActivity.this);
        			boolean memCache = false;
        			boolean fileCache = true;

        			if(pictureURL.contains(".jpg")) {
        				int dotJPGPosition = pictureURL.indexOf(".jpg");
        				String largerImageURL = pictureURL.substring(0,  dotJPGPosition) + "_1024x1024" + pictureURL.substring(dotJPGPosition); 		
        				aq.id(imageViewPicture).progress(progressBar).image(largerImageURL, memCache, fileCache, 0, 0, null, AQuery.FADE_IN);
        			}
        			else {
        				aq.id(imageViewPicture).progress(progressBar).image(pictureURL, memCache, fileCache, 0, 0, null, AQuery.FADE_IN);
            		}
        			
        			productImageViewOnTouchListener.setStartingPictureURL(pictureURL);
    	        }

    	   });
    	}
    }
    
    private class ImageButtonAddToCartOnClickListener implements ImageButton.OnClickListener {

		@Override
		public void onClick(View v) {
			String[] appOptionArray = getResources().getStringArray(R.array.appMainOptionArray);
			boolean useVariants = true;
			if(appOptionArray.length > 0) {
				String mainOptionName = product.getOptions().get(0).getName();
				if(product.getVariants().size() > 0) {
					for(Variant variant : product.getVariants()) {
						if(variant.getOption1().equals(mainOptionName)) {
							useVariants = false;
							break;
						}
					}
				}
			}
			if(useVariants) {
				Variant variant = product.getVariants().get((int)spinnerOptions.getSelectedItemPosition());
				Cart.addItem(product.getId(), product.getTitle(), product.getImage().getSrc(), variant.getId(), Double.valueOf(variant.getPrice()), 1, null, variant.getOption1());
			}
			else {
				Variant variant = product.getVariants().get(0);
				double price = Double.valueOf(variant.getPrice());
				int optionID = spinnerOptions.getSelectedItemPosition();				
				String option = appOptionArray[optionID];
				String optionName = getString(R.string.mainOptionName);
				Cart.addItem(product.getId(), product.getTitle(), product.getImage().getSrc(), variant.getId(), price, 1, optionName, option);
			}

			Toast.makeText(ProductDetailsActivity.this, "You have successfully added the item to cart (total: " + Cart.getItemNum() + " items). Enjoying your shopping!", Toast.LENGTH_LONG).show();
		}
    }
    
}

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

package com.wms.opensource.shopfast.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.shopify.api.resources.Product;
import com.shopify.api.resources.Variant;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.activity.ProductDetailsActivity;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.listener.ImageViewSrcSelector;
import com.wms.opensource.shopfast.shopify.Constants;
import com.wms.opensource.shopfast.shopify.PersistFileNameProvider;
import com.wms.opensource.shopfast.task.LoadCachedProductsInCollectionTask;
import com.wms.opensource.shopfast.task.LoadProductsInCollectionTask;
import com.wms.opensource.shopfast.type.NetworkStatus;
import com.wms.opensource.shopfast.util.ActivityUtil;
import com.wms.opensource.shopfast.util.FileUtil;
import com.wms.opensource.shopfast.util.StorageUtil;
import com.wms.opensource.shopfast.util.NetworkUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ProductsInCollectionFragment extends Fragment {

	private static final String KEY_CONTENT = "ProductsInCollectionFragment";

	private String mContent = "";
	private int collectionID = 0;
	private String collectionTitle = "";
	private int page = 1;

	private RelativeLayout layout = null;
	private ListView listView = null;
	private ImageView imageViewPreview = null;
	private ProgressBar progressBar = null;
	
	private LoadProductsInCollectionTask loadShopifyProductsTask = null;
	private LoadCachedProductsInCollectionTask loadProductsFromLocalTask = null;
	
	private LoadProductsInCollectionHandler loadProductsHandler = new LoadProductsInCollectionHandler();
	
	private List<Product> products = new ArrayList<Product>();
	
    private AQuery listAq = new AQuery(getActivity());
    private AQuery aq = new AQuery(getActivity());

    public static ProductsInCollectionFragment newInstance(String content, int collectionID, String collectionTitle, int page) {
    	ProductsInCollectionFragment fragment = new ProductsInCollectionFragment();
        fragment.collectionID = collectionID;
        fragment.collectionTitle = collectionTitle;
        fragment.page = page;
        return fragment;        
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if(layout != null) {	
    		ViewGroup parent = (ViewGroup) layout.getParent();    		
    		parent.removeView(layout);
    		return layout;
    	}
    	
    	if(listView == null) {
            layout = new RelativeLayout(getActivity());           
            
	        listView = new ListView(getActivity());
	        
	        layout.addView(listView);
            
            imageViewPreview = new ImageView(getActivity());
            RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            imageViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageViewPreview.setLayoutParams(imageViewParams);
            imageViewPreview.setVisibility(View.INVISIBLE);
            imageViewPreview.setOnClickListener(new ImageView.OnClickListener() {

    			@Override
    			public void onClick(View v) {
    				// Dismiss the image view
    				v.setVisibility(View.INVISIBLE);
    	    		Animation myAnim = AnimationUtils.loadAnimation(ProductsInCollectionFragment.this.getActivity(), R.anim.fadeout);
    	    		v.startAnimation(myAnim);
    			}
            	
            });
            layout.addView(imageViewPreview);
	        
            progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleLarge);

            RelativeLayout.LayoutParams progressBarBarams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            progressBarBarams.addRule(RelativeLayout.CENTER_IN_PARENT);
            progressBar.setLayoutParams(progressBarBarams);
            progressBar.setVisibility(View.INVISIBLE);
            layout.addView(progressBar);

            // Before reloading products, display products if they have been saved before.
	        String productsFilePath = StorageUtil.getTempDirectoryPath(getActivity()) + "/" + PersistFileNameProvider.getProductsFileName(collectionID, page);
	    	boolean productsFileExists = FileUtil.fileExist(productsFilePath);
	    	if(productsFileExists) {
	        	loadProductsFromLocalTask = new LoadCachedProductsInCollectionTask(getActivity(), loadProductsHandler, progressBar, page);
	        	loadProductsFromLocalTask.execute(collectionID);
	    	}
	    	else {
		        NetworkStatus networkStatus = NetworkUtil.getNetworkConnection(getActivity());        
		        if (networkStatus.equals(NetworkStatus.WIFI_CONNECTED) || networkStatus.equals(NetworkStatus.MOBILE_CONNECTED)) {
			       	loadShopifyProductsTask = new LoadProductsInCollectionTask(getActivity(), loadProductsHandler, progressBar, page);
			       	loadShopifyProductsTask.execute(collectionID);
		        }
		    	else {
		    		Toast.makeText(getActivity(), getString(R.string.noNetworkAvailable), Toast.LENGTH_LONG).show();
		    	}
	    	}
    	}    	
        return layout;
    }
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
    
    @SuppressLint("HandlerLeak")
 	private class LoadProductsInCollectionHandler extends Handler { 	
     	
     	public void handleMessage(Message msg) {
     		super.handleMessage(msg);
     		if (msg.what == HandlerMessage.STORE_PRODUCTS_LOADED) {    			
    			products = loadShopifyProductsTask.getProducts();    			     			
     		}
     		else if(msg.what == HandlerMessage.CACHED_DATA_LOADED) {
     			products = loadProductsFromLocalTask.getProducts();
     		}

     		setCollectionList();

 			int startProduct = (page - 1) * Constants.PRODUCTS_PER_PAGE + 1;
 			int endProduct = (page - 1) * Constants.PRODUCTS_PER_PAGE + products.size();    			
 			if(page == 1) {
 				if(getActivity() != null) {
 					if(endProduct > 0) {
 						getActivity().setTitle(collectionTitle + " (" + startProduct + " to " + endProduct + ")");
 					}
 					else {
 						getActivity().setTitle(collectionTitle + " (0 product)");
 					}
 				}
 			}
     	}
     	
    }

    private void setCollectionList() {
    	if(getActivity() != null)
    	{	
	    	ProductArrayAdatper collectionArrayAdapter = new ProductArrayAdatper(getActivity(), R.layout.product_list_item, products);		
			listView.setAdapter(collectionArrayAdapter);
			listView.setOnItemClickListener(new ListView.OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Product product = products.get(position);
					ActivityUtil.goToActivity(getActivity(), ProductDetailsActivity.class, "productID", Integer.valueOf(product.getId()));				
				}
				
			});
    	}
    }
    
    private class ProductArrayAdatper extends ArrayAdapter<Product> {

		public ProductArrayAdatper(Context context, int resource, List<Product> products) {
			super(context, resource, products);
		}
		
		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.product_list_item, null);
			}
			
			final Product product = this.getItem(position);
			
			// Find lowest and highest price
			double lowestPrice = Double.MAX_VALUE;
			double highestPrice = 0;
			for(Variant variant : product.getVariants()) {
				double price = Double.valueOf(variant.getPrice()) ;
				if(price < lowestPrice) {
					lowestPrice = price;
				}
				else if(price >= highestPrice) {
					highestPrice = price;
				}
			}
			
			String url = product.getImage().getSrc();
			aq = listAq.recycle(convertView);
			ImageOptions options = new ImageOptions();
			options.round = 5;
			options.ratio = AQuery.RATIO_PRESERVE;
			options.memCache = true;
			options.fileCache = true;
			options.targetWidth = 60;
			aq.id(R.id.imageViewRepresentativeImage).progress(R.id.progressBar).image(url, options);
		
			ImageView imageViewRepresentativeImage = (ImageView) convertView.findViewById(R.id.imageViewRepresentativeImage);
			imageViewRepresentativeImage.setOnClickListener(new ImageView.OnClickListener() {

				@Override
				public void onClick(View v) {
					String pictureURL = product.getImage().getSrc();
        			// Try to load large version of the image using Android Query. If no large version, load the original version
        			AQuery aq = new AQuery(getActivity());
        			boolean memCache = false;
        			boolean fileCache = true;

        			if(pictureURL.contains(".jpg")) {
        				int dotJPGPosition = pictureURL.indexOf(".jpg");
        				String largerImageURL = pictureURL.substring(0,  dotJPGPosition) + "_1024x1024" + pictureURL.substring(dotJPGPosition); 		
        				aq.id(imageViewPreview).progress(progressBar).image(largerImageURL, memCache, fileCache, 0, 0, null, AQuery.FADE_IN);
        			}
        			else {
        				aq.id(imageViewPreview).progress(progressBar).image(pictureURL, memCache, fileCache, 0, 0, null, AQuery.FADE_IN);
            		}					
				}
				
			});
			imageViewRepresentativeImage.setOnTouchListener(new ImageViewSrcSelector());
			
			DecimalFormat df = new DecimalFormat("0.00##");
			TextView textViewTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
        	if(lowestPrice == 0) {
        		textViewTitle.setText(product.getTitle() + "  $" + df.format(highestPrice));
        	}
        	else if(highestPrice > lowestPrice) {
        		textViewTitle.setText(product.getTitle() + "  $" + df.format(lowestPrice) + " ~ &"+ df.format(highestPrice));
        	}
        	else {
        		textViewTitle.setText(product.getTitle() + "  $" + df.format(lowestPrice));
        	}
			
			TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
			String body = android.text.Html.fromHtml(product.getBodyHtml()).toString();
			
			if(body.length() > 255) {
				body = body.substring(0, 254) + "...";
			}

			textViewDescription.setText(body);
			
			return convertView;
		}
    }
    
    public ImageView getImageViewPreview() {
    	return imageViewPreview;
    }
    
}

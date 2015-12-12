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

package com.wms.opensource.shopfast.listener;

import com.androidquery.AQuery;
import com.shopify.api.resources.Product;
import com.wms.opensource.shopfast.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class ProductImageViewOnTouchListener implements OnTouchListener {

	private int swipingIndex = -1;
	private Context context = null;
	private Product product = null;
	private String startingPictureURL = "";
	private View rootView = null;
	
	private float downX, upX;
	
	static final int MIN_DISTANCE = 100;
	private long downTime = 0;
	
	public ProductImageViewOnTouchListener(Context context, Product product, View rootView) {
		this.context = context;
		this.product = product;		
		this.rootView = rootView;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onTouch(View v, MotionEvent event) {
	    switch(event.getAction()) {
	    	case MotionEvent.ACTION_DOWN: {
	    		downX = event.getX();
	    		downTime = System.currentTimeMillis();
	    		return true;
	    	}
	    	case MotionEvent.ACTION_UP: {
	    		upX = event.getX();
	    		long upTime = System.currentTimeMillis();
	    		if(upTime - downTime > 750) {
	    			// Long pressing
					return true;		        			
	    		}
	    		
	    		float deltaX = downX - upX;
	
	    		// swipe horizontal?
	    		boolean leftToRight = true;
	    		if(Math.abs(deltaX) > MIN_DISTANCE) {
	    			// left or right
	    			if(deltaX < 0) {
	    				if(swipingIndex > 0) {
	    					swipingIndex--;
	    				}
	    				else {
	    					// Already at the beginning, no need to reload image
	    					return true;
	    				}
	    			}
	    			else if(deltaX > 0) { 
	    				if(swipingIndex < product.getImages().size() - 1) {
	    					swipingIndex++;
	    					leftToRight = false;
	    				}
	    				else {
	    					// Already at the end, no need to reload image
	    					return true;
	    				}
	   				}
        			// Try to load large version of the image using Android Query. If no large version, load the original version
        			final AQuery aq = new AQuery((Activity) context, rootView);
        			final boolean memCache = false;
        			final boolean fileCache = true;
        			final String pictureURL = product.getImages().get(swipingIndex).getSrc();
        			if(pictureURL.contains(".jpg")) {
        				int dotJPGPosition = pictureURL.indexOf(".jpg");
        				final String largerImageURL = pictureURL.substring(0,  dotJPGPosition) + "_1024x1024" + pictureURL.substring(dotJPGPosition);
        				if(leftToRight) {
        					aq.id(R.id.imageViewPicture).animate(R.anim.slide_out_right, new AnimationListener() {

								@Override
								public void onAnimationEnd(Animation arg0) {
									aq.id(R.id.imageViewPicture).progress(R.id.progress).image(largerImageURL, memCache, fileCache, 0, 0, null, R.anim.slide_in_left);		
								}

								@Override
								public void onAnimationRepeat(Animation arg0) {
								}

								@Override
								public void onAnimationStart(Animation arg0) {
								}
        						
        					});
        					
        				}
        				else {
        					aq.id(R.id.imageViewPicture).animate(R.anim.slide_out_left, new AnimationListener() {

								@Override
								public void onAnimationEnd(Animation animation) {
									aq.id(R.id.imageViewPicture).progress(R.id.progress).image(largerImageURL, memCache, fileCache, 0, 0, null, R.anim.slide_in_right);										
								}

								@Override
								public void onAnimationRepeat(Animation animation) {									
								}

								@Override
								public void onAnimationStart(Animation animation) {										
								}
        						
        					});            					
        				}
        			}
        			else {
        				if(leftToRight) {
        					aq.id(R.id.imageViewPicture).animate(R.anim.slide_out_right, new AnimationListener() {

								@Override
								public void onAnimationEnd(Animation arg0) {
									aq.id(R.id.imageViewPicture).progress(R.id.progress).image(pictureURL, memCache, fileCache, 0, 0, null, R.anim.slide_in_left);		
								}

								@Override
								public void onAnimationRepeat(Animation arg0) {
								}

								@Override
								public void onAnimationStart(Animation arg0) {
								}
        						
        					});
        					
        				}
        				else {
        					aq.id(R.id.imageViewPicture).animate(R.anim.slide_out_left, new AnimationListener() {

								@Override
								public void onAnimationEnd(Animation animation) {
									aq.id(R.id.imageViewPicture).progress(R.id.progress).image(pictureURL, memCache, fileCache, 0, 0, null, R.anim.slide_in_right);										
								}

								@Override
								public void onAnimationRepeat(Animation animation) {									
								}

								@Override
								public void onAnimationStart(Animation animation) {										
								}
        						
        					});
        					
        				}
        			}
	   				return true; 
	    		}
	    		else {
	                // It's actually click
					v.setVisibility(View.INVISIBLE);	
	        		Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.fadeout);
	        		v.startAnimation(myAnim);
	                return false;
	    		}	
	    	}
	    }

	    return false;		
	}

	private int findPicturePositionInProduct(String pictureURL) {
		for(int i = 0; i < product.getImages().size(); i++) {
			String src = product.getImages().get(i).getSrc();
			if(src.equals(pictureURL)) {
				return i;
			}
		}
		return 0;
	}

	public void setStartingPictureURL(String startingPictureURL) {
		this.startingPictureURL = startingPictureURL;
		swipingIndex = findPicturePositionInProduct(this.startingPictureURL);
	}

}

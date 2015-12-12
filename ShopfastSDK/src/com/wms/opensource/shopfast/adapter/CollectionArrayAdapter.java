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

package com.wms.opensource.shopfast.adapter;

import java.util.List;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.listener.ImageViewSrcSelector;
import com.wms.opensource.shopfast.shopify.Collection;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CollectionArrayAdapter extends ArrayAdapter<Collection> {

	private ImageView imageViewPreview = null;
	private ProgressBar progressBar = null;
	
	public CollectionArrayAdapter(Context context, int resource, List<Collection> collections, ImageView imageViewPreview, ProgressBar progressBar) {
		super(context, resource, collections);
		this.imageViewPreview = imageViewPreview;
		this.progressBar = progressBar;
	}
	
	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null) {
			convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.collection_list_item, null);
		}
		
		final Collection collection = this.getItem(position);
		String url = "";
    	if(collection.getImage() != null) {
    		url = collection.getImage().getSrc();
    	}
    	else {
    		url = getContext().getString(R.string.placeHolderImage);
    	}
		ImageOptions options = new ImageOptions();
		options.round = 5;
		options.ratio = AQuery.RATIO_PRESERVE;
		options.memCache = true;
		options.fileCache = true;
		options.targetWidth = 60;
		
		AQuery listAq = new AQuery(getContext());
		AQuery aq = listAq.recycle(convertView);    			
		aq.id(R.id.imageViewRepresentativeImage).progress(R.id.progressBar).image(url, options);
	
		ImageView imageViewRepresentativeImage = (ImageView) convertView.findViewById(R.id.imageViewRepresentativeImage);
		imageViewRepresentativeImage.setOnClickListener(new ImageView.OnClickListener() {

			@Override
			public void onClick(View v) {
				String pictureURL = "";
	        	if(collection.getImage() != null) {
	        		pictureURL = collection.getImage().getSrc();
	        	}
	        	else {
	        		pictureURL = getContext().getString(R.string.placeHolderImage);
	        	}
    			// Try to load large version of the image using Android Query. If no large version, load the original version
    			AQuery aq = new AQuery(getContext());
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
		
		TextView textViewTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
		textViewTitle.setText(collection.getTitle());
		
		TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
		String body = "";
		if(collection.getBodyHtml() != null) {
			body = android.text.Html.fromHtml(collection.getBodyHtml()).toString();
		}
		textViewDescription.setText(body);
		
		return convertView;
	}
	
}

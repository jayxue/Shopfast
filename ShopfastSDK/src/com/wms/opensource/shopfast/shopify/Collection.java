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

/**
 * This class unifies SmartCollection and CustomCollection in Shopify.
 */
package com.wms.opensource.shopfast.shopify;

import com.shopify.api.resources.CustomCollection;
import com.shopify.api.resources.Image;
import com.shopify.api.resources.SmartCollection;

public class Collection {

	private String bodyHtml;
	private Image image;
	private String title;
	private int id;
	private CollectionType type = null;
	
	public Collection() {
		
	}
	
	public Collection(SmartCollection smartCollection) {
		this.bodyHtml = smartCollection.getBodyHtml();
		this.image = smartCollection.getImage();
		this.title = smartCollection.getTitle();
		this.id = smartCollection.getId();
		this.type = CollectionType.SMART_COLLECTION;
	}

	public Collection(CustomCollection customCollection) {
		this.bodyHtml = customCollection.getBodyHtml();
		this.image = customCollection.getImage();
		this.title = customCollection.getTitle();
		this.id = customCollection.getId();		
		this.type = CollectionType.CUSTOM_COLLECTION;
	}
	
	public String getBodyHtml() {
		return bodyHtml;
	}

	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public CollectionType getType() {
		return type;
	}

	public void setType(CollectionType type) {
		this.type = type;
	}
		
}

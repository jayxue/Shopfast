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
 * The product description may be encoded with HTML and needs to be converted to plain text.
 */
package com.wms.opensource.shopfast.shopify;

public class HTMLBodyProcessor {

	public static String convertToPlainText(String bodyHtml) {
		String plainBody = bodyHtml;
		if(plainBody.contains("<img")) {
			plainBody = filterImageTag(plainBody);
		}
		plainBody = android.text.Html.fromHtml(plainBody).toString();
		return plainBody;
	}
	
	public static String filterImageTag(String bodyHtml) {
		int imgStartPosition = bodyHtml.indexOf("<img");
		String preImgHtml = bodyHtml.substring(0, imgStartPosition);
		String imgHtml = bodyHtml.substring(imgStartPosition + 1);
		int imgEndPosition = imgHtml.indexOf(">") + 1;
		return preImgHtml + imgHtml.substring(imgEndPosition);
	}

}

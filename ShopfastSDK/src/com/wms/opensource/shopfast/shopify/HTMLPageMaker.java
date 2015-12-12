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

package com.wms.opensource.shopfast.shopify;

import java.text.DecimalFormat;
import java.util.List;

import com.wms.opensource.shopfast.shopify.Cart.Item;
import com.wms.opensource.shopfast.util.FileUtil;

import android.content.Context;

public class HTMLPageMaker {

	public static String getCartSection(Context context) {
		StringBuilder builder = new StringBuilder();
        builder.append("          <div class=\"product\">\n");
        builder.append("            <div id=\"product_form\">\n");
        builder.append("                  <form id=\"cartform\" method=\"post\" action=\"http://www.solerebelsfootwear.co/cart\">");
        builder.append("                    <table>\n");
        List<Item> items = Cart.getItems();
        DecimalFormat df = new DecimalFormat("0.00##");
        for(int i = 0; i < items.size(); i++) {
        	Item item = items.get(i);        	
			builder.append("                  <tr>\n");
			builder.append("                    <td>\n");
			builder.append("                      <div class=\"product_img\"><a href=\"view_product?productID=" + item.getProductID() + "\"><img src=\"" + item.getProductImageSrc() + "\" width=\"70\"/></a></div>\n");
			builder.append("                    </td>\n");
			builder.append("                    <td>\n");
			builder.append("                      <b>" + item.getProductName() + "</b>\n");
			if(item.getOption() != null) {
				builder.append("<br/>" + item.getOption() + "\n");
			}
			builder.append("                    </td>\n");
			builder.append("                    <td>\n");
			builder.append("                      " + df.format(item.getPrice()) + "\n");
			builder.append("                    </td>\n");
			builder.append("                    <td>\n");
			builder.append("                      <div class=\"button\" style=\"width: 60px; font-size: 14px;\">\n");
			builder.append("                        <a href=\"remove?variantID=" + item.getVariantID() + "\">Remove</a>\n");
			builder.append("                      </div>\n");
			builder.append("                    </td>\n");
			builder.append("                  </tr>");
		}
        builder.append("                    </table>\n");
        builder.append("                  </form>\n");
		builder.append("            </div>\n");
		builder.append("          </div>\n");		
		
		return builder.toString();
	}

	public static String getPageHead(Context context) {
		String css = FileUtil.loadAsset("android.css", context);
		StringBuilder builder = new StringBuilder();
		builder.append("  <head>\n");						    
		builder.append("    <meta name=\"viewport\" content=\"width=device-width, user-scalable=no\" />\n");
		builder.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");	
		builder.append("	   <style>\n");
		builder.append(css);	
		builder.append("    </style>\n");
		builder.append("  </head>\n\n");		
		return builder.toString();
	}
	
	public static String getFullHTML(Context context, String pageTitle, String itemsBody, boolean needFormDataProcessingJS, String backgroundColor) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		builder.append(getPageHead(context));		
		
		if(backgroundColor == null) {
			builder.append("  <body>\n");
		}
		else {
			builder.append("  <body style=\"background-color: " + backgroundColor + ";\">");
		}
		builder.append("    <div id=\"container\">\n");		
		builder.append(itemsBody);
		builder.append("      <div style=\"height: 10px;\"></div>");
		builder.append("    </div>\n");
		
		if(needFormDataProcessingJS == true) {
			builder.append("    <script type=\"text/javascript\">\n");
			builder.append(getFormDataProcessingJS(context));
			builder.append("    </script>\n");
		}
		builder.append("  </body>\n");
		builder.append("</html>\n");
		
		return builder.toString();
		
	}
	
	
	private static String getFormDataProcessingJS(Context context) {
		return FileUtil.loadAsset("form_data.js", context);
	}
}

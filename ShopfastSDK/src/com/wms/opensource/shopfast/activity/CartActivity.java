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

import java.text.DecimalFormat;
import java.util.List;

import com.wms.opensource.shopfast.R;
import com.wms.opensource.shopfast.config.PreferenceItemNames;
import com.wms.opensource.shopfast.handler.HandlerMessage;
import com.wms.opensource.shopfast.shopify.Cart;
import com.wms.opensource.shopfast.shopify.TaxManager;
import com.wms.opensource.shopfast.task.LoadCartTask;
import com.wms.opensource.shopfast.util.ActivityUtil;
import com.wms.opensource.shopfast.util.MessageUtil;
import com.wms.opensource.shopfast.util.SharedPreferenceUtil;
import com.wms.opensource.shopfast.util.ViewUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class CartActivity extends Activity {

	private WebView webViewCart = null;
	private ProgressBar progressBarCart = null;
	private TextView textViewItemNumber = null;
	private TextView textViewTotal = null;
	private TextView textViewShippingCost = null;
	
	private Spinner spinnerCountries = null;
	private Spinner spinnerProvinces = null;
	private TextView textViewTax = null;
	
	private String countryName = "";
	private String provinceName = "";
	private String taxName = "";
	private double tax = -1;
	
	private LoadCartTask loadCartTask = null;
	private Handler myHandler = null;
	
	@SuppressLint("HandlerLeak")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_cart);
        
        textViewItemNumber = (TextView) findViewById(R.id.textViewItemNumber);
        textViewTotal = (TextView) findViewById(R.id.textViewTotal);
        textViewShippingCost = (TextView) findViewById(R.id.textViewShippingCost);
        textViewShippingCost.setText(getString(R.string.shippingCost) + " " + getString(R.string.currencyType));
        
        progressBarCart = (ProgressBar) (ProgressBar) findViewById(R.id.progressBarCart);
        progressBarCart.setVisibility(View.INVISIBLE);
        
        webViewCart = (WebView) findViewById(R.id.webViewCart);
        webViewCart.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webViewCart.setWebViewClient(new WebViewClient(){
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("remove")) {
                	int variantIDStartingPosition = url.indexOf("variantID") + 10;
                	int variantID = Integer.valueOf(url.substring(variantIDStartingPosition, url.length()));
                	Cart.removeItemByVariantID(variantID);
                	MessageUtil.sendHandlerMessage(myHandler, HandlerMessage.UPDATE_CART_STATUS);
                	loadCartTask = new LoadCartTask(CartActivity.this, webViewCart);
                    loadCartTask.execute();                    
                	return true;
                }
                else if(url.contains("view_product")){
                	if(url.indexOf("productID") > 0) {
                		int productIDStartingPosition = url.indexOf("productID") + 10;
                		String productID = url.substring(productIDStartingPosition, url.length());
                		ActivityUtil.goToActivity(CartActivity.this, ProductDetailsActivity.class, "productID", Integer.valueOf(productID));
                		CartActivity.this.finish();
                	}   
                	return true;
                }
                else
                	return super.shouldOverrideUrlLoading(view, url);
            }       
        });
        webViewCart.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if(progress == 100) {
                	
                }
            }
        });	 
        
        Cart.loadFromJSONFile(this);
        
        setupTaxControls();
        
		myHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
					case HandlerMessage.CART_LOADED:						
						break;
      				case HandlerMessage.UPDATE_CART_STATUS:
      					updateItemNumber();
      					updateItemTotal();
      					break;
      				default:
      					break;
				}
			}
		};

    }
    
    public void onResume() {
    	MessageUtil.sendHandlerMessage(myHandler, HandlerMessage.UPDATE_CART_STATUS);
    	loadCartTask = new LoadCartTask(CartActivity.this, webViewCart);
        loadCartTask.execute();     
        super.onResume();
    }
    
    public void onDestroy() {
		Cart.writeToJSONFile(this);
		Cart.clear();
		super.onDestroy();
    }
    
	private void updateItemNumber() {
		textViewItemNumber.setText(getString(R.string.itemsInCart) + " " + Cart.getItemNum());
	}

	private void updateItemTotal() {		
		double totalAfterTax = 0;
		if(tax >= 0) {
			totalAfterTax = (1 + tax) * Cart.getSubtotal();
		}
		else {
			totalAfterTax = Cart.getSubtotal();
		}
		DecimalFormat df = new DecimalFormat("0.00");
		textViewTotal.setText(getString(R.string.itemTotal) + " " + df.format(totalAfterTax) + " " + getString(R.string.currencyType));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupTaxControls() {
		textViewTax = (TextView) findViewById(R.id.textViewTax);
		
		spinnerProvinces = (Spinner) findViewById(R.id.spinnerProvinces);
		spinnerProvinces.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View selectedItemView,
					int position, long id) {
				provinceName = spinnerProvinces.getItemAtPosition(position).toString();
				taxName = TaxManager.getTaxName(countryName, provinceName);
				tax = TaxManager.getTax(countryName, provinceName);
				DecimalFormat df = new DecimalFormat("0.00##");
				String tatalAfterTax = df.format(tax * 100);
				textViewTax.setText(getString(R.string.tax) + " " + taxName + " " + tatalAfterTax + "%");
				
				SharedPreferenceUtil.savePreferenceItemByName(CartActivity.this, PreferenceItemNames.selectedCountryName, countryName);
				SharedPreferenceUtil.savePreferenceItemByName(CartActivity.this, PreferenceItemNames.selectedProvinceName, provinceName);
				SharedPreferenceUtil.savePreferenceItemByName(CartActivity.this, PreferenceItemNames.taxName, taxName);
				SharedPreferenceUtil.savePreferenceItemByName(CartActivity.this, PreferenceItemNames.tax, tax + "");
				
				updateItemTotal();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		
		spinnerCountries = (Spinner) findViewById(R.id.spinnerCountries);
		List<String> countryNames = TaxManager.getCountryNames(this);
		ViewUtil.setStringsToSpinner(this, countryNames, spinnerCountries);
		spinnerCountries.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View selectedItemView,
					int position, long id) {
				countryName = spinnerCountries.getItemAtPosition(position).toString();
				List<String> provinceNames = TaxManager.getProvinceNames(countryName); 
				ViewUtil.setStringsToSpinner(CartActivity.this, provinceNames, spinnerProvinces);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		spinnerCountries.setSelection(-1);
		
		countryName = SharedPreferenceUtil.getPreferenceItemByName(this, PreferenceItemNames.selectedCountryName);
		if(!countryName.isEmpty()) {
			spinnerCountries.setSelection(((ArrayAdapter)spinnerCountries.getAdapter()).getPosition(countryName), true);
			List<String> provinceNames = TaxManager.getProvinceNames(countryName);
			ViewUtil.setStringsToSpinner(this, provinceNames, spinnerProvinces);
			provinceName = SharedPreferenceUtil.getPreferenceItemByName(this, PreferenceItemNames.selectedProvinceName);
			spinnerProvinces.setSelection(((ArrayAdapter)spinnerProvinces.getAdapter()).getPosition(provinceName), true);
			taxName = SharedPreferenceUtil.getPreferenceItemByName(this, PreferenceItemNames.taxName);
			tax = Double.valueOf(SharedPreferenceUtil.getPreferenceItemByName(this, PreferenceItemNames.tax));
			textViewTax.setText(getString(R.string.tax) + " " + taxName + " " + (tax * 100) + "%");
		}
	}

}

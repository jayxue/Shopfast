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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.shopify.api.resources.Country;
import com.shopify.api.resources.Province;
import com.wms.opensource.shopfast.util.FileUtil;

public class TaxManager {

	private static List<Country> countries = new ArrayList<Country>();
	
	private static List<String> countryNames = new ArrayList<String>();
	
	private static Map<String, List<Province>> provinces = new HashMap<String, List<Province>>();	// <CountryName, Provinces>
	
	public static List<Country> getCountries(Context context) {
		if(countries.size() == 0) {
			String jsonString = FileUtil.loadAsset("countries.json", context);
			countries = JSONProcessor.getCountriesFromJSON(jsonString); 
		}
		return countries;
	}
	
	
	public static List<String> getCountryNames(Context context) {
		if(countryNames.size() == 0) {
			List<Country> countries = getCountries(context);
			for(Country country : countries) {
				countryNames.add(country.getName());
			}
		}
		return countryNames;
	}
	
	public static List<Province> getProvinces(String countryName) {
		if(!provinces.containsKey(countryName)) {
			for(Country country : countries) {
				if(country.getName().equals(countryName)) {
					provinces.put(countryName, country.getProvinces());
					break;
				}
			}
		}
		return provinces.get(countryName);
	}
	
	public static List<String> getProvinceNames(String countryName) {
		List<String> results = new ArrayList<String>();
		List<Province> myProvinces = getProvinces(countryName);
		for(Province province : myProvinces) {
			results.add(province.getName());
		}
		return results;
	}
	
	public static String getTaxName(String countryName, String provinceName) {
		List<Province> myProvinces = getProvinces(countryName);
		for(Province province : myProvinces) {
			if(province.getName().equals(provinceName)) {
				return province.getTaxName();
			}
		}
		return "";
	}
	
	public static double getTax(String countryName, String provinceName) {
		List<Province> myProvinces = getProvinces(countryName);
		for(Province province : myProvinces) {
			if(province.getName().equals(provinceName)) {
				return province.getTax();
			}
		}
		return 0;
	}
	
}

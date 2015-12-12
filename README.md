# Shopfast - An Andriod SDK to build custom apps for online stores supported by Shopify platform.

Shopfast is an Android SDK that helps develop custom apps for online stores supported by Shopify (http://www.shopify.com/), a fully managed commerce platform for online businesses.

![Demo Screenshot 1](https://github.com/jayxue/Shopfast/blob/master/ShopfastSDK/res/raw/screenshot_1.png)
![Demo Screenshot 2](https://github.com/jayxue/Shopfast/blob/master/ShopfastSDK/res/raw/screenshot_2.png)
![Demo Screenshot 3](https://github.com/jayxue/Shopfast/blob/master/ShopfastSDK/res/raw/screenshot_3.png)
![Demo Screenshot 4](https://github.com/jayxue/Shopfast/blob/master/ShopfastSDK/res/raw/screenshot_4.png)
![Demo Screenshot 5](https://github.com/jayxue/Shopfast/blob/master/ShopfastSDK/res/raw/screenshot_5.png)

Details
-------
Shopify is a fully managed commerce platform that helps establish online businesses and provides retail point-of-sale systems for both online and offline companies. Shopify's core features include the ability to manage products, inventory, customers, orders and discounts (https://en.wikipedia.org/wiki/Shopify). Today, 175,000 active stores are making $10 billion worth of sales through Shopify (http://www.shopify.com/about).

Shopfast is a SDK for developing Android applications that consume Shopify platform services. It wraps service calls and data structures and makes integration dramatically simple.

The SDK works as an application SDK, meaning that you can include it as a library, change some configurations, and get a fully working Android application without writing any code. Of course, you can integrate components it provides into your application and build richer functionality for end users.

Shopfast SDK provides the following features (for a Shopify store which API information has been properly configured in an Android application):
* Browse smart collections and custom collections.
* Preview collection description and representative image.
* Browse products in a collection on multiple pages.
* Preview product description and representative image.
* Browse product details.
* Browse product images in gallery.
* Swipe to review previous/next image.
* Browse prices of various product options such as sizes.
* Add product to shopping cart.
* Review shopping cart.
* Remove product from shopping cart.
* Select country, province and tax to calculate total.

Comparing to many stores' mobile-friendly Web sites, native applications developed using the Shopfast SDK have higher performance, richer functionality, smoother UI and better flexibility, thus provide a much enhanced user experience.

The SDK also serves as a tutorial for Android application development. Besides fundamental mechanisms about UI, activity, adapter, asynchronous task, message, handler, etc., it also demonstrates:
* Making direct HTTP requests using HttpClient.
* Using HTML, JavaScript, CSS with WebView to build embedded Web application.
* JSON data processing.
* And more...

Usage
-----

In order to utilize Shopfast SDK, you just need to do some configurations without writing any code.
* Import all the three projects into workspace: actionbarsherlock, AndroidViewPageIndicator and ShopfastSDK.
* In your application, include ```ShopfastSDK``` as library.
* In your application's ```AndroidManifest.xml```, make sure that you have the following permissions:
  * ```android.permission.INTERNET```
  * ```android.permission.ACCESS_NETWORK_STATE```
  * ```android.permission.ACCESS_WIFI_STATE```
  * ```android.permission.WRITE_EXTERNAL_STORAGE```
  * ```com.android.launcher.permission.INSTALL_SHORTCUT```
* In your application's ```AndroidManifest.xml```, include the following activities:
  * ```com.wms.opensource.shopfast.activity.CollectionsActivity```
  * ```com.wms.opensource.shopfast.activity.ProductsActivity```
  * ```com.wms.opensource.shopfast.activity.ProductDetailsActivity```
  * ```com.wms.opensource.shopfast.activity.CartActivity``` 
* In your application's ```res/values/strings.xml```,
  * Set ```ShopifyAPIKey```, ```ShopifySharedSecret```, ```ShopifyShopName```, ```ShopifyPassword``` of a store that the application is created for. To get such information, the store owner needs to log in from https://STORE_NAME.myshopify.com/admin/api. Then, In the "Shopify API" page followed, click "Generate new application" and an application will be automatically created. The API Key, Password, Shared Secret, and store name can be found from the generated application. Those information enables an Android application to access product data but not to modify data, so there is no concern about the data security. These information is also different from the real store admin account and password. Therefore it is safe for a store to provide such information to a developer.
  * Set ```mainOptionName``` of the store for product options.
* Update your application's ```assets/countries.json```. To load country information, issue a GET request using RESTClient in Firefox to the store's API, for example https://STORE_NAME.myshopify.com/admin/countries.json. Enter API key and API password for login. Then replace original content in countries.json with returned content.
* In your application's ```res/values/options_array.xml```, update options for the store.

Future Plan
-----------

We may integrate third-party payment SDK (such as PayPal) to add payment capability, thus end users can make real purchases within applications developed using Shopfast SDK.

Special Notes
------------

* Part of Shopfast SDK is based on Shopify4J (https://github.com/csaunders/shopify4j), an Android-compatible library that allows developers to programatically access the Admin section of Shopify stores. Shopify4J is no longer actively maintained. Shopfast SDK fixed some issues to keep it working smoothly.
* Source code of Apache Common Codec (https://commons.apache.org/proper/commons-codec/) and codegist/crest (https://github.com/codegist/crest) are included for debugging purpose. Specially, a bug in org.codegist.crest.HttpRequest was fixed.

Acknowledgement
---------------

Shopfast SDK utilizes the following libraries/contributions:
* Shopify4J: https://github.com/csaunders/shopify4j
* actionbarsherlock: http://actionbarsherlock.com/
* Android ViewPageIndicator developed by Patrik Åkerfeldt/Jake Wharton: https://github.com/JakeWharton/ViewPagerIndicator
* Android Query: https://code.google.com/p/android-query/
* Jackson project: https://github.com/FasterXML/jackson
* codegist/common: https://github.com/codegist/common
* Apache Commons Lang: https://commons.apache.org/proper/commons-lang/
* Apache HttpClient Mime : https://hc.apache.org/httpcomponents-client-ga/httpmime/project-summary.html

Developer
---------
* Jay Xue <yxue24@gmail.com>, Waterloo Mobile Studio

License
-------

    Copyright 2015 Waterloo Mobile Studio

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

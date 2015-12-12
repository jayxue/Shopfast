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

package com.wms.opensource.shopfast.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityUtil {

	public static void goToActivity(Context packageContext, Class<?> targetActivityClass) {
		Intent intent = new Intent();
		intent.setClass(packageContext, targetActivityClass);
		packageContext.startActivity(intent);
	}

	public static void goToActivity(Context packageContext,	Class<?> targetActivityClass, String paraName, Object paramValue) {
		Intent intent = new Intent();
		intent.setClass(packageContext, targetActivityClass);
		Bundle bundle = new Bundle();
		if(paramValue instanceof String) {
			bundle.putString(paraName, (String) paramValue);
		}
		else {
			bundle.putInt(paraName, (int) paramValue);
		}
		intent.putExtras(bundle);
		packageContext.startActivity(intent);
	}

}

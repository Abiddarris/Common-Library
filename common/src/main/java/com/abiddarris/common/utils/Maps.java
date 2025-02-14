/***********************************************************************************
 * Copyright 2024-2025 Abiddarris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************************/
package com.abiddarris.common.utils;

import java.util.HashMap;

public class Maps {
	
	public static <T,K,V> HashMap<K,V> newHashMap(T[] values,Supplier<T,K,V> supplier) {
		HashMap<K,V> map = new HashMap<>();
		for(T value : values){
			Entry<K,V> entry = supplier.get(value);
			map.put(entry.key,entry.value);
		}
		return map;
	}
	
	public static interface Supplier<T,K,V> {
		Entry<K,V> get(T obj);
	}
	
	public static class Entry<K,V> {
		private K key;
		private V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}	
	}
}

/* bytefrog: a tracing instrumentation toolset for the JVM. For more information, see
 * <https://github.com/codedx/bytefrog>
 *
 * Copyright (C) 2014-2017 Code Dx, Inc. <https://codedx.com/>
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
 */

package com.codedx.bytefrog.instrumentation.id;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Assigns numeric IDs to classes, storing their information for later retrieval.
  *
  * @author robertf
  */
public class ClassIdentifier {
	private final AtomicInteger nextId = new AtomicInteger();
	protected final ConcurrentHashMap<Integer, ClassInformation> map = new ConcurrentHashMap<>();

	public int record(String className, String sourceFile) {
		int id = nextId.getAndIncrement();
		map.put(id, new ClassInformation(className, sourceFile));
		return id;
	}

	public ClassInformation get(int id) {
		return map.get(id);
	}

	/** Stores information about a class. */
	public static class ClassInformation {
		private final String name;
		private final String sourceFile;

		public ClassInformation(String name, String sourceFile) {
			this.name = name;
			this.sourceFile = sourceFile;
		}

		/** Gets the name of the class.
		  * @returns the name of the class
		  */
		public String getName() {
			return name;
		}

		/** Gets the source filename for the class.
		  * @returns the source filename for the class, or null if unknown
		  */
		public String getSourceFile() {
			return sourceFile != null ? sourceFile : "";
		}
	}
}
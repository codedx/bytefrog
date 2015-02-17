/*
 * bytefrog: a tracing framework for the JVM. For more information
 * see http://code-pulse.com/bytefrog
 *
 * Copyright (C) 2014 Applied Visions - http://securedecisions.avi.com
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

import sbt._

import java.io.File
import java.net.URL

/** A helper object for running Jar Jar Links.
  *
  * @author robertf
  */
class JarJarRunner(java: File, cacheDir: File, logger: Logger) {
	lazy val jarjar = {
		val jarjar = cacheDir / "jarjar-1.4.jar"

		if (!jarjar.exists) {
			logger info "Downloading Jar Jar..."
			IO.download(JarJarRunner.url, jarjar)
		}

		jarjar
	}

	def repackage(rulesFile: File, in: File, out: File) {
		val cmd =
			java.getCanonicalPath ::
			"-jar" :: jarjar.getCanonicalPath ::
			"process" :: rulesFile.getCanonicalPath :: in.getCanonicalPath :: out.getCanonicalPath ::
			Nil

		val ret = Process(cmd).!

		if (ret != 0) sys error s"Non-zero exit code from Jar Jar ($ret)"
	}
}

object JarJarRunner {
	private[JarJarRunner] val url = new URL("https://jarjar.googlecode.com/files/jarjar-1.4.jar")

	def apply(java: File, cacheDir: File, logger: Logger) = new JarJarRunner(java, cacheDir, logger)
}
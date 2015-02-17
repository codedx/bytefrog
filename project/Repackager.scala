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
import Keys._

/** Repackages a dependency, so as to avoid collisions in tracee applications.
  *
  * @author robertf
  */
class Repackager(
	name: String,
	deps: Seq[ModuleID],
	rules: Repackager.Rule*
) {
	val Repackager = config(s"repackager-$name").hide

	val jarjarRunner = TaskKey[JarJarRunner]("jarjar-runner")
	val repackage = TaskKey[Seq[File]]("repackage")

	val settings = Seq(
		ivyConfigurations += Repackager,
		libraryDependencies ++= deps.map(_ % Repackager),

		jarjarRunner in Repackager := {
			val taskStreams = streams.value
			val java = ((javaHome in Repackager).value getOrElse file(System getProperty "java.home")) / "bin" / "java"
			JarJarRunner(java, taskStreams.cacheDirectory, taskStreams.log)
		},

		repackage in Repackager <<= Def.task {
			val taskStreams = streams.value
			val log = taskStreams.log
			val cache = taskStreams.cacheDirectory

			val outDir = target.value / s"repackaged-$name"
			val jjRunner = (jarjarRunner in Repackager).value

			outDir.mkdirs

			val cachedRepackage = FileFunction.cached(cache / s"repackage-$name", inStyle = FilesInfo.lastModified, outStyle = FilesInfo.exists) { (jars: Set[File]) =>
				IO.withTemporaryFile("jarjar-", ".rules") { ruleFile =>
					IO.writeLines(ruleFile, rules.map(_.line))

					val outs = for (jar <- jars) yield {
						val out = outDir / s"repackaged-${jar.getName}"

						log info s"Repackaging ${jar.getName}..."
						jjRunner.repackage(ruleFile, jar, out)

						out
					}

					outs.toSet
				}
			}

			cachedRepackage(Classpaths.managedJars(Repackager, classpathTypes.value, update.value).map(_.data).toSet).toSeq
		},

		externalDependencyClasspath in Compile <++= repackage in Repackager,
		externalDependencyClasspath in Test <++= repackage in Repackager,
		externalDependencyClasspath in Runtime <++= repackage in Repackager
	)
}

object Repackager {
	def apply(name: String, deps: Seq[ModuleID], rules: Rule*) =
		new Repackager(name, deps, rules: _*).settings

	sealed trait Rule { def line: String }

	case class Rename(pattern: String, result: String) extends Rule {
		def line = s"rule $pattern $result"
	}

	case class Zap(pattern: String) extends Rule {
		def line = s"zap $pattern"
	}

	case class Keep(pattern: String) extends Rule {
		def line = s"keep $pattern"
	}
}
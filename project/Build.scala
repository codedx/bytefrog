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
import Dependencies._
import com.typesafe.sbteclipse.core.EclipsePlugin._
import sbtassembly.AssemblyPlugin._
import sbtassembly.AssemblyPlugin.autoImport._

object BuildDef extends Build {

	val baseCompilerSettings = Seq(
		scalacOptions := List("-deprecation", "-unchecked", "-feature"),
		scalaVersion := "2.10.4",
		// note: -Xlint:-options disables warnings about compiling for Java 6 on Java 7
		// without the boot classpath. We're depending on the travis openjdk6 matrix
		// target to pick up on any potential issues with Java 6 support.
		javacOptions := List("-source", "1.6", "-target", "1.6", "-Xlint:-options")
	)

	lazy val Shared = JavaOnlyProject("Common", file("common"))
		.settings(baseCompilerSettings: _*)
		.settings(useScalaTest: _*)
		.settings(parallelExecution in Test := false)

	lazy val Agent = JavaOnlyProject("Agent", file("agent"))
		.dependsOn(Shared)
		.settings(baseCompilerSettings: _*)
		.settings(assemblySettings: _*)
		.settings(useScalaTest: _*)
		.settings(Repackager("asm", asm, Repackager.Rename("org.objectweb.asm.**", "com.secdec.bytefrog.asm.@1")): _*)
		.settings(
			version := "1.0",

			assemblyJarName in assembly := "bytefrog-tracer.jar",
			packageOptions in assembly <+= (assemblyJarName in assembly) map { jarName =>
				Package.ManifestAttributes(
					"Premain-Class" -> "com.secdec.bytefrog.agent.javaagent.JavaAgent",
					"Agent-Class" -> "com.secdec.bytefrog.agent.javaagent.JavaAgent",
					"Boot-Class-Path" -> jarName,
					"Can-Redefine-Classes" -> "true",
					"Can-Retransform-Classes" -> "true"
				)
			},

			parallelExecution in Test := false
		)

	lazy val HQ = Project("HQ", file("hq"))
		.dependsOn(Shared)
		.settings(baseCompilerSettings: _*)
		.settings(useScalaTest: _*)
		.settings(
			libraryDependencies += reactive,
			parallelExecution in Test := false
		)

	lazy val Stack = Project("Tracer-Stack", file("."))
		.settings(baseCompilerSettings: _*)
		.aggregate(Shared, Agent, HQ)

	def JavaOnlyProject(name: String, root: File) = Project(name, root)
		.settings(
			EclipseKeys.projectFlavor in Compile := EclipseProjectFlavor.Java,
			unmanagedSourceDirectories in Compile <<= (javaSource in Compile) { _ :: Nil },
			// unmanagedSourceDirectories in Test <<= (javaSource in Test) { _ :: Nil },
			autoScalaLibrary := false
		)

	def useScalaTest = Seq(
		scalaVersion := "2.10.4",
		libraryDependencies += scalaMock
		// this ^ will automatically include the appropriate scalatest dependency too
	)
}
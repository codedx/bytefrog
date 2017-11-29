val shared = Seq(
	organization := "com.codedx",
	scalacOptions := List("-deprecation", "-unchecked", "-feature"),
	scalaVersion := "2.12.4",
	javacOptions := List("-source", "1.7", "-target", "1.7", "-Xlint:-options", "-Xlint:unchecked")
)

val javaOnly = Seq(
	autoScalaLibrary := false,
	crossPaths := false
)

val withTesting = Seq(
	libraryDependencies += Dependencies.scalactic,
	libraryDependencies += Dependencies.scalaTest,
	libraryDependencies += Dependencies.scalaMock
)

lazy val Instrumentation = (project in file("instrumentation"))
	.settings(
		shared,
		javaOnly,
		withTesting,

		libraryDependencies ++= Dependencies.asm
	)

lazy val FilterInjector = (project in file("filter-injector"))
	.dependsOn(Util)
	.settings(
		shared,
		javaOnly,

		libraryDependencies ++= Dependencies.asm,
		libraryDependencies += Dependencies.minlog
	)

lazy val Util = (project in file("util"))
	.settings(
		shared,
		javaOnly,
		withTesting,

		libraryDependencies ++= Dependencies.asm,
		libraryDependencies += Dependencies.minlog
	)

lazy val Stack = (project in file("."))
	.aggregate(Instrumentation, FilterInjector, Util)
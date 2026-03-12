name := """FlickLytics"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "3.3.1"

libraryDependencies += guice
libraryDependencies += javaWs
libraryDependencies += "jakarta.inject" % "jakarta.inject-api" % "2.0.1"
libraryDependencies += "org.mockito" % "mockito-core" % "5.14.1" % Test
jacocoReportSettings := JacocoReportSettings()

addCommandAlias("javadoc", "doc")
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "4.12.0"

name := """FlickLytics"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "3.8.1"
javacOptions ++= Seq("--release", "21")

libraryDependencies += guice
libraryDependencies += javaWs
libraryDependencies += "jakarta.inject" % "jakarta.inject-api" % "2.0.1"
libraryDependencies += "org.mockito" % "mockito-core" % "5.14.1" % Test

// Source: https://mvnrepository.com/artifact/org.apache.pekko/pekko-actor
libraryDependencies += "org.apache.pekko" %% "pekko-actor" % "1.0.3"
libraryDependencies += "org.apache.pekko" %% "pekko-actor-testkit-typed" % "1.0.3" % Test

jacocoReportSettings := JacocoReportSettings()
Test / javaSource := baseDirectory.value / "test"
jacocoExcludes := Seq(
  "models.dto.PersonDTO",
  "models.dto.KnownForDTO",
  "models.dto.MovieSearchResponseDTO",
  "models.dto.TVShowSearchResponseDTO",
  "models.dto.PersonSearchResponseDTO",
  "views.*",
  "router.*",
  "controllers.javascript.*",
  "controllers.ReverseTmdbSearchController",
  "controllers.routes",
  "controllers.routes$javascript",
  "controllers.ReverseAssets",
  "models.mapper.TmdbMapper.this",
  "services.tmdb.TmdbConfig",
  "services.tmdb.TmdbSearchService$GenreCacheEntry",
)

addCommandAlias("javadoc", "doc")
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "4.12.0"
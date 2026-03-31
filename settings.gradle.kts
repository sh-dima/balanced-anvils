pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")

		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	// Apply the foojay-resolver plugin to allow automatic download of JDKs
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "template"

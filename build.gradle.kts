import org.apache.batik.transcoder.SVGAbstractTranscoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.util.jar.Attributes

buildscript {
	repositories {
		mavenCentral()
	}

	dependencies {
		classpath(libs.batik.transcoder)
		classpath(libs.batik.codec)
	}
}

plugins {
	alias(libs.plugins.kotlin)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.loom)
}

repositories {
	mavenCentral()
}

dependencies {
	minecraft(libs.minecraft)
	mappings(loom.officialMojangMappings())

	modImplementation(libs.fabric.loader)
	modImplementation(libs.fabric.api)

	modImplementation(libs.fabric.kotlin)

	implementation(libs.kotlin.serialization)
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

group = "org.example"
description = "A template repository for Minecraft Fabric mods."

version = ProcessBuilder("git", "describe", "--tags", "--always", "--dirty")
	.directory(project.projectDir)
	.start()
	.inputStream
	.bufferedReader()
	.readText()
	.trim()

loom {
	splitEnvironmentSourceSets()

	mods {
		register(project.name) {
			sourceSet("main")
			sourceSet("client")
		}
	}
}

val generatedResources: Directory = layout.buildDirectory.dir("generated/resources").get()

sourceSets.main {
	resources {
		exclude("assets/${project.name}/icon.svg")
		srcDir(generatedResources)
	}
}

tasks {
	val generateIcon = register("generateIcon") {
		val inputFile = file("src/main/resources/assets/${project.name}/icon.svg")
		val outputFile = generatedResources.file("assets/${project.name}/icon.png").asFile

		inputs.file(inputFile)
		outputs.file(outputFile)

		doLast {
			outputFile.parentFile.mkdirs()

			val transcoder = PNGTranscoder()

			transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, 128F)
			transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, 128F)

			val input = TranscoderInput(inputFile.toURI().toString())
			val output = TranscoderOutput(outputFile.outputStream())

			transcoder.transcode(input, output)
		}
	}

	processResources {
		inputs.property("version", project.version)
		inputs.property("minecraft_version", libs.versions.minecraft.get())
		inputs.property("fabric_version", libs.versions.fabric.loader.get())
		inputs.property("fabric_kotlin_version", libs.versions.fabric.kotlin.get())
		inputs.property("fabric_api_version", libs.versions.fabric.api.get())
		inputs.property("java_version", java.toolchain.languageVersion.get().asInt())

		inputs.property("name", project.name)
		inputs.property("group", project.group)

		inputs.property("description", project.description)

		filesMatching("fabric.mod.json") {
			expand(
				mapOf(
					"name" to inputs.properties["name"],
					"group" to inputs.properties["group"],
					"description" to inputs.properties["description"],

					"minecraft_version" to inputs.properties["minecraft_version"],
					"fabric_version" to inputs.properties["fabric_version"],
					"fabric_kotlin_version" to inputs.properties["fabric_kotlin_version"],
					"fabric_api_version" to inputs.properties["fabric_api_version"],
					"java_version" to inputs.properties["java_version"],
					"version" to inputs.properties["version"]
				)
			)
		}

		dependsOn(generateIcon)
	}

	withType<AbstractArchiveTask> {
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true

		filePermissions {
			user.read = true
			user.write = true
			user.execute = false

			group.read = true
			group.write = false
			group.execute = false

			other.read = true
			other.write = false
			other.execute = false
		}

		dirPermissions {
			user.read = true
			user.write = true
			user.execute = true

			group.read = true
			group.write = false
			group.execute = true

			other.read = false
			other.write = false
			other.execute = true
		}
	}

	withType<Jar> {
		manifest {
			attributes[Attributes.Name.IMPLEMENTATION_TITLE.toString()] = "Template Kotlin Project"
			attributes[Attributes.Name.IMPLEMENTATION_VERSION.toString()] = project.version
			attributes[Attributes.Name.IMPLEMENTATION_VENDOR.toString()] = "Дима Ш."
		}
	}
}

listOf(tasks.jar, tasks.kotlinSourcesJar).forEach {
	it {
		into("META-INF") {
			from("LICENSE.txt")
			from("NOTICE.txt")
			from("docs/DISCLAIMER.txt")
		}
	}
}

configurations.all {
	resolutionStrategy {
		failOnNonReproducibleResolution()
	}
}

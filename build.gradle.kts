import java.util.jar.Attributes

buildscript {
	repositories {
		mavenCentral()
	}
}

plugins {
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
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

group = "io.gitlab.shdima"
description = "Makes anvil experience costs make more sense."

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
		}
	}
}

tasks {
	processResources {
		inputs.property("version", project.version)
		inputs.property("minecraft_version", libs.versions.minecraft.get())
		inputs.property("fabric_version", libs.versions.fabric.loader.get())
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
					"fabric_api_version" to inputs.properties["fabric_api_version"],
					"java_version" to inputs.properties["java_version"],
					"version" to inputs.properties["version"]
				)
			)
		}
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
			attributes[Attributes.Name.IMPLEMENTATION_TITLE.toString()] = "Balanced Anvils"
			attributes[Attributes.Name.IMPLEMENTATION_VERSION.toString()] = project.version
			attributes[Attributes.Name.IMPLEMENTATION_VENDOR.toString()] = "Дима Ш."
		}
	}
}

listOf(tasks.jar).forEach {
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

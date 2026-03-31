package org.example.template

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object TemplateMod : ModInitializer {
	const val MOD_ID = "template"

	val container: ModContainer = FabricLoader.getInstance()
		.getModContainer(MOD_ID)
		.orElseThrow()
	val logger: Logger = LoggerFactory.getLogger(container.metadata.name.replace(" ", ""))

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
	}
}

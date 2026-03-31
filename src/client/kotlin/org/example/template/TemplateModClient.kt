package org.example.template

import net.fabricmc.api.ClientModInitializer

object TemplateModClient : ClientModInitializer {
	override fun onInitializeClient() {
		TemplateMod.logger.info("Hello Fabric client world!")
	}
}

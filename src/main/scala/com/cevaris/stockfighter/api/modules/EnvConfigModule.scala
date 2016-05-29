package com.cevaris.stockfighter.api.modules

import com.cevaris.stockfighter.ApiKey
import com.cevaris.stockfighter.guice.GuiceModule
import com.google.inject.Provides
import java.io.{File, FileInputStream}
import org.yaml.snakeyaml.Yaml


case class EnvConfigModule() extends GuiceModule {
  @Provides
  def providesApiKey(): ApiKey = {
    val yaml = new Yaml()
    val configPath = getClass.getResource("/stockfighter/.env.yml").getPath
    val fs = new FileInputStream(new File(configPath))
    val config = yaml.load(fs).asInstanceOf[java.util.Map[String, String]]
    ApiKey(config.get("api_key"))
  }
}


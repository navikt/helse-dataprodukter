package no.nav.helse

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val env = System.getenv()
    val datasource = datasource(
        env.value("DATABASE_USERNAME"),
        env.value("DATABASE_PASSWORD"),
        String.format(
            "jdbc:postgresql://%s:%s/%s",
            env.value("DATABASE_HOST"),
            env.value("DATABASE_PORT"),
            env.value("DATABASE_DATABASE")
        )
    )

    RapidApplication.create(env).apply {
        Mediator(this, VedtakFattetDao(datasource), UtbetalingEndretDao(datasource))
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                migrate(datasource)
            }
        })
    }.start()
}

fun Map<String, String>.value(key: String) = requireNotNull(this[key]) { "Environment value $key must be defined" }

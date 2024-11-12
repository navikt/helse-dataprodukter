package no.nav.helse

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.helse.rapids_rivers.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

val logger: Logger = LoggerFactory.getLogger("forstegangsbehandling")

fun main() {
    val env = System.getenv()
    val datasource = datasource(
        env["DATABASE_USERNAME"] ?: throw IllegalArgumentException("Missing envvar"),
        env["DATABASE_PASSWORD"] ?: throw IllegalArgumentException("Missing envvar"),
        String.format(
            "jdbc:postgresql://%s:%s/%s",
            requireNotNull(env["DATABASE_HOST"]) { "database host must be set" },
            requireNotNull(env["DATABASE_PORT"]) { "database port must be set" },
            requireNotNull(env["DATABASE_DATABASE"]) { "database name must be set" })
    )

    RapidApplication.create(env).apply {
        SøknadMediator(this, FørstegangsbehandlingDao(datasource))
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                migrate(datasource)
            }
        })
    }.start()
}

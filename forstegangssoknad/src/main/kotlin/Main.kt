package no.nav.helse

import no.nav.helse.rapids_rivers.*
import java.lang.IllegalArgumentException

fun main() {
    val env = System.getenv()
    val datasource = datasource(
        env["DATABASE_USERNAME"] ?: throw IllegalArgumentException("Missing envvar"),
        env["DATABASE_PASSWORD"] ?: throw IllegalArgumentException("Missing envvar"),
        env["DATABASE_URL"] ?: throw IllegalArgumentException("Missing envvar")
    )

    RapidApplication.create(env).apply {
        SøkandMediator(this, FørstegangsbehandlingDao(datasource))
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                migrate(datasource)
            }
        })
    }.start()
}

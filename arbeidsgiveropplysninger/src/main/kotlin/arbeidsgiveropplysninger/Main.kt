package no.nav.helse.arbeidsgiveropplysninger

import no.nav.helse.datasource
import no.nav.helse.migrate
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("arbeidsgiveropplysninger")

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

    val inntektsmeldingHåndtertDao = InntektsmeldingHåndtertDao(datasource)

    RapidApplication.create(env).apply {
        InntektsmeldingHåndtertRiver(this, inntektsmeldingHåndtertDao)
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                migrate(datasource)
            }
        })
    }.start()
}

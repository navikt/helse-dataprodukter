package no.nav.helse

import no.nav.helse.rapids_rivers.*

fun main() {
    val env = System.getenv()


    RapidApplication.create(env).apply {
        SøknadsRiver(this)
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
            }
        })
    }.start()
}

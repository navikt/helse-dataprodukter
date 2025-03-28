package no.nav.helse

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.kv

class SøknadMediator(
    rapidsConnection: RapidsConnection,
    private val dao: FørstegangsbehandlingDao,
) {

    init {
        SøknadsRiver(rapidsConnection, this)
    }

    fun håndter(søknad: Søknad) {
        logger.info("lagrer person")
        dao.lagrePerson(søknad.fnr, søknad.orgnummer)
        logger.info("finner personRef")
        val personRef = dao.refFor(søknad.fnr, søknad.orgnummer)
        logger.info("lagrer søknad med {}, {}", keyValue("@id", søknad.id), keyValue("søknadId", søknad.søknadId))
        dao.lagreSøknad(personRef, søknad, false)
        logger.info("henter søknadsperioder")
        val søknadsPerioder = hentSøknadsperioder(personRef)
        val updateMap = søknadsPerioder.mapping()
        logger.info("oppdaterer søknader for {} med data $updateMap", kv("personRef", personRef))
        dao.oppdaterSøknader(personRef, updateMap)
    }

    private fun hentSøknadsperioder(personRef: Long): Førstegangsbehandling {
        val søknader = dao.hentSøknader(personRef)
        return Førstegangsbehandling().apply { søknader.forEach { motta(it) } }
    }
}

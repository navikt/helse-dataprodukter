package no.nav.helse

import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.RapidsConnection
import java.time.LocalDate

class SøknadMediator(
    rapidsConnection: RapidsConnection,
    private val dao: FørstegangsbehandlingDao,
    private val env: Map<String, String> = System.getenv(),
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
        val søknader = dao.hentSøknader(personRef).utenDårligeTestdata()
        return Førstegangsbehandling().apply { søknader.forEach { motta(it) } }
    }

    private fun Iterable<Søknad>.utenDårligeTestdata() =
        filterNot { søknad ->
            env["NAIS_CLUSTER_NAME"] == "dev-gcp" && try {
                Periode(søknad.fom, minOf(søknad.tom, søknad.arbeidGjenopptatt ?: LocalDate.MAX))
                false
            } catch (e: Exception) {
                true
            }
        }
}

package no.nav.helse

import no.nav.helse.rapids_rivers.RapidsConnection

class SøkandMediator(private val rapidsConnection: RapidsConnection, private val dao: FørstegangsbehandlingDao) {

    init {
        SøknadsRiver(rapidsConnection, this)
    }


    fun håndter(søknad: Søknad) {
        dao.lagrePerson(søknad.fnr, søknad.orgnummer)
        val personRef = dao.refFor(søknad.fnr, søknad.orgnummer)
        dao.lagreSøknad(personRef, søknad, false)
        val søknadsPerioder = hentSøkandsperioder(personRef)
        val updateMap = søknadsPerioder.mapping()
        dao.oppdaterSøknader(personRef, updateMap)
    }

    private fun hentSøkandsperioder(personRef: Long): Førstegangsbehandling {
        val søknader = dao.hentSøknader(personRef)
        return Førstegangsbehandling().apply { søknader.forEach { motta(it) } }
    }

}
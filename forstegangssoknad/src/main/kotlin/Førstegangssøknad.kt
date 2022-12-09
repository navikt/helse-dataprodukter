package no.nav.helse

import no.nav.helse.Periode.Companion.grupperSammenhengendePerioderMedHensynTilHelg
import java.time.LocalDate

class Førstegangssøknad {

    var søknadsPerioder = mutableListOf<Periode>()


    /**
     * Mottar søknader og returnerer om søknaden er av typen førstegangsbehandling
     */
    fun motta(søknad: Søknad): Boolean {
        val antallFørstegangssøknad = søknadsPerioder.size
        søknadsPerioder.add(Periode(søknad.fom, minOf(søknad.tom, søknad.arbeidGjenopptatt)))
        val nyeSøknadsPerioder = søknadsPerioder.grupperSammenhengendePerioderMedHensynTilHelg()
        val nyttAntallFørstegangssøknad = nyeSøknadsPerioder.size
        søknadsPerioder = nyeSøknadsPerioder.toMutableList()

        return nyttAntallFørstegangssøknad > antallFørstegangssøknad
    }

    override fun toString(): String {
        return søknadsPerioder.toString()
    }
}

data class Søknad(val fom: LocalDate, val tom: LocalDate, val arbeidGjenopptatt: LocalDate)
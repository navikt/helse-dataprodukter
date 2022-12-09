package no.nav.helse

import no.nav.helse.Periode.Companion.grupperSammenhengendePerioderMedHensynTilHelg
import java.time.LocalDate

class Førstegangssøknad {

    var søknadsPerioder = mutableListOf<Periode>()

    fun handle(søknad: Søknad): Boolean {
        val antallFørstegangssøknad = søknadsPerioder.size
        søknadsPerioder.add(Periode(søknad.fom, søknad.tom))
        val nyeSøknadsPerioder = søknadsPerioder.grupperSammenhengendePerioderMedHensynTilHelg()
        val nyttAntallFørstegangssøknad = nyeSøknadsPerioder.size
        søknadsPerioder = nyeSøknadsPerioder.toMutableList()

        return nyttAntallFørstegangssøknad > antallFørstegangssøknad
    }
}

data class Søknad(val fom: LocalDate, val tom: LocalDate, val arbeidGjenopptatt: LocalDate)
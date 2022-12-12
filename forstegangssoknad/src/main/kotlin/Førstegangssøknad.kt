package no.nav.helse

import no.nav.helse.Periode.Companion.grupperSammenhengendePerioderMedHensynTilHelg
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Førstegangssøknad {

    var søknadsPerioder = mutableListOf<Periode>()


    /**
     * Mottar søknader og returnerer om søknaden er av typen førstegangsbehandling
     */
    fun motta(søknad: Søknad): Boolean {
        val antallFørstegangssøknad = søknadsPerioder.size
        søknadsPerioder.add(Periode(søknad.fom, minOf(søknad.tom, søknad.arbeidGjenopptatt ?: LocalDate.MAX)))
        val nyeSøknadsPerioder = søknadsPerioder.grupperSammenhengendePerioderMedHensynTilHelg()
        val nyttAntallFørstegangssøknad = nyeSøknadsPerioder.size
        søknadsPerioder = nyeSøknadsPerioder.toMutableList()

        return nyttAntallFørstegangssøknad > antallFørstegangssøknad
    }

    override fun toString(): String {
        return søknadsPerioder.toString()
    }
}

data class Søknad(
    val id: UUID,
    val søkandsId: UUID,
    val sykemeldingId: UUID,
    val fnr: String,
    val orgnummer: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val arbeidGjenopptatt: LocalDate?,
    val opprettet: LocalDateTime
)
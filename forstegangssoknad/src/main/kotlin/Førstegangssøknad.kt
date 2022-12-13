package no.nav.helse

import no.nav.helse.Periode.Companion.grupperSammenhengendePerioderMedHensynTilHelg
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Førstegangssøknad {

    private var søknadsPerioder = mutableListOf<Periode>()
    private val søknader = mutableListOf<Søknad>()


    /**
     * Mottar søknader og returnerer om søknaden er av typen førstegangsbehandling
     */
    internal fun motta(søknad: Søknad) {
        søknader.add(søknad)
        søknader.sortBy { it.fom }
        søknadsPerioder.add(Periode(søknad.fom, minOf(søknad.tom, søknad.arbeidGjenopptatt ?: LocalDate.MAX)))
        val nyeSøknadsPerioder = søknadsPerioder.grupperSammenhengendePerioderMedHensynTilHelg()
        søknadsPerioder = nyeSøknadsPerioder.toMutableList()
    }

    internal fun førstegangsbehandlinger() = søknadsPerioder
        .map { periode -> søknader.last { it.fom == periode.start } }
        .map { it.id }

    internal fun Ikkeførstegangsbehandlinger(): List<UUID> {
        val førstegangsbehandlinger = førstegangsbehandlinger()
        return søknader.map { it.id }.filterNot {
            it in førstegangsbehandlinger
        }
    }

    override fun toString(): String {
        return søknadsPerioder.toString()
    }
}

class Søknad(
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
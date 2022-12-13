package no.nav.helse

import no.nav.helse.Periode.Companion.grupperSammenhengendePerioderMedHensynTilHelg
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Førstegangsbehandling {

    private var søknadsPerioder = mutableListOf<Periode>()
    private val søknader = mutableListOf<Søknad>()


    /**
     * Mottar søknader og returnerer om søknaden er av typen førstegangsbehandling
     */
    internal fun motta(søknad: Søknad) {
        søknader.add(søknad)
        søknadsPerioder.add(Periode(søknad.fom, minOf(søknad.tom, søknad.arbeidGjenopptatt ?: LocalDate.MAX)))
        val nyeSøknadsPerioder = søknadsPerioder.grupperSammenhengendePerioderMedHensynTilHelg()
        søknadsPerioder = nyeSøknadsPerioder.toMutableList()
    }

    internal fun førstegangsbehandlinger() = søknadsPerioder
        .map { periode -> søknader.sortedBy { it.opprettet }.last { it.fom == periode.start } }
        .map { it.id }

    internal fun mapping(): List<Pair<UUID, Boolean>> {
        val førstegangsbehandlinger = førstegangsbehandlinger()
        val forlengelser = søknader.map { it.id }.filterNot { it in førstegangsbehandlinger }
        return førstegangsbehandlinger.map { it to true } + forlengelser.map { it to false }
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
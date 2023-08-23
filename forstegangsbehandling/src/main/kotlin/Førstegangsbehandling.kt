package no.nav.helse

import no.nav.helse.Periode.Companion.grupperSammenhengendePerioderMedHensynTilHelg
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Førstegangsbehandling {

    private var søknadsperioder = mutableListOf<Periode>()
    private val søknader = mutableListOf<Søknad>()


    internal fun motta(søknad: Søknad) {
        søknader.add(søknad)
        val tom = when {
            søknad.tom < søknad.fom -> null
            søknad.arbeidGjenopptatt != null && søknad.arbeidGjenopptatt >= søknad.fom ->
                minOf(søknad.tom, søknad.arbeidGjenopptatt)
            søknad.arbeidGjenopptatt == null && søknad.fom <= søknad.tom ->
                søknad.tom
            else -> null
        } ?: return
        søknadsperioder.add(Periode(søknad.fom, tom))
        val nyeSøknadsPerioder = søknadsperioder.grupperSammenhengendePerioderMedHensynTilHelg()
        søknadsperioder = nyeSøknadsPerioder.toMutableList()
    }

    internal fun førstegangsbehandlinger() = søknadsperioder
        .map { periode -> søknader.sortedBy { it.opprettet }.last { it.fom == periode.start } }
        .map { it.id }

    internal fun mapping(): List<Pair<UUID, Boolean>> {
        val førstegangsbehandlinger = førstegangsbehandlinger()
        val forlengelser = søknader.map { it.id }.filterNot { it in førstegangsbehandlinger }
        return førstegangsbehandlinger.map { it to true } + forlengelser.map { it to false }
    }

    override fun toString(): String {
        return søknadsperioder.toString()
    }
}

data class Søknad(
    val id: UUID,
    val søknadId: UUID,
    val sykmeldingId: UUID,
    val fnr: String,
    val orgnummer: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val arbeidGjenopptatt: LocalDate?,
    val opprettet: LocalDateTime
)

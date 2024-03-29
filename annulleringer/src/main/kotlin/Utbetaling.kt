package no.nav.helse

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.Utbetaling.Versjon.Companion.håndterAnnullering
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

internal class Utbetaling(
    private val korrelasjonsId: UUID,
    private val arbeidsgiverFagsystemId: String,
    private val personFagsystemId: String,
    private val opprettet: LocalDateTime,
    versjoner: List<Versjon> = emptyList()
) {
    private val versjoner: MutableList<Versjon> = versjoner.toMutableList()

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }

    internal fun håndter(mediator: Mediator, versjon: Versjon) {
        if (versjon in versjoner)
            return sikkerlogg.info(
                "{} for {} eksisterer allerede",
                kv("versjon", versjon),
                kv("korrelasjonsId", korrelasjonsId)
            )

        lagreVersjon(mediator, versjon)
        if (versjon.erAnnullering()) versjoner.håndterAnnullering(mediator, korrelasjonsId)
    }

    internal fun lagre(mediator: Mediator, versjon: Versjon) {
        mediator.nyUtbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        lagreVersjon(mediator, versjon)
    }

    private fun lagreVersjon(mediator: Mediator, versjon: Versjon) {
        versjoner.add(versjon)
        versjon.lagre(mediator, korrelasjonsId)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Utbetaling &&
            korrelasjonsId == other.korrelasjonsId &&
            arbeidsgiverFagsystemId == other.arbeidsgiverFagsystemId &&
            personFagsystemId == other.personFagsystemId &&
            opprettet.truncatedTo(ChronoUnit.MILLIS) == other.opprettet.truncatedTo(ChronoUnit.MILLIS)
            )
    }

    override fun hashCode(): Int {
        var result = korrelasjonsId.hashCode()
        result = 31 * result + arbeidsgiverFagsystemId.hashCode()
        result = 31 * result + personFagsystemId.hashCode()
        result = 31 * result + opprettet.hashCode()
        return result
    }

    internal class Versjon(
        private val utbetalingId: UUID,
        private val utbetalingstype: Utbetalingstype,
        private val opprettet: LocalDateTime
    ) {
        internal companion object {
            internal fun List<Versjon>.håndterAnnullering(mediator: IMediator, korrelasjonsId: UUID) {
                mediator.håndterAnnullering(korrelasjonsId, this.map { it.utbetalingId })
            }
        }

        internal fun erAnnullering() = utbetalingstype == Utbetalingstype.ANNULLERING
        internal fun lagre(mediator: Mediator, korrelasjonsId: UUID) {
            mediator.nyVersjon(korrelasjonsId, utbetalingId, utbetalingstype, opprettet)
        }

        override fun toString(): String {
            return "utbetalingId=$utbetalingId, utbetalingstype=$utbetalingstype, opprettet=$opprettet"
        }

        override fun equals(other: Any?): Boolean {
            return this === other || (other is Versjon &&
                utbetalingId == other.utbetalingId &&
                utbetalingstype == other.utbetalingstype &&
                opprettet.truncatedTo(ChronoUnit.MILLIS) == other.opprettet.truncatedTo(ChronoUnit.MILLIS)
                )
        }

        override fun hashCode(): Int {
            var result = utbetalingId.hashCode()
            result = 31 * result + utbetalingstype.hashCode()
            result = 31 * result + opprettet.hashCode()
            return result
        }
    }
}

internal enum class Utbetalingstype {
    UTBETALING, ETTERUTBETALING, ANNULLERING, REVURDERING, FERIEPENGER;

    internal companion object {
        internal val gyldigeTyper = EnumSet.of(UTBETALING, ANNULLERING, REVURDERING)

        internal fun EnumSet<Utbetalingstype>.values() = this.map(Utbetalingstype::toString)
    }
}
package no.nav.helse

import java.time.LocalDateTime
import java.util.*

internal class Utbetaling(
    private val korrelasjonsId: UUID,
    private val utbetalingId: UUID,
    private val utbetalingstype: Utbetalingstype,
    private val opprettet: LocalDateTime
) {
    internal fun håndter(mediator: Mediator, utbetaling: Utbetaling) {
        if (utbetalingId == utbetaling.utbetalingId) return
        utbetaling.lagre(mediator)
        if (utbetaling.utbetalingstype == Utbetalingstype.ANNULLERING) mediator.håndterAnnullering(korrelasjonsId)
    }

    internal fun lagre(mediator: Mediator) {
        mediator.nyUtbetaling(korrelasjonsId, utbetalingId, utbetalingstype, opprettet)
    }
}

enum class Utbetalingsstatus {
    GODKJENT,
    SENDT,
    OVERFØRT,
    UTBETALING_FEILET,
    UTBETALT,
    ANNULLERT,
    IKKE_UTBETALT,
    FORKASTET,
    IKKE_GODKJENT,
    GODKJENT_UTEN_UTBETALING,
    NY;

    internal companion object {
        internal val gyldigeStatuser = EnumSet.of(ANNULLERT, UTBETALT, GODKJENT_UTEN_UTBETALING)

        internal fun EnumSet<Utbetalingsstatus>.values() = this.map(Utbetalingsstatus::toString)
    }
}
enum class Utbetalingstype {
    UTBETALING, ETTERUTBETALING, ANNULLERING, REVURDERING, FERIEPENGER;

    internal companion object {
        internal val gyldigeTyper = EnumSet.of(UTBETALING, ANNULLERING, REVURDERING)

        internal fun EnumSet<Utbetalingstype>.values() = this.map(Utbetalingstype::toString)
    }
}
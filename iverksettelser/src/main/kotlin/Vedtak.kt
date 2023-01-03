package no.nav.helse

import java.time.LocalDateTime
import java.util.UUID

internal class Vedtak(
    private val hendelseId: UUID,
    private val vedtaksperiodeId: UUID,
    private val utbetalingId: UUID?,
    private val korrelasjonsId: UUID?,
    private val fattetTidspunkt: LocalDateTime,
) {
}
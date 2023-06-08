import FørstegangsbehandlingTest.Companion.lagSøknad
import TestDatasource.migratedDb
import no.nav.helse.FørstegangsbehandlingDao
import no.nav.helse.SøknadMediator
import no.nav.helse.februar
import no.nav.helse.januar
import no.nav.helse.mars
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.*

class E2ETest {

    private val db = migratedDb
    private val dao = FørstegangsbehandlingDao(db)
    private val rapid = TestRapid()


    @BeforeEach
    fun reset() = resetDatabase()

    @Test
    fun `Person ender opp i databasen`() {
        SøknadMediator(rapid, dao)
        rapid.sendTestMessage(testSøknad("2022-10-01", "2022-10-31"))
        val result = dao.refFor("27845899830", "805824352")
        assertEquals(1L, result)
    }
    @Test
    fun `Førstegangsbehandling ender i databasen`() {
        SøknadMediator(rapid, dao)
        rapid.sendTestMessage(testSøknad("2022-10-01", "2022-10-31"))
        val ref = dao.refFor("27845899830", "805824352")
        val søknader = dao.hentSøknader(ref)
        assertEquals(1, søknader.size)
    }

    @Test
    fun `Ignorerer råtne testdata`() {
        SøknadMediator(rapid, dao, mapOf("NAIS_CLUSTER_NAME" to "prod-gcp"))
        val testSøknadMedDårligeData = lagSøknad(1.februar(2023), 18.januar(2023), null, fnr = "27845899830", orgnr = "805824352")
        val testSøknadMedFineData = lagSøknad(1.mars(2023), 18.mars(2023), null, fnr = "27845899830", orgnr = "805824352")
        val personRef = dao.lagrePerson(testSøknadMedDårligeData.fnr, testSøknadMedDårligeData.orgnummer)
        dao.lagreSøknad(personRef, testSøknadMedDårligeData, true)
        dao.lagreSøknad(personRef, testSøknadMedFineData, true)

        assertThrows<Exception> {
            rapid.sendTestMessage(testSøknad("2023-04-01", "2023-04-18"))
        }

        val rapid2 = TestRapid()
        SøknadMediator(rapid2, dao, mapOf("NAIS_CLUSTER_NAME" to "dev-gcp"))

        assertDoesNotThrow {
            rapid2.sendTestMessage(testSøknad("2023-05-01", "2023-05-18"))
        }
    }

//    @Test
//    fun `Førstegangsbehandling endring registres i database`() {
//        SøknadMediator(rapid, dao)
//        val t1 = testSøknad("2022-10-01", "2022-10-31")
//        val t2 = testSøknad("2022-10-01", "2022-10-31")
//        val t3 = testSøknad("2022-10-01", "2022-10-31")
//
//        rapid.sendTestMessage(t1)
//        rapid.sendTestMessage(t2)
//        rapid.sendTestMessage(t3)
//
//    }

}

@Language("JSON")
private fun testSøknad(fom: String, tom: String) = """
    {
      "id": "${UUID.randomUUID()}",
      "type": "ARBEIDSTAKERE",
      "status": "SENDT",
      "fnr": "27845899830",
      "sykmeldingId": "b14ff260-ace6-4625-8fa7-860a6f54c706",
      "arbeidsgiver": {
        "navn": "Vegansk Slakteri",
        "orgnummer": "805824352"
      },
      "arbeidssituasjon": "ARBEIDSTAKER",
      "korrigerer": null,
      "korrigertAv": null,
      "soktUtenlandsopphold": null,
      "arbeidsgiverForskutterer": null,
      "fom": "$fom",
      "tom": "$tom",
      "dodsdato": null,
      "startSyketilfelle": "2022-08-01",
      "arbeidGjenopptatt": null,
      "sykmeldingSkrevet": "2022-10-01T02:00:00",
      "opprettet": "2022-12-09T14:55:17.106944",
      "opprinneligSendt": null,
      "sendtNav": "2022-12-09T14:56:50.945681886",
      "sendtArbeidsgiver": "2022-12-09T14:56:50.945681886",
      "egenmeldinger": [],
      "fravarForSykmeldingen": [],
      "papirsykmeldinger": [],
      "fravar": [],
      "andreInntektskilder": [],
      "soknadsperioder": [
        {
          "fom": "2022-10-01",
          "tom": "2022-10-31",
          "sykmeldingsgrad": 80,
          "faktiskGrad": null,
          "avtaltTimer": null,
          "faktiskTimer": null,
          "sykmeldingstype": "GRADERT",
          "grad": 80
        }
      ],
      "sporsmal": [],
      "avsendertype": "BRUKER",
      "ettersending": false,
      "mottaker": "ARBEIDSGIVER_OG_NAV",
      "egenmeldtSykmelding": false,
      "arbeidUtenforNorge": null,
      "harRedusertVenteperiode": null,
      "behandlingsdager": null,
      "permitteringer": [],
      "merknaderFraSykmelding": null,
      "merknader": null,
      "sendTilGosys": null,
      "@id": "${UUID.randomUUID()}",
      "@opprettet": "2022-12-09T14:56:50.945681886",
      "@event_name": "sendt_søknad_nav"
    }
""".trimIndent()

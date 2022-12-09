import java.time.LocalDateTime


fun Int.januar(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 1, this, 9, 0)
fun Int.februar(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 2, this, 9, 0)
fun Int.mars(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 3, this, 9, 0)
fun Int.april(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 4, this, 9, 0)
fun Int.mai(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 5, this, 9, 0)
fun Int.juni(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 6, this, 9, 0)
fun Int.juli(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 7, this, 9, 0)
fun Int.august(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 8, this, 9, 0)
fun Int.september(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 9, this, 9, 0)
fun Int.oktober(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 10, this, 9, 0)
fun Int.november(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 11, this, 9, 0)
fun Int.desember(year: Int = 2022): LocalDateTime = LocalDateTime.of(year, 12, this, 9, 0)